import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.List;

public class DelState {

    TaskSet worldTasks;
    TaskSet pickedTasks;
    Topology.City location;

    int capacity;

    public DelState(TaskSet t, TaskSet t_, Topology.City location, int capacity) {
        this.worldTasks = t;
        this.pickedTasks = t_;
        this.location = location;
        this.capacity = capacity;
    }

    public List<DelState> nextStates() {

        ArrayList<DelState> l = new ArrayList<>();

        if (worldTasks.isEmpty() && pickedTasks.isEmpty()) {
            return l;
        }


        //Next deliveries states
        for (Task t : pickedTasks) {
            DelState tmp = new DelState(this);
            tmp.pickedTasks.remove(t);

            capacity += t.weight;

            l.add(tmp);
        }

        //Next pickup tasks
        for (Task t : worldTasks) {

            DelState tmp = new DelState(this);

            if (capacity >= t.weight) {
                tmp.worldTasks.remove(t);
                tmp.pickedTasks.add(t);

                capacity -= t.weight;

                l.add(tmp);
            }


        }


        return l;
    }

    public DelState(DelState d) {
        this(d.getWorldTasks().clone(), d.getPickedTasks().clone(), d.getLocation(), d.getCapacity());
    }

    public TaskSet getWorldTasks() {
        return worldTasks;
    }

    public void setWorldTasks(TaskSet worldTasks) {
        this.worldTasks = worldTasks;
    }

    public TaskSet getPickedTasks() {
        return pickedTasks;
    }

    public void setPickedTasks(TaskSet pickedTasks) {
        this.pickedTasks = pickedTasks;
    }

    public Topology.City getLocation() {
        return location;
    }

    public void setLocation(Topology.City location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}

