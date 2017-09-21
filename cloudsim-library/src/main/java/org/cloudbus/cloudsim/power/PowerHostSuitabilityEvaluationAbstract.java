package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.HostSuitabilityEvaluation;

public abstract class PowerHostSuitabilityEvaluationAbstract implements HostSuitabilityEvaluation<PowerHost, PowerVm> {
    @Override
    public boolean isHostSuitable(PowerHost host, PowerVm vm) {
        return isHostBwSuitable(host, vm) && isHostRamSuitable(host, vm) && isHostMipsSuitable(host, vm);
    }

    public boolean isHostBwSuitable(PowerHost host, PowerVm vm) {
        return host.getBwProvisioner().isSuitableForVm(vm, vm.getBw());
    }

    public boolean isHostRamSuitable(PowerHost host, PowerVm vm) {
        return host.getRamProvisioner().isSuitableForVm(vm, vm.getRam());
    }

    public abstract boolean isHostMipsSuitable(PowerHost host, PowerVm vm);
}
