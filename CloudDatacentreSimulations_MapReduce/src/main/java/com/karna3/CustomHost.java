package com.karna3;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;
//Extending the Host class, adding diskspeed here.
public class CustomHost extends Host {

    private Double diskSpeed = 0.0;

    public CustomHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
    }

    public Double getDiskSpeed() {
        return diskSpeed;
    }

    public void setDiskSpeed(Double diskSpeed) {
        this.diskSpeed = diskSpeed;
    }
}
