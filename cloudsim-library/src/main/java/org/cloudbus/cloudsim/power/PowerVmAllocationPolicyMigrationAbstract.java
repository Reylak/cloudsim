/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.lists.PowerVmList;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract power-aware VM allocation policy that dynamically optimizes the VM
 * allocation (placement) using migration.
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
 * @since CloudSim Toolkit 3.0
 */
public abstract class PowerVmAllocationPolicyMigrationAbstract extends PowerVmAllocationPolicyAbstract {

	/** The vm selection policy. */
	private PowerVmSelectionPolicy vmSelectionPolicy;

	/** A list of maps between a VM and the host where it is place.
         * @todo This list of map is implemented in the worst way.
         * It should be used just a Map<Vm, Host> to find out 
         * what PM is hosting a given VM.
         */
	private final Map<Vm, Host> savedAllocation = new HashMap<>();

	/** A map of CPU utilization history (in percentage) for each host,
         where each key is a host id and each value is the CPU utilization percentage history.*/
	private final Map<Integer, List<Double>> utilizationHistory = new HashMap<Integer, List<Double>>();

	/** 
         * The metric history. 
         * @todo the map stores different data. Sometimes it stores the upper threshold,
         * other it stores utilization threshold or predicted utilization, that
         * is very confusing.
         */
	private final Map<Integer, List<Double>> metricHistory = new HashMap<Integer, List<Double>>();

	/** The time when entries in each history list was added. 
         * All history lists are updated at the same time.
         */
	private final Map<Integer, List<Double>> timeHistory = new HashMap<Integer, List<Double>>();

	/** The history of time spent in VM selection 
         * every time the optimization of VM allocation method is called. 
         * @see #optimizeAllocation(java.util.List) 
         */
	private final List<Double> executionTimeHistoryVmSelection = new LinkedList<Double>();

	/** The history of time spent in host selection 
         * every time the optimization of VM allocation method is called. 
         * @see #optimizeAllocation(java.util.List) 
         */
	private final List<Double> executionTimeHistoryHostSelection = new LinkedList<Double>();

	/** The history of time spent in VM reallocation 
         * every time the optimization of VM allocation method is called. 
         * @see #optimizeAllocation(java.util.List) 
         */
	private final List<Double> executionTimeHistoryVmReallocation = new LinkedList<Double>();

	/** The history of total time spent in every call of the 
         * optimization of VM allocation method. 
         * @see #optimizeAllocation(java.util.List) 
         */
	private final List<Double> executionTimeHistoryTotal = new LinkedList<Double>();

	/**
	 * Instantiates a new PowerVmAllocationPolicyMigrationAbstract.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 */
	public PowerVmAllocationPolicyMigrationAbstract(
			List<? extends PowerHost> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy) {
		this(hostList, OVERSUBSCRIBE_DEFAULT, vmSelectionPolicy);
	}

	public PowerVmAllocationPolicyMigrationAbstract(
			List<? extends PowerHost> list, boolean oversubscribe,
			PowerVmSelectionPolicy vmSelectionPolicy) {
		super(list, oversubscribe);
		init(vmSelectionPolicy);
	}

	public PowerVmAllocationPolicyMigrationAbstract(
			List<? extends PowerHost> list,
			PowerHostSuitabilityEvaluationAbstract suitabilityEvaluation,
			PowerVmSelectionPolicy vmSelectionPolicy) {
		super(list, suitabilityEvaluation);
		init(vmSelectionPolicy);
	}

	private void init(PowerVmSelectionPolicy vmSelectionPolicy) {
		setVmSelectionPolicy(vmSelectionPolicy);
		getLogger().setPrefix("VM alloc. (migration): ");
	}

	/**
	 * Optimize allocation of the VMs according to current utilization.
	 * 
	 * @param vmList the vm list
	 * 
	 * @return the array list< hash map< string, object>>
	 */
	@Override
	public Map<? extends PowerVm, ? extends PowerHost> optimizeAllocation(List<? extends Vm> vmList) {
		ExecutionTimeMeasurer.start("optimizeAllocationTotal");

		ExecutionTimeMeasurer.start("optimizeAllocationHostSelection");
		List<PowerHostUtilizationHistory> overUtilizedHosts = getOverUtilizedHosts();
		getExecutionTimeHistoryHostSelection().add(
				ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));

		getLogger().debug("overused hosts: {}",
                overUtilizedHosts.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")));

		saveAllocation();

		ExecutionTimeMeasurer.start("optimizeAllocationVmSelection");
		List<? extends PowerVm> vmsToMigrate = getVmsToMigrateFromHosts(overUtilizedHosts);
		getExecutionTimeHistoryVmSelection().add(ExecutionTimeMeasurer.end("optimizeAllocationVmSelection"));

		ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");
		Map<PowerVm, PowerHost> migrationMap = getNewVmPlacement(vmsToMigrate, new HashSet<Host>(
				overUtilizedHosts));
		getExecutionTimeHistoryVmReallocation().add(
				ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));

		migrationMap.putAll(getMigrationMapFromUnderUtilizedHosts(overUtilizedHosts));

		restoreAllocation();

		getExecutionTimeHistoryTotal().add(ExecutionTimeMeasurer.end("optimizeAllocationTotal"));

		return migrationMap;
	}

	/**
	 * Gets the migration map from under utilized hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 * @return the migration map from under utilized hosts
	 */
	protected Map<PowerVm, ? extends PowerHost> getMigrationMapFromUnderUtilizedHosts(
			List<PowerHostUtilizationHistory> overUtilizedHosts) {
		Map<PowerVm, PowerHost> migrationMap = new HashMap<>();
		List<PowerHost> switchedOffHosts = getSwitchedOffHosts();

		Set<PowerHost> excludedHostsForFindingUnderUtilizedHost = new HashSet<>();
		excludedHostsForFindingUnderUtilizedHost.addAll(overUtilizedHosts);
		excludedHostsForFindingUnderUtilizedHost.addAll(switchedOffHosts);

		// over-utilized + under-utilized hosts
		Set<PowerHost> excludedHostsForFindingNewVmPlacement = new HashSet<PowerHost>();
		excludedHostsForFindingNewVmPlacement.addAll(overUtilizedHosts);
		excludedHostsForFindingNewVmPlacement.addAll(switchedOffHosts);

		int numberOfHosts = getHostList().size();

		while (true) {
			if (numberOfHosts == excludedHostsForFindingUnderUtilizedHost.size())
				break;

			PowerHost underUtilizedHost = getUnderUtilizedHost(excludedHostsForFindingUnderUtilizedHost);
			if (underUtilizedHost == null)
				break;

			getLogger().debug("emptying underused host {}", underUtilizedHost);

			excludedHostsForFindingUnderUtilizedHost.add(underUtilizedHost);
			excludedHostsForFindingNewVmPlacement.add(underUtilizedHost);

			List<? extends PowerVm> vmsToMigrateFromUnderUtilizedHost = getVmsToMigrateFromUnderUtilizedHost(underUtilizedHost);
			if (vmsToMigrateFromUnderUtilizedHost.isEmpty())
				continue;

			getLogger().debug("trying to relocate VMs {}",
					vmsToMigrateFromUnderUtilizedHost.stream()
							.map(Object::toString)
							.collect(Collectors.joining(", ")));

			Map<PowerVm, PowerHost> newVmPlacement = getNewVmPlacementFromUnderUtilizedHost(
					vmsToMigrateFromUnderUtilizedHost,
					excludedHostsForFindingNewVmPlacement);

			excludedHostsForFindingUnderUtilizedHost.addAll(newVmPlacement.values());

			migrationMap.putAll(newVmPlacement);
		}

		return migrationMap;
	}

	/**
	 * Finds a PM that has enough resources to host a given VM
         * and that will not be overloaded after placing the VM on it.
         * The selected host will be that one with most efficient
         * power usage for the given VM.
	 * 
	 * @param vm the VM
	 * @param excludedHosts the excluded hosts
	 * @return the host found to host the VM
	 */
	public <T extends PowerHost> T findHostForVm(PowerVm vm, Set<? extends Host> excludedHosts) {
		double minPower = Double.MAX_VALUE;
		T allocatedHost = null;

		for (T host : this.<T>getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			if (isHostSuitableForVm(host, vm)) {
				if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
					continue;
				}

				try {
					double powerAfterAllocation = getPowerAfterAllocation(host, vm);
					if (powerAfterAllocation != -1) {
						double powerDiff = powerAfterAllocation - host.getPower();
						if (powerDiff < minPower) {
							minPower = powerDiff;
							allocatedHost = host;
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return allocatedHost;
	}

	/**
	 * Checks if a host will be over utilized after placing of a candidate VM.
	 * 
	 * @param host the host to verify
	 * @param vm the candidate vm 
	 * @return true, if the host will be over utilized after VM placement; false otherwise
	 */
	protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, Vm vm) {
		boolean isHostOverUtilizedAfterAllocation = true;
		if (host.vmCreate(vm)) {
			isHostOverUtilizedAfterAllocation = isHostOverUtilized(host);
			host.vmDestroy(vm);
		}
		return isHostOverUtilizedAfterAllocation;
	}

	@Override
	public PowerHost findHostForVm(PowerVm vm) {
		Set<Host> excludedHosts = new HashSet<Host>();
		if (vm.getHost() != null) {
			excludedHosts.add(vm.getHost());
		}
		return findHostForVm(vm, excludedHosts);
	}

	/**
	 * Gets a new vm placement considering the list of VM to migrate.
	 * 
	 * @param vmsToMigrate the list of VMs to migrate
	 * @param excludedHosts the list of hosts that aren't selected as destination hosts
	 * @return the new vm placement map
	 */
	protected <T extends PowerHost> Map<PowerVm, T> getNewVmPlacement(
			List<? extends PowerVm> vmsToMigrate,
			Set<? extends Host> excludedHosts) {
		Map<PowerVm, T> migrationMap = new HashMap<>();
		PowerVmList.sortByCpuUtilization(vmsToMigrate);
		for (PowerVm vm : vmsToMigrate) {
			T allocatedHost = findHostForVm(vm, excludedHosts);
			if (allocatedHost != null) {
				allocatedHost.vmCreate(vm);

				getLogger().debug("allocated VM {} to host {}", vm, allocatedHost);

				migrationMap.put(vm, allocatedHost);
			}
		}
		return migrationMap;
	}

	/**
	 * Gets the new vm placement from under utilized host.
	 * 
	 * @param vmsToMigrate the list of VMs to migrate
	 * @param excludedHosts the list of hosts that aren't selected as destination hosts
	 * @return the new vm placement from under utilized host
	 */
	protected Map<PowerVm, PowerHost> getNewVmPlacementFromUnderUtilizedHost(
			List<? extends PowerVm> vmsToMigrate,
			Set<? extends Host> excludedHosts) {
		Map<PowerVm, PowerHost> migrationMap = new HashMap<>();
		PowerVmList.sortByCpuUtilization(vmsToMigrate);
		for (PowerVm vm : vmsToMigrate) {
			PowerHost allocatedHost = findHostForVm(vm, excludedHosts);
			if (allocatedHost != null) {
				allocatedHost.vmCreate(vm);

				getLogger().debug("allocated VM {} to host {}", vm, allocatedHost);

				migrationMap.put(vm, allocatedHost);
			} else {
				getLogger().debug("failed relocating all VMs from underused host {}; cancelling "
                        + "relocation", vm.getHost());

				for (Map.Entry<PowerVm, PowerHost> entry: migrationMap.entrySet())
					entry.getValue().vmDestroy(entry.getKey());
				migrationMap.clear();
				break;
			}
		}
		return migrationMap;
	}

	/**
	 * Gets the VMs to migrate from hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 * @return the VMs to migrate from hosts
	 */
	protected List<? extends PowerVm>
	  getVmsToMigrateFromHosts(List<PowerHostUtilizationHistory> overUtilizedHosts) {
		List<PowerVm> vmsToMigrate = new LinkedList<>();
		for (PowerHostUtilizationHistory host : overUtilizedHosts) {
			while (true) {
				PowerVm vm = (PowerVm)getVmSelectionPolicy().getVmToMigrate(host);
				if (vm == null) {
					break;
				}
				vmsToMigrate.add(vm);
				host.vmDestroy(vm);
				if (!isHostOverUtilized(host)) {
					break;
				}
			}
		}
		return vmsToMigrate;
	}

	/**
	 * Gets the VMs to migrate from under utilized host.
	 * 
	 * @param host the host
	 * @return the vms to migrate from under utilized host
	 */
	protected List<? extends PowerVm> getVmsToMigrateFromUnderUtilizedHost(PowerHost host) {
		List<PowerVm> vmsToMigrate = new LinkedList<>();
		for (PowerVm vm : host.<PowerVm>getVmList()) {
			if (!vm.isInMigration()) {
				vmsToMigrate.add(vm);
			}
		}
		return vmsToMigrate;
	}

	/**
	 * Gets the over utilized hosts.
	 * 
	 * @return the over utilized hosts
	 */
	protected List<PowerHostUtilizationHistory> getOverUtilizedHosts() {
		List<PowerHostUtilizationHistory> overUtilizedHosts = new LinkedList<PowerHostUtilizationHistory>();
		for (PowerHostUtilizationHistory host : this.<PowerHostUtilizationHistory> getHostList()) {
			if (isHostOverUtilized(host)) {
				overUtilizedHosts.add(host);
			}
		}
		return overUtilizedHosts;
	}

	/**
	 * Gets the switched off hosts.
	 * 
	 * @return the switched off hosts
	 */
	protected List<PowerHost> getSwitchedOffHosts() {
		List<PowerHost> switchedOffHosts = new LinkedList<PowerHost>();
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.getUtilizationOfCpu() == 0) {
				switchedOffHosts.add(host);
			}
		}
		return switchedOffHosts;
	}

	/**
	 * Gets the most under utilized host.
	 * 
	 * @param excludedHosts the excluded hosts
	 * @return the most under utilized host
	 */
	protected PowerHost getUnderUtilizedHost(Set<? extends Host> excludedHosts) {
		double minUtilization = 1;
		PowerHost underUtilizedHost = null;
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			double utilization = host.getUtilizationOfCpu();
			if (utilization > 0 && utilization < minUtilization
					&& !areAllVmsMigratingOutOrAnyVmMigratingIn(host)) {
				minUtilization = utilization;
				underUtilizedHost = host;
			}
		}
		return underUtilizedHost;
	}

	/**
	 * Checks whether all VMs of a given host are in migration.
	 * 
	 * @param host the host
	 * @return true, if successful
	 */
	protected boolean areAllVmsMigratingOutOrAnyVmMigratingIn(PowerHost host) {
		for (PowerVm vm : host.<PowerVm> getVmList()) {
			if (!vm.isInMigration()) {
				return false;
			}
			if (host.getVmsMigratingIn().contains(vm)) {
				return true;
			}
		}
		return true;
	}

	/**
	 * Checks if host is over utilized.
	 * 
	 * @param host the host
	 * @return true, if the host is over utilized; false otherwise
	 */
	protected abstract boolean isHostOverUtilized(PowerHost host);

	/**
	 * Adds an entry for each history map of a host.
	 * 
	 * @param host the host to add metric history entries
	 * @param metric the metric to be added to the metric history map
	 */
	protected void addHistoryEntry(HostDynamicWorkload host, double metric) {
		int hostId = host.getId();
		if (!getTimeHistory().containsKey(hostId)) {
			getTimeHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getUtilizationHistory().containsKey(hostId)) {
			getUtilizationHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getMetricHistory().containsKey(hostId)) {
			getMetricHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getTimeHistory().get(hostId).contains(CloudSim.clock())) {
			getTimeHistory().get(hostId).add(CloudSim.clock());
			getUtilizationHistory().get(hostId).add(host.getUtilizationOfCpu());
			getMetricHistory().get(hostId).add(metric);
		}
	}

	/**
	 * Updates the list of maps between a VM and the host where it is place.
         * @see #savedAllocation
	 */
	protected void saveAllocation() {
		getSavedAllocation().clear();
		for (Host host : getHostList()) {
			for (Vm vm : host.getVmList().stream()
							 .filter(vm -> !host.getVmsMigratingIn().contains(vm))
							 .collect(Collectors.toList()))
				this.savedAllocation.put(vm, host);
		}
	}

	/**
	 * Restore VM allocation from the allocation history.
         * @see #savedAllocation
	 */
	protected void restoreAllocation() {
		for (Host host : getHostList()) {
			host.vmDestroyAll();
			host.reallocateMigratingInVms();
		}
		for (Map.Entry<Vm, Host> allocation: getSavedAllocation().entrySet()) {
		    Vm vm = allocation.getKey();
		    Host host = allocation.getValue();
			if (!host.vmCreate(vm)) {
				getLogger().error("failed restoring allocation of VM {} on host {}", vm, host);
				System.exit(0);
			}
			getVmTable().put(vm.getUid(), host);
		}
	}

	/**
	 * Gets the power consumption of a host after placement of a candidate VM.
         * The VM is not in fact placed at the host.
	 * 
	 * @param host the host
	 * @param vm the candidate vm
	 * 
	 * @return the power after allocation
	 */
	protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
		double power = 0;
		try {
			power = host.getPower(getMaxUtilizationAfterAllocation(host, vm));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the max power consumption of a host after placement of a candidate VM.
         * The VM is not in fact placed at the host.
         * We assume that load is balanced between PEs. The only
	 * restriction is: VM's max MIPS < PE's MIPS
	 * 
	 * @param host the host
	 * @param vm the vm
	 * 
	 * @return the power after allocation
	 */
	protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
		double requestedTotalMips = vm.getCurrentRequestedTotalMips();
		double hostUtilizationMips = getUtilizationOfCpuMips(host);
		double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
		double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips();
		return pePotentialUtilization;
	}
	
	/**
	 * Gets the utilization of the CPU in MIPS for the current potentially allocated VMs.
	 *
	 * @param host the host
	 *
	 * @return the utilization of the CPU in MIPS
	 */
	protected double getUtilizationOfCpuMips(PowerHost host) {
		double hostUtilizationMips = 0;
		for (Vm vm2 : host.getVmList()) {
			if (host.getVmsMigratingIn().contains(vm2)) {
				// calculate additional potential CPU usage of a migrating in VM
				hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2) * 0.9 / 0.1;
			}
			hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
		}
		return hostUtilizationMips;
	}

	/**
	 * Gets the saved allocation.
	 * 
	 * @return the saved allocation
	 */
	protected Map<Vm, Host> getSavedAllocation() {
		return savedAllocation;
	}

	/**
	 * Sets the vm selection policy.
	 * 
	 * @param vmSelectionPolicy the new vm selection policy
	 */
	protected void setVmSelectionPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
		this.vmSelectionPolicy = vmSelectionPolicy;
	}

	/**
	 * Gets the vm selection policy.
	 * 
	 * @return the vm selection policy
	 */
	protected PowerVmSelectionPolicy getVmSelectionPolicy() {
		return vmSelectionPolicy;
	}

	/**
	 * Gets the utilization history.
	 * 
	 * @return the utilization history
	 */
	public Map<Integer, List<Double>> getUtilizationHistory() {
		return utilizationHistory;
	}

	/**
	 * Gets the metric history.
	 * 
	 * @return the metric history
	 */
	public Map<Integer, List<Double>> getMetricHistory() {
		return metricHistory;
	}

	/**
	 * Gets the time history.
	 * 
	 * @return the time history
	 */
	public Map<Integer, List<Double>> getTimeHistory() {
		return timeHistory;
	}

	/**
	 * Gets the execution time history vm selection.
	 * 
	 * @return the execution time history vm selection
	 */
	public List<Double> getExecutionTimeHistoryVmSelection() {
		return executionTimeHistoryVmSelection;
	}

	/**
	 * Gets the execution time history host selection.
	 * 
	 * @return the execution time history host selection
	 */
	public List<Double> getExecutionTimeHistoryHostSelection() {
		return executionTimeHistoryHostSelection;
	}

	/**
	 * Gets the execution time history vm reallocation.
	 * 
	 * @return the execution time history vm reallocation
	 */
	public List<Double> getExecutionTimeHistoryVmReallocation() {
		return executionTimeHistoryVmReallocation;
	}

	/**
	 * Gets the execution time history total.
	 * 
	 * @return the execution time history total
	 */
	public List<Double> getExecutionTimeHistoryTotal() {
		return executionTimeHistoryTotal;
	}

}
