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

/**
 * Parameter Sets for otp route requests
 * 
 * The ParameterProfiles differ in the availability of transport modes, e.g.
 * pt+walk or bike only.
 * 
 * They do not differ in the maximum walk distance because this parameter
 * appears to have no influence on the itineraries proposed by otp except of a
 * warning message in the internet browser view (for example: SWU Ulm data set,
 * departure at 13:13 2014-02-14 from [48.41385, 9.94580] to [48.38379,
 * 9.95726]: for maximum walk distances 750m and 1750m all proposed itineraries
 * are equal and both propose an itinerary which includes a 1000m walk as second
 * choice although other itineraries with less walk distance are available.)
 * 
 * @author gleich
 *
 */
enum OtpParameterProfile {

	Pt_and_Walk(true, false), Pt_and_Bike(false, true), Bike_only(false, true);

	final boolean walkAllowed;
	final boolean bikeAllowed;

	OtpParameterProfile(boolean walkAllowed, boolean bikeAllowed) {
		this.walkAllowed = walkAllowed;
		this.bikeAllowed = bikeAllowed;
	}
}
