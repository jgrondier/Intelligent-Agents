package template;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

public class CentralizedPickupAction extends CentralizedAction {


    public CentralizedPickupAction(Task t) {
        super(t);
        this.type = Type.PickUp;
    }


    @Override
    Topology.City getDestinationCity() {
        return task.pickupCity;
    }

    @Override
    logist.plan.Action toLogistAction() {
        return new logist.plan.Action.Pickup(task);
    }

    @Override
    double dist(Vehicle v) {
        return v.getCurrentCity().distanceTo(task.pickupCity);

    }
}
