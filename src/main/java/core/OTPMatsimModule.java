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

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.RoutingModule;

/**
 * @author gleich
 */
public class OTPMatsimModule extends AbstractModule {

	@Override
	public void install() {
		if (getConfig().transit().isUseTransit()) {
			bind(OTPMatsim.class).toProvider(OTPMatsimFactory.class);

			for (String mode : getConfig().transit().getTransitModes()) {
				addRoutingModuleBinding(mode).toProvider(OTPMatsimRoutingModuleProvider.class);
			}
			addRoutingModuleBinding(TransportMode.transit_walk)
					.to(Key.get(RoutingModule.class, Names.named(TransportMode.walk)));
		}
	}

}
