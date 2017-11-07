package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class PickupAction extends Action {


    public PickupAction(Task t) {
        super(t);
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
