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

import java.io.File;

import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.InputStreamGraphSource;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.standalone.OTPMain;
import org.opentripplanner.visualizer.GraphVisualizer;

public class RunOTP {
	
	public static void runGraphBuilder(String baseDir){
		OTPMain.main(new String[]{"--build", baseDir});
	}
	
    public static void runGraphVisualizer(String baseDir) {
        GraphVisualizer graphVisualizer = new GraphVisualizer(createGraphService(baseDir).getRouter());
        graphVisualizer.setVisible(true);
    }
    
    public static final GraphService createGraphService(String graphFile) {
		GraphService graphService = new GraphService();
		graphService.registerGraph("routerId", InputStreamGraphSource.newFileGraphSource("routerId", new File(graphFile), Graph.LoadLevel.FULL));
		return graphService;
    }

}
