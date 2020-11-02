package com.karna3;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class StandardPolicyBroker extends DatacenterBroker {

    private Logger LOG = LoggerFactory.getLogger(CustomBroker.class);
    private ArrayList<CustomCloudlet> reducerArrayList = null;
    private int CountX = 0;
    private static final int MAP_RED_RATIO = 3;

    public StandardPolicyBroker(String name) throws Exception {
        super(name);
        reducerArrayList = new ArrayList<>();
    }

    public void submitMapperList(List<? extends CustomCloudlet> list) {
        super.submitCloudletList(list);
    }

    public void submitReducerList(List<? extends CustomCloudlet> list) {
        reducerArrayList.addAll(list);
    }

    public ArrayList<CustomCloudlet> getReducerArrayList() {
        return reducerArrayList;
    }
    private int vmIndex = 0;

    /**
     * submitCloudlets()
     * We set some delay for loading the  cloudlet
     */
    @Override
    protected void submitCloudlets() {
        List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();
        for (Cloudlet cloudlet : getCloudletList()) {

            Vm vm;
            if (cloudlet.getVmId() == -1) {
                vm = getVmsCreatedList().get(vmIndex);
            } else {
                vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
                if (vm == null) { 
                    if(!Log.isDisabled()) {
                        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing the cloudlet execution",
                                cloudlet.getCloudletId(), ": bount VM not available");
                    }
                    continue;
                }
            }

            if (!Log.isDisabled()) {
                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ":cloudlet being sent ",
                        cloudlet.getCloudletId(), " to VM #", vm.getId());
            }

            ((CustomCloudlet) cloudlet).setHost(vm.getHost());
            cloudlet.setVmId(vm.getId());
            double diskSpeed = 0;
            if (vm.getHost() instanceof CustomHost) {
                diskSpeed = ((CustomHost)vm.getHost()).getDiskSpeed();
            }

            if (cloudlet instanceof CustomCloudlet) {
                ((CustomCloudlet) cloudlet).setDelay(cloudlet.getCloudletFileSize()*diskSpeed);
            }
            send(getVmsToDatacentersMap().get(vm.getId()), cloudlet.getCloudletFileSize()*diskSpeed, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            cloudletsSubmitted++;
            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
            getCloudletSubmittedList().add(cloudlet);
            successfullySubmitted.add(cloudlet);
        }
        getCloudletList().removeAll(successfullySubmitted);
        // removing cloudlets after submition
    }

    /**
     * @param ev SimEvent ev
     *We work with Map/Reduce architecture - 3 maps give space for 1 reduce cloudlet
     */
    @Override
    public void processEvent(SimEvent ev) {

        switch (ev.getTag()) {
            case CloudSimTags.CLOUDLET_RETURN:

                Object obj = ev.getData();
                if (obj!=null && obj instanceof CustomCloudlet) {
                    CustomCloudlet current = (CustomCloudlet) ev.getData();
                    if (current.getType() == CustomCloudlet.Type.MAPPER) {
                        CountX++; 
                    }

                    //now submit reducers
                    if (CountX==MAP_RED_RATIO) {
                        LOG.debug("CountX limit approached. New reducers submitted");
                        if (reducerArrayList.size()>0) {
                            LOG.debug("Submitted cloudlet with ID: "+reducerArrayList.get(0).getCloudletId());
                            getCloudletList().add(reducerArrayList.get(0)); 
                            reducerArrayList.remove(0);
                        }

                        submitCloudlets();
                        CountX=0;
                    }

                    processCloudletReturn(ev);

                }

                break;
            default:
                super.processEvent(ev);
        }
    }
}
