package org.cloudbus.cloudsim.power;

import org.apache.commons.math3.util.Precision;
import org.cloudbus.cloudsim.Vm;

public class PowerHostSuitabilityEvaluationNoOversubscription extends PowerHostSuitabilityEvaluationAbstract {
    @Override
    public boolean isHostMipsSuitable(PowerHost host, PowerVm vm) {
        return Precision.compareTo(
                host.getTotalMips() - host.getVmList().stream().mapToDouble(Vm::getTotalMips).sum(),
                vm.getTotalMips(), Precision.EPSILON) > 0;
    }
}
