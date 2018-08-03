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

package config;

import org.matsim.core.config.ReflectiveConfigGroup;

import java.util.Map;

/**
 * @author gleich
 */
public class OTPMatsimConfigGroup extends ReflectiveConfigGroup {

	public static final String GROUP = "otpMatsim";
	private static final String PARAM_OTP_GRAPH_FILE = "otpGraphFile";
	private static final String PARAM_OTP_TIME_ZONE = "otpTimeZone";
	private static final String PARAM_OTP_DAY = "otpDay";
	private static final String PARAM_OTP_COORDINATE_SYSTEM = "otpCoordinateSystem";

	private String otpGraphFile;
	private String otpTimeZone;
	private String otpDay;
	private String otpCoordinateSystem;

	public OTPMatsimConfigGroup() {
		super(GROUP);
	}

	@StringGetter(PARAM_OTP_GRAPH_FILE)
	public String getOtpGraphFile() {
		return this.otpGraphFile;
	}

	@StringSetter(PARAM_OTP_GRAPH_FILE)
	public void setOtpGraphFile(String otpGraphFile) {
		this.otpGraphFile = otpGraphFile;
	}

	@StringGetter(PARAM_OTP_TIME_ZONE)
	public String getOtpTimeZone() {
		return this.otpTimeZone;
	}

	@StringSetter(PARAM_OTP_TIME_ZONE)
	public void setOtpTimeZone(String otpTimeZone) {
		this.otpTimeZone = otpTimeZone;
	}
	
	@StringGetter(PARAM_OTP_DAY)
	public String getOtpDay() {
		return this.otpDay;
	}

	@StringSetter(PARAM_OTP_DAY)
	public void setOtpDay(String otpDay) {
		this.otpDay = otpDay;
	}
	
	@StringGetter(PARAM_OTP_COORDINATE_SYSTEM)
	public String getOtpCoordinateSystem() {
		return this.otpCoordinateSystem;
	}

	@StringSetter(PARAM_OTP_COORDINATE_SYSTEM)
	public void setOtpCoordinateSystem(String otpCoordinateSystem) {
		this.otpCoordinateSystem = otpCoordinateSystem;
	}

	@Override
	public Map<String, String> getComments() {
		Map<String, String> comments = super.getComments();
		comments.put(PARAM_OTP_GRAPH_FILE,
				"Input file containing the OTP graph, which has to be created before e.g. by RunOTP.runGraphBuilder().");
		comments.put(PARAM_OTP_TIME_ZONE,
				"Time zone string to be used in OTP (OTP needs a specific date and time zone whereas matsim tipically has only one day).");
		comments.put(PARAM_OTP_DAY,
				"Day string to be used in OTP (OTP needs a specific date and time zone whereas matsim tipically has only one day).");
		comments.put(PARAM_OTP_COORDINATE_SYSTEM,
				"Coordinate system of target coordinate system (gtfs for OTP is always WGS-84?).");
return comments;
	}
}
