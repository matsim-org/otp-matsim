/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package core;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.facilities.Facility;
import org.matsim.pt.PtConstants;
import org.matsim.pt.router.TransitRouter;
import org.matsim.pt.routes.ExperimentalTransitRoute;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.*;
import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.algorithm.AStar;
import org.opentripplanner.routing.core.*;
import org.opentripplanner.routing.edgetype.OnboardEdge;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.TransitBoardAlight;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.routing.vertextype.TransitVertex;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * Real code was moved to OTPMatsim, this is now only what the
 * TransitRouterWrapper is for the default TransitRouter. Therefore code is
 * duplicated from there.
 *
 */
public class OTPMatsimRoutingModule implements RoutingModule {

	private final OTPMatsim otpMatsim;
	private final RoutingModule walkRouter;
	private final TransitSchedule transitSchedule;
	private final Network network;

	private final static Logger log = Logger.getLogger(OTPMatsimRoutingModule.class);

	public OTPMatsimRoutingModule(final OTPMatsim otpMatsim, final TransitSchedule transitSchedule, Network network,
			final RoutingModule walkRouter) {
		if (otpMatsim == null) {
			throw new NullPointerException("The otp-to-matsim-converter object is null, but is required later.");
		}
		this.otpMatsim = otpMatsim;
		this.transitSchedule = transitSchedule;
		this.network = network;
		if (walkRouter == null) {
			throw new NullPointerException("The walkRouter object is null, but is required later.");
		}
		this.walkRouter = walkRouter;
	}

	/**
	 * Just links to
	 * {@link TransitRouter#calcRoute(Facility, Facility, double, Person)}.
	 *
	 * @return the list of legs returned by the transit router.
	 */
	@Override
	public List<? extends PlanElement> calcRoute(final Facility fromFacility, final Facility toFacility,
			final double departureTime, final Person person) {
		List<Leg> baseTrip = otpMatsim.calcRoute(fromFacility, toFacility, departureTime, person);

		// the previous approach was to return null when no trip was found and
		// not to replace the trip if so.
		// However, this makes the output of routing modules more tricky to handle.
		// Thus, every module should return a valid trip. When available, the "main
		// mode" flag should be put to the mode of the routing module.
		return baseTrip != null ? fillWithActivities(baseTrip, fromFacility, toFacility, departureTime, person)
				: walkRouter.calcRoute(fromFacility, toFacility, departureTime, person);
	}

	/**
	 * This treats the TransitRouter as a third-party interface, where missing
	 * fields must be filled in (distance, travel-time in routes).
	 */
	private List<PlanElement> fillWithActivities(final List<Leg> baseTrip, final Facility fromFacility,
			final Facility toFacility, double departureTime, Person person) {
		List<PlanElement> trip = new ArrayList<>();
		Coord nextCoord = null;
		int i = 0;
		for (Leg leg : baseTrip) {
			if (i == 0) {
				// (access leg)
				Facility firstToFacility;
				if (baseTrip.size() > 1) { // at least one pt leg available
					ExperimentalTransitRoute tRoute = (ExperimentalTransitRoute) baseTrip.get(1).getRoute();
					firstToFacility = this.transitSchedule.getFacilities().get(tRoute.getAccessStopId());
				} else {
					firstToFacility = toFacility;
				}
				// (*)
				Route route = createWalkRoute(fromFacility, departureTime, person, leg.getTravelTime(),
						firstToFacility);
				leg.setRoute(route);
			} else {
				if (leg.getRoute() instanceof ExperimentalTransitRoute) {
					ExperimentalTransitRoute tRoute = (ExperimentalTransitRoute) leg.getRoute();
					tRoute.setTravelTime(leg.getTravelTime());
					tRoute.setDistance(RouteUtils.calcDistance(tRoute, transitSchedule, network));
					Activity act = PopulationUtils.createActivityFromCoordAndLinkId(PtConstants.TRANSIT_ACTIVITY_TYPE,
							this.transitSchedule.getFacilities().get(tRoute.getAccessStopId()).getCoord(),
							tRoute.getStartLinkId());
					act.setMaximumDuration(0.0);
					trip.add(act);
					nextCoord = this.transitSchedule.getFacilities().get(tRoute.getEgressStopId()).getCoord();
				} else {
					// it is not an instance of an ExperimentalTransitRoute so it must be a
					// (transit) walk leg.

					// walk legs don't have a coord, use the coord from the last egress point. yyyy
					// But I don't understand why in one case we take "nextCoord", while in the
					// other case we retrieve the facility from the previous route.

					if (i == baseTrip.size() - 1) {
						// if this is the last leg, we don't believe the leg from the TransitRouter.
						// Why?

						ExperimentalTransitRoute tRoute = (ExperimentalTransitRoute) baseTrip.get(baseTrip.size() - 2)
								.getRoute();
						Facility lastFromFacility = this.transitSchedule.getFacilities().get(tRoute.getEgressStopId());

						Route route = createWalkRoute(lastFromFacility, departureTime, person, leg.getTravelTime(),
								toFacility);
						leg.setRoute(route);
					}
					Activity act = PopulationUtils.createActivityFromCoordAndLinkId(PtConstants.TRANSIT_ACTIVITY_TYPE,
							nextCoord, leg.getRoute().getStartLinkId());
					act.setMaximumDuration(0.0);
					trip.add(act);
				}
			}
			trip.add(leg);
			i++;
		}
		return trip;
	}

	private Route createWalkRoute(final Facility fromFacility, double departureTime, Person person, double travelTime,
			Facility firstToFacility) {
		// yyyy I extracted this method to make a bit more transparent that it is used
		// twice. But I don't know why it is done in this way
		// (take distance from newly computed walk leg, but take travelTime from
		// elsewhere). Possibly, the problem is that the TransitRouter
		// historically just does not compute the distances. kai, may'17

		Route route = RouteUtils.createGenericRouteImpl(fromFacility.getLinkId(), firstToFacility.getLinkId());
		final List<? extends PlanElement> walkRoute = walkRouter.calcRoute(fromFacility, firstToFacility, departureTime,
				person);
		route.setDistance(((Leg) walkRoute.get(0)).getRoute().getDistance());
		route.setTravelTime(travelTime);
		return route;
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {
		return new StageActivityTypesImpl(Arrays.asList(PtConstants.TRANSIT_ACTIVITY_TYPE));
	}

}
