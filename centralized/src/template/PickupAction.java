package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public class PickupAction extends Action {


    public PickupAction(Task t) {
        super(t);
    }


    @Override
    double dist(Vehicle v) {
        return v.getCurrentCity().distanceTo(task.pickupCity);

    }
}
