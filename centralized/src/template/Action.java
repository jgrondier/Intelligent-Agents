package template;

import logist.simulation.Vehicle;
import logist.task.Task;

public abstract class Action {

    Task task;

    public Action(Task t) {
        this.task = t;
    }

    abstract double dist(Vehicle v);

    abstract logist.plan.Action toLogistAction();
}
