/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * PowerDatacenter is a class that enables simulation of power-aware data centers.
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
public class PowerDatacenter extends Datacenter {

	/** The datacenter consumed power. */
	private double power;

	/** Indicates if migrations are disabled or not. */
	private boolean disableMigrations;

	/** The last time submitted cloudlets were processed. */
	private double cloudletSubmitted;

	/** The VM migration count. */
	private int migrationCount;

	/**
	 * Instantiates a new PowerDatacenter.
	 * 
	 * @param name the datacenter name
	 * @param characteristics the datacenter characteristics
	 * @param schedulingInterval the scheduling interval
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 */
	public PowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
	}

	@Override
	protected void updateCloudletProcessing() {
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();
        double timeFrameDatacenterEnergy = 0.0;

		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			if (!isDisableMigrations()) {
				Map<? extends Vm, ? extends Host> migrationMap = getVmAllocationPolicy().optimizeAllocation(getVmList());

				for (Map.Entry<? extends Vm, ? extends Host> entry : migrationMap.entrySet()) {
					Vm vm = entry.getKey();
					PowerHost targetHost = (PowerHost) entry.getValue();
					PowerHost oldHost = (PowerHost) vm.getHost();

					if (oldHost == null)
						getLogger().debug("started migrating VM {} to host {}", vm, targetHost);
					else
						getLogger().debug("started migrating VM {} from host {} to host {}", vm, oldHost, targetHost);

					targetHost.addMigratingInVm(vm);
					incrementMigrationCount();

					/* VM migration delay = RAM / available bandwidth
					 * We simulate the bandwidth available for migration, with total bandwidth / 2;
					 * the other half of the bandwidth being for normal VM communication
					 */
					Map<String, Object> eventParam = new HashMap<>();
					eventParam.put("vm", vm);
					eventParam.put("host", targetHost);
					send(
							getId(),
							vm.getRam() / ((double) targetHost.getBw() / (2 * 8)),
							CloudSimTags.VM_MIGRATE, eventParam);
				}
			}

			if (getLogger().isTraceEnabled()) {
				String hostCpuUsage = this.<PowerHost>getHostList().stream()
						.map(host -> "<" + host + "> " + String.format("%.2f%%", host.getUtilizationOfCpu() * 100))
						.collect(Collectors.joining(", "));
				getLogger().trace("hosts CPU usage at {}: {}", String.format("%.3f", currentTime), hostCpuUsage);
			}

			StringJoiner hostPowerUsageJoiner = new StringJoiner(", ");

			for (PowerHost host : this.<PowerHost>getHostList()) {
				double timeFrameHostEnergy = host.getEnergyConsumption(this.getLastProcessTime(), currentTime - 1);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				hostPowerUsageJoiner.add("<" + host + "> " + String.format("%.2fWs", timeFrameHostEnergy));
			}

			getLogger().trace("hosts power usage for time frame {}: {}",
					String.format("[%.3f, %.3f]", this.getLastProcessTime(), currentTime),
					hostPowerUsageJoiner);

			getLogger().info("total power usage for time frame {}: {}",
					String.format("[%.3f, %.3f]", this.getLastProcessTime(), currentTime),
					String.format("%.2fWs", timeFrameDatacenterEnergy));

			setPower(getPower() + timeFrameDatacenterEnergy);

			// ensure a minimal time between simulation events
			minTime = Math.max(minTime, CloudSim.clock() + CloudSim.getMinTimeBetweenEvents() + 0.01);
			// set time of next event to scheduling date, or to date of next event if closer
			minTime = Math.min(minTime, CloudSim.clock() + (this.getSchedulingInterval() - (CloudSim.clock() % this.getSchedulingInterval())));
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			send(getId(), minTime - CloudSim.clock(), CloudSimTags.VM_DATACENTER_EVENT);

			setLastProcessTime(currentTime);
		}
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
         * @see #updateCloudetProcessingWithoutSchedulingFutureEventsForce() 
         * @todo There is an inconsistence in the return value of this
         * method with return value of similar methods
         * such as {@link #updateCloudetProcessingWithoutSchedulingFutureEventsForce()},
         * that returns {@link Double#MAX_VALUE} by default.
         * The current method returns 0 by default.
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
		if (CloudSim.clock() > getLastProcessTime()) {
			return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
		return 0;
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return expected time of completion of the next cloudlet in all VMs of all hosts or
	 *         {@link Double#MAX_VALUE} if there is no future events expected in this host
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			if (time < minTime) {
				minTime = time;
			}
		}

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				getLogger().info("removed completed VM {} from host {}", vm, host);
			}
		}

		setLastProcessTime(currentTime);
		return minTime;
	}

	@Override
	protected boolean processVmMigrate(Vm vm, Host host) {
		updateCloudetProcessingWithoutSchedulingFutureEvents();

		boolean res = super.processVmMigrate(vm, host);

		SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
		if (event == null || event.eventTime() > CloudSim.clock()) {
			updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}

		return res;
	}

	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}

	/**
	 * Gets the power.
	 * 
	 * @return the power
	 */
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power.
	 * 
	 * @param power the new power
	 */
	protected void setPower(double power) {
		this.power = power;
	}

	/**
	 * Checks if PowerDatacenter is in migration.
	 * 
	 * @return true, if PowerDatacenter is in migration; false otherwise
	 */
	protected boolean isInMigration() {
		boolean result = false;
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if migrations are disabled.
	 * 
	 * @return true, if  migrations are disable; false otherwise
	 */
	public boolean isDisableMigrations() {
		return disableMigrations;
	}

	/**
	 * Disable or enable migrations.
	 * 
	 * @param disableMigrations true to disable migrations; false to enable
	 */
	public void setDisableMigrations(boolean disableMigrations) {
		this.disableMigrations = disableMigrations;
	}

	/**
	 * Checks if is cloudlet submited.
	 * 
	 * @return true, if is cloudlet submited
	 */
	protected double getCloudletSubmitted() {
		return cloudletSubmitted;
	}

	/**
	 * Sets the cloudlet submitted.
	 * 
	 * @param cloudletSubmitted the new cloudlet submited
	 */
	protected void setCloudletSubmitted(double cloudletSubmitted) {
		this.cloudletSubmitted = cloudletSubmitted;
	}

	/**
	 * Gets the migration count.
	 * 
	 * @return the migration count
	 */
	public int getMigrationCount() {
		return migrationCount;
	}

	/**
	 * Sets the migration count.
	 * 
	 * @param migrationCount the new migration count
	 */
	protected void setMigrationCount(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	/**
	 * Increment migration count.
	 */
	protected void incrementMigrationCount() {
		setMigrationCount(getMigrationCount() + 1);
	}

}
