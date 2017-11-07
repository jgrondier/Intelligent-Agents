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

    private static final int ITERATIONS_MAX = 100000; //max SLS iterations
    private static final float CHOICE_PROBABILITY = 0.4f; //for localChoice
    private static final float EPSILON = 0.01f; //cost comparison
    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;

    @Override
    public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_default.xml");
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

    private CSP selectInitialSolution(List<Vehicle> vehicles, TaskSet tasks) {
        List<Vehicle> vehicleList = new ArrayList<>(vehicles);
        vehicleList.sort(Comparator.comparingInt(Vehicle::capacity).reversed());
        ArrayList<CentralizedAction> actionsList = new ArrayList<>();
        HashMap<Vehicle, List<CentralizedAction>> actions = new HashMap<>();
        for (Task t : tasks) {
            actionsList.add(new CentralizedPickupAction(t));
            actionsList.add(new CentralizedDeliveryAction(t));
        }
        actions.put(vehicleList.get(0), actionsList);
        for (int i = 1; i < vehicleList.size(); i++) {
            actions.put(vehicleList.get(i), new ArrayList<CentralizedAction>());
        }
        return new CSP(actions, vehicleList);
    }

    private List<CSP> chooseNeighbours(CSP old) {
        List<CSP> neighbours = new ArrayList<>();
        Vehicle vi;
        do {
            vi = old.vehiclesList.get(new Random().nextInt(old.vehiclesList.size()));
        } while (old.nextTask(vi) == null);
        CentralizedAction t = old.nextTask(vi);


        // Change Vehicle
        for (Vehicle vj : old.vehiclesList) {
            if (vi == vj)
                continue;

            if (t.task != null && t.task.weight <= vj.capacity()) {
                neighbours.add(old.changingVehicle(vi, vj));
            }
        }

        // Change Task Order
        int i = 0;
        do {
            t = old.nextTask(t);
            i++;
        } while (t != null);

        if (i > 1) {
            for (int tIdx1 = 1; tIdx1 < i - 1; tIdx1++) {
                for (int tIdx2 = tIdx1; tIdx2 < i; tIdx2++) {

                    CSP csp = old.changingTaskOrder(vi, tIdx1, tIdx2);
                    if (csp != null)
                        neighbours.add(csp);
                }
            }
        }

        return neighbours;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        CSP csp = selectInitialSolution(vehicles, tasks);
        
        long start = System.currentTimeMillis();
        int iterations = ITERATIONS_MAX;
        
        do {
            CSP old = csp;
            List<CSP> neighbours = chooseNeighbours(old);
            csp = localChoice(neighbours, old);
        } while (System.currentTimeMillis() - start > .02*timeout_plan && --iterations > 0);
                
        return csp.toPlan();
    }

    private CSP localChoice(List<CSP> neighbours, CSP old) {
        List<CSP> best = new ArrayList<>();
        double bestCost = Double.POSITIVE_INFINITY;
        
        for (CSP csp : neighbours) {
            double cost = csp.totalCompanyCost();
            if (Math.abs(bestCost-cost) > EPSILON) {
                best.add(csp);
            }
            else if (bestCost > cost) {
                bestCost = cost;
                best.clear();
                best.add(csp);
            }
        }
        
        Random r = new Random();
        return r.nextFloat() > CHOICE_PROBABILITY ? old : best.get(r.nextInt(best.size()));
    }

}
