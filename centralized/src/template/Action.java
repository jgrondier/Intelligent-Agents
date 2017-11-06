package template;

import logist.task.Task;

public abstract class Action {

    Task task;

    public Action(Task t) {
        this.task = t;
    }
}
