package template;

//the list of imports

import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.CentralizedBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class CentralizedAgent implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;

    @Override
    public void setup(Topology topology, TaskDistribution distribution,
                      Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config\\settings_default.xml");
        } catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;


    }

    public CSP SelectInitialSolution() {
        List<Vehicle> vehicleList = agent.vehicles();
        vehicleList.sort(Comparator.comparingInt(Vehicle::capacity).reversed());
        ArrayList<Action> actionsList = new ArrayList<>();
        HashMap<Vehicle, List<Action>> actions = new HashMap<>();
        for (Task t : agent.getTasks()) {
            actionsList.add(new PickupAction(t));
            actionsList.add(new DeliveryAction(t));
        }
        actions.put(vehicleList.get(0), actionsList);
        for (int i = 1; i < vehicleList.size(); i++) {
            actions.put(vehicleList.get(i), new ArrayList<Action>());
        }
        return new CSP(actions, vehicleList);
    }
    
    public List<CSP> chooseNeighbours(CSP old) {
    	List<CSP> neighbours = new ArrayList<>();
    	Vehicle vi;
    	do {
    		vi = old.vehiclesList.get(new Random().nextInt(old.vehiclesList.size()));
    	} while (old.nextTask(vi)!=null);
		Action t = old.nextTask(vi);
    	
		//Change Vehicle
    	for (Vehicle vj : old.vehiclesList) {
			if (vi == vj)
				continue;
			if (t.task.weight <= vj.capacity()) {
				neighbours.add(old.changingVehicle(vi, vj));
			}
		}
    	
    	//Change Task Order
    	int i = 0;
    	do {
    		t = old.nextTask(t);
    		i++;
    	} while (t!=null);
    	
    	if (i > 1) {
    		for (int tIdx1 = 1; tIdx1 < i; tIdx1++) {
				for (int tIdx2 = tIdx1; tIdx2 <= i; tIdx2++) {
					neighbours.add(old.changingTaskOrder(vi, tIdx1, tIdx2));
				}
			}
    	}
    	
    	return neighbours;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        return null;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        return null;
    }


}
