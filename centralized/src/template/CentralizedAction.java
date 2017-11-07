package template;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

public abstract class CentralizedAction {

    Task task;
    Type type;
    CentralizedAction twin;

    public CentralizedAction(Task t) {
        this.task = t;
    }

    abstract double dist(Vehicle v);

    abstract Topology.City getDestinationCity();

    abstract logist.plan.Action toLogistAction();

    CentralizedAction getTwin() {
        return twin;
    }

    void setTwin(CentralizedAction twin) {
        this.twin = twin;
    }

}

enum  Type {
    PickUp, Delivery;
}
