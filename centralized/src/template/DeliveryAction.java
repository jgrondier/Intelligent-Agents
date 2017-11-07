package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class DeliveryAction extends Action {


    public DeliveryAction(Task t) {
        super(t);
    }


    @Override
    double dist(Vehicle v) {
        return v.getCurrentCity().distanceTo(task.deliveryCity);

    }

    @Override
    logist.plan.Action toLogistAction() {
        return new logist.plan.Action.Delivery(task);
    }
}
