/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
public class DelAgent implements DeliberativeBehavior {
    
    enum Algorithm {
        BFS, ASTAR
    }
    
    /* Environment */
    Topology topology;
    TaskDistribution td;
    
    /* the properties of the agent */
    Agent agent;
    int capacity;
    int costPerKm;
    
    /* the planning class */
    Algorithm algorithm;
    
    TaskSet carriedTasks = TaskSet.create(new Task[0]); //start off empty
    
    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;
        
        // initialize the planner
        capacity = agent.vehicles().get(0).capacity();
        costPerKm = agent.vehicles().get(0).costPerKm();
        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
        
        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
        
        // ...
    }
    
    @Override
    public Plan plan(Vehicle vehicle, TaskSet tasks) {
        Plan plan;
        
        DelState state = new DelState(tasks, vehicle.getCurrentTasks(), vehicle.getCurrentCity(), vehicle.capacity());
        
        // Compute the plan with the selected algorithm.
        switch (algorithm) {
            case ASTAR:
                // ...
                plan = naivePlan(vehicle, tasks);
                break;
            case BFS:
                plan = BFSPlan(state);
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        return plan;
    }
    
    private Plan BFSPlan(DelState state) {        
        Queue<DelState> queue = new LinkedList<>();
        queue.add(state);
        ArrayList<DelState> checked = new ArrayList<>();
        
        do {
            DelState n = queue.poll();
            if (n.nextStates(costPerKm).isEmpty()) {
                System.out.println(n.getActions().size());
                return new Plan(state.getLocation(), n.getActions());}
            if (!checked.contains(n)) {
                checked.add(n);
                queue.addAll(n.nextStates(costPerKm));
            }
        } while (!queue.isEmpty());
        throw new IllegalStateException("Could not compute any path");
    }
    
    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);
        
        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity))
                plan.appendMove(city);
            
            plan.appendPickup(task);
            
            // move: pickup location => delivery location
            for (City city : task.path())
                plan.appendMove(city);
            
            plan.appendDelivery(task);
            
            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
    
    @Override
    public void planCancelled(TaskSet carriedTasks) {
        //handling carriedTasks is not needed since we can call vehicle.getCurrentTasks()
    }
}
