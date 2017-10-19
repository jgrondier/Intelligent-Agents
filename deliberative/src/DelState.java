import logist.plan.Action;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.List;

public class DelState {
    
    TaskSet worldTasks;
    TaskSet pickedTasks;
    Topology.City location;
    double totalCost = 0;
    ArrayList<Action> actions = new ArrayList<>();
    
    int capacity;
    
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
            
            tmp.addAction(new Action.Delivery(t));
            
            tmp.setCapacity(capacity + t.weight);
            
            l.add(tmp);
        }
        
        // Next pickup tasks
        for (Task t : worldTasks) {
            
            DelState tmp = new DelState(this);
            
            if (capacity >= t.weight) {
                tmp.worldTasks.remove(t);
                tmp.pickedTasks.add(t);
                tmp.setLocation(t.pickupCity);
                
                tmp.addCost(costPerKm * location.distanceTo(t.pickupCity));
                
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
}