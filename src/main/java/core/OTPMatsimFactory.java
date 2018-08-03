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

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.router.RoutingModule;
import org.matsim.pt.router.TransitRouterConfig;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.InputStreamGraphSource;
import org.opentripplanner.routing.services.GraphService;

import config.OTPMatsimConfigGroup;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gleich
 */
@Singleton
public class OTPMatsimFactory implements Provider<OTPMatsim> {

	private GraphService graphService = null;
	private final Network matsimNetwork;
	private final TransitSchedule matsimSchedule;
	private final TransitRouterConfig transitRouterConfig;
	private final OTPMatsimConfigGroup otpMatsimConfigGroup;
	private final Map<String, Provider<RoutingModule>> routingModuleProviders;

	@Inject
	public OTPMatsimFactory(final TransitSchedule schedule, final Config config, final Network network,
			Map<String, Provider<RoutingModule>> routingModules) {
		this.matsimSchedule = schedule;
		this.matsimNetwork = network;
		this.transitRouterConfig = new TransitRouterConfig(config);

		otpMatsimConfigGroup = ConfigUtils.addOrGetModule(config, OTPMatsimConfigGroup.class);
		this.routingModuleProviders = new HashMap<>();
	}

	@Override
	public OTPMatsim get() {
		GraphService graphService = getGraphService();
		Map<String, RoutingModule> neededRoutingModules = new HashMap<>();
		for (Map.Entry<String, Provider<RoutingModule>> e : this.routingModuleProviders.entrySet()) {
			String mode = e.getKey();
			RoutingModule module = e.getValue().get();
			neededRoutingModules.put(mode, module);
		}
		return new OTPMatsim(graphService, matsimSchedule, matsimNetwork, neededRoutingModules, transitRouterConfig,
				otpMatsimConfigGroup);
	}

	private GraphService getGraphService() {
		if (graphService == null) {
			graphService = RunOTP.createGraphService(otpMatsimConfigGroup.getOtpGraphFile());
		}
		return graphService;
	}

}
