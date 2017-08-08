/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * PowerDatacenterNonPowerAware is a class that represents a <b>non-power</b> aware data center in the
 * context of power-aware simulations.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerDatacenterNonPowerAware extends PowerDatacenter {

	/**
	 * Instantiates a new datacenter.
	 * 
	 * @param name the datacenter name
	 * @param characteristics the datacenter characteristics
	 * @param schedulingInterval the scheduling interval
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 * 
	 * @throws Exception the exception
	 */
	public PowerDatacenterNonPowerAware(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
	}

	@Override
	protected void updateCloudletProcessing() {
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();
		double timeframePower = 0.0;

		if (currentTime > getLastProcessTime()) {
			double timeDiff = currentTime - getLastProcessTime();
			double minTime = Double.MAX_VALUE;

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
				if (time < minTime) {
					minTime = time;
				}
			}

			if (getLogger().isInfoEnabled()) {
				String hostCpuUsage = this.<PowerHost>getHostList().stream()
						.map(host -> "<" + host + "> " + String.format("%.2f%%", host.getUtilizationOfCpu() * 100))
						.collect(Collectors.joining(", "));
				getLogger().info("hosts CPU usage at {}: {}", String.format("%.3f", currentTime), hostCpuUsage);
			}

			StringJoiner hostPowerUsageJoiner = new StringJoiner(", ");

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double hostPower = 0.0;

				hostPower = host.getMaxPower() * timeDiff;
				timeframePower += hostPower;

				hostPowerUsageJoiner.add("<" + host + "> " + String.format("%.2fWs", hostPower));
			}

			getLogger().info("hosts power usage for time frame {}: {}",
					String.format("[%.3f, %.3f]", this.getLastProcessTime(), currentTime),
					hostPowerUsageJoiner);

			getLogger().info("total power usage for time frame {}: {}",
					String.format("[%.3f, %.3f]", this.getLastProcessTime(), currentTime),
					timeframePower);

			setPower(getPower() + timeframePower);

			checkCloudletCompletion();

			/** Remove completed VMs **/
			for (PowerHost host : this.<PowerHost> getHostList()) {
				for (Vm vm : host.getCompletedVms()) {
					getVmAllocationPolicy().deallocateHostForVm(vm);
					getVmList().remove(vm);
					getLogger().info("removed completed VM {} from host {}", vm, host);
				}
			}

			if (!isDisableMigrations()) {
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
						getVmList());

				if (migrationMap != null) {
					for (Map<String, Object> migrate : migrationMap) {
						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if (oldHost == null)
							getLogger().info("started migrating VM {} to host {}", vm, targetHost);
						else
							getLogger().info("started migrating VM {} from host {} to host {}", vm, oldHost, targetHost);

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth + C (C = 10 sec) **/
						send(
								getId(),
								vm.getRam() / ((double) vm.getBw() / 8000) + 10,
								CloudSimTags.VM_MIGRATE,
								migrate);
					}
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				// CloudSim.cancelAll(getId(), CloudSim.SIM_ANY);
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}

			setLastProcessTime(currentTime);
		}
	}

}
