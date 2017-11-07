package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public abstract class CentralizedAction {

    Task task;

    public CentralizedAction(Task t) {
        this.task = t;
    }

    abstract double dist(Vehicle v);

    abstract logist.plan.Action toLogistAction();
}
