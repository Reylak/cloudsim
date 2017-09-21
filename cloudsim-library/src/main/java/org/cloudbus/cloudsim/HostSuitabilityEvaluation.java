package org.cloudbus.cloudsim;

public interface HostSuitabilityEvaluation<H extends Host, V extends Vm> {
    boolean isHostSuitable(H host, V vm);
}
