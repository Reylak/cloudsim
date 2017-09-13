package org.cloudbus.cloudsim.examples.power.planetlab;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.power.PowerDatacenterNonPowerAware;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.List;

/**
 * A simulation of a heterogeneous non-power aware data center: all hosts consume maximum power all
 * the time.
 * 
 * This example uses a real PlanetLab workload: 20110303.
 * 
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class NonPowerAware {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		String experimentName = "planetlab_npa";
		String outputFolder = "output";
		String inputFolder = NonPowerAware.class.getClassLoader().getResource("workload/planetlab/20110303")
				.getPath();

		getLogger().info("starting {}", experimentName);

		try {
			CloudSim.init(1, Calendar.getInstance());

			DatacenterBroker broker = Helper.createBroker();
			int brokerId = broker.getId();

			List<Cloudlet> cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
			List<Vm> vmList = Helper.createVmList(brokerId, cloudletList.size());
			List<PowerHost> hostList = Helper.createHostList(PlanetLabConstants.NUMBER_OF_HOSTS);

			PowerDatacenterNonPowerAware datacenter = (PowerDatacenterNonPowerAware) Helper.createDatacenter(
					"Datacenter",
					PowerDatacenterNonPowerAware.class,
					hostList,
					new PowerVmAllocationPolicySimple(hostList));

			datacenter.setDisableMigrations(true);

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);

			CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
			double lastClock = CloudSim.startSimulation();

			getLogger().info("received {} cloudlets", cloudletList.size());

			CloudSim.stopSimulation();

			Helper.printResults(
					datacenter,
					vmList,
					lastClock,
					experimentName,
					Constants.OUTPUT_CSV,
					outputFolder);

		} catch (Exception e) {
			getLogger().error("simulation terminated due to unexpected error", e);
			System.exit(0);
		}

		getLogger().info("terminated experiment {}", experimentName);
	}

	public static Logger getLogger() {
		return logger;
	}
}
