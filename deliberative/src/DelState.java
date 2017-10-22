import logist.plan.Action;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.ArrayList;
import java.util.List;

public class DelState {

    private TaskSet worldTasks;
    private TaskSet pickedTasks;
    private Topology.City location;
    private double totalCost = 0;
    private ArrayList<Action> actions = new ArrayList<>();

    private int capacity;

    public DelState(TaskSet worldTasks, TaskSet carriedTasks, Topology.City location, int capacity) {
        this.worldTasks = worldTasks;
        this.pickedTasks = carriedTasks;
        this.location = location;
        this.capacity = capacity;
    }

    public List<DelState> nextStates(int costPerKm) {

        ArrayList<DelState> l = new ArrayList<>();

        if (worldTasks.isEmpty() && pickedTasks.isEmpty()) {
            return l;
        }

        // Next deliveries states
        for (Task t : pickedTasks) {
            DelState tmp = new DelState(this);
            tmp.pickedTasks.remove(t);
            tmp.setLocation(t.deliveryCity);

            tmp.addCost(costPerKm * location.distanceTo(t.deliveryCity));

            for (City c : location.pathTo(t.deliveryCity)) {
                tmp.addAction(new Action.Move(c));
            }

            tmp.addAction(new Action.Delivery(t));

            tmp.setCapacity(capacity + t.weight);

            l.add(tmp);
        }

        // Next pickup tasks
        for (Task t : worldTasks) {
            if (capacity >= t.weight) {

                DelState tmp = new DelState(this);

                tmp.worldTasks.remove(t);
                tmp.pickedTasks.add(t);
                tmp.setLocation(t.pickupCity);

                tmp.addCost(costPerKm * location.distanceTo(t.pickupCity));

                for (City c : location.pathTo(t.pickupCity)) {
                    tmp.addAction(new Action.Move(c));
                }

                tmp.addAction(new Action.Pickup(t));

                tmp.setCapacity(capacity - t.weight);

                l.add(tmp);
            }

        }

        return l;
    }

    public DelState(DelState d) {
        this(d.getWorldTasks().clone(), d.getPickedTasks().clone(), d.getLocation(), d.getCapacity());
        totalCost = d.getTotalCost();
        actions = d.getActions();
    }

    public double getTotalCost() {
        return totalCost;
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

    public void addCost(double d) {
        this.totalCost += d;
    }

    public void addAction(Action a) {
        actions.add(a);
    }

    public ArrayList<Action> getActions() {
        return new ArrayList<>(actions);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        DelState o = (DelState) obj;
        boolean eq = true;
        eq = eq && this.worldTasks.equals(o.getWorldTasks());
        eq = eq && this.pickedTasks.equals(o.getPickedTasks());
        eq = eq && this.location.equals(o.getLocation());
        return eq;
    }


}