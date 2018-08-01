package examples.vbb;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.PlanRouter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.transformations.IdentityTransformation;
import org.matsim.core.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.core.population.algorithms.ParallelPersonAlgorithmUtils;
import org.matsim.core.population.algorithms.PersonPrepareForSim;

import core.OTPTripRouterFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * 
 * @author mzilske
 * @author gleich
 *
 */
public class GenerateAndRoutePopulation {



	private static Population population;

	private Network network;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new GenerateAndRoutePopulation().convert();
	}



	public GenerateAndRoutePopulation() throws IOException, ClassNotFoundException {

	}



	private void convert() {
		// final Scenario scenario = readScenario();
		Config config = ConfigUtils.createConfig();
		config.scenario().setUseVehicles(true);
		config.scenario().setUseTransit(true);
		config.transit().setTransitScheduleFile("/Users/zilske/gtfs-bvg/transit-schedule.xml");
		config.transit().setVehiclesFile("/Users/zilske/gtfs-bvg/transit-vehicles.xml");
		config.network().setInputFile("/Users/zilske/gtfs-bvg/network.xml");
		final Scenario scenario = ScenarioUtils.loadScenario(config);

//		new MatsimNetworkReader(scenario.getNetwork()).readFile("/Users/zilske/gtfs-bvg/network.xml");
//		new VehicleReaderV1(scenario.getVehicles()).readFile(config.transit().getVehiclesFile());
//		new TransitScheduleReader(scenario).readFile(config.transit().getTransitScheduleFile());

		// new NetworkCleaner().run(scenario.getNetwork());
		System.out.println("Scenario has " + scenario.getNetwork().getLinks().size() + " links.");

		//	new NetworkWriter(scenario.getNetwork()).write("/Users/zilske/gtfs-bvg/network.xml");
		//	new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile("/Users/zilske/gtfs-bvg/transit-schedule.xml");
		//	new VehicleWriterV1(((ScenarioImpl) scenario).getVehicles()).writeFile("/Users/zilske/gtfs-bvg/transit-vehicles.xml");

		double minX=13.1949;
		double maxX=13.5657;
		double minY=52.3926;
		double maxY=52.6341;


		population = scenario.getPopulation();
		network = scenario.getNetwork();
		for (int i=0; i<10; ++i) {
			Coord source = CoordUtils.createCoord(minX + Math.random() * (maxX - minX), minY + Math.random() * (maxY - minY));
			Coord sink = CoordUtils.createCoord(minX + Math.random() * (maxX - minX), minY + Math.random() * (maxY - minY));
			Person person = population.getFactory().createPerson(Id.create(Integer.toString(i), Person.class));
			Plan plan = population.getFactory().createPlan();
			plan.addActivity(createHome(source));
			List<Leg> homeWork = createLeg(source, sink);
			for (Leg leg : homeWork) {
				plan.addLeg(leg);
			}
			plan.addActivity(createWork(sink));
			List<Leg> workHome = createLeg(sink, source);
			for (Leg leg : workHome) {
				plan.addLeg(leg);
			}
			plan.addActivity(createHome(source));
			person.addPlan(plan);
			population.addPerson(person);
		}

		final OTPTripRouterFactory trf = new OTPTripRouterFactory(scenario.getTransitSchedule(), 
				scenario.getNetwork(), new IdentityTransformation(), "2013-08-24", "Europe/Berlin", 
				"/Users/michaelzilske/gtfs-ulm/Graph.obj", false, 1, false);

		// make sure all routes are calculated.
		ParallelPersonAlgorithmUtils.run(population, config.global().getNumberOfThreads(),
				new ParallelPersonAlgorithmUtils.PersonAlgorithmProvider() {
			@Override
			public AbstractPersonAlgorithm getPersonAlgorithm() {
				return new PersonPrepareForSim(new PlanRouter(trf.get(), scenario.getActivityFacilities()), scenario);
			}
		});

		new PopulationWriter(population, scenario.getNetwork()).writeV5("/Users/zilske/gtfs-bvg/population.xml");

	}

	private List<Leg> createLeg(Coord source, Coord sink) {
		Leg leg = population.getFactory().createLeg(TransportMode.pt);
		return Arrays.asList(new Leg[]{leg});
	}

	private Activity createWork(Coord workLocation) {
		Activity activity = population.getFactory().createActivityFromCoord("work", workLocation);
		activity.setEndTime(17*60*60);
		activity.setLinkId(NetworkUtils.getNearestLinkExactly(network, workLocation).getId());
		return activity;
	}

	private Activity createHome(Coord homeLocation) {
		Activity activity = population.getFactory().createActivityFromCoord("home", homeLocation);
		activity.setEndTime(9*60*60);
		Link link = NetworkUtils.getNearestLinkExactly(network, homeLocation);
		activity.setLinkId(link.getId());
		return activity;
	}

}
