package org.cloudbus.cloudsim.power;

import org.apache.commons.math3.util.Precision;

public class PowerHostSuitabilityEvaluationOversubscription extends PowerHostSuitabilityEvaluationAbstract {
    @Override
    public boolean isHostMipsSuitable(PowerHost host, PowerVm vm) {
        return Precision.compareTo(host.getAvailableMips(), vm.getCurrentRequestedTotalMips(), Precision.EPSILON) > 0;
    }
}
