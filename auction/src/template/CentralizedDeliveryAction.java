package template;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

public class CentralizedDeliveryAction extends CentralizedAction {


    public CentralizedDeliveryAction(Task t) {
        super(t);
        this.type = Type.Delivery;
    }

    public CentralizedDeliveryAction(CentralizedAction a, Task t) {
        this(t);
        twin = a;
    }


    @Override
    double dist(Vehicle v) {
        return v.getCurrentCity().distanceTo(task.deliveryCity);

    }

    @Override
    logist.plan.Action toLogistAction() {
        return new logist.plan.Action.Delivery(task);
    }

    @Override
    Topology.City getDestinationCity() {
        return task.deliveryCity;
    }
}
