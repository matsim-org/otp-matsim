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

import javax.inject.Inject;
import javax.inject.Provider;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.router.RoutingModule;

import com.google.inject.name.Named;

public class OTPMatsimRoutingModuleProvider implements Provider<RoutingModule> {

	private final OTPMatsim otpMatsim;
	private final Scenario scenario;
	private final RoutingModule transitWalkRouter;

	@Inject
	OTPMatsimRoutingModuleProvider(OTPMatsim otpMatsim, Scenario scenario,
			@Named(TransportMode.transit_walk) RoutingModule transitWalkRouter) {
		this.otpMatsim = otpMatsim;
		this.scenario = scenario;
		this.transitWalkRouter = transitWalkRouter;
	}

	@Override
	public RoutingModule get() {
		return new OTPMatsimRoutingModule(otpMatsim, scenario.getTransitSchedule(), scenario.getNetwork(),
				transitWalkRouter);
	}

}
