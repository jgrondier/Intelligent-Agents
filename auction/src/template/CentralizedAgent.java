package template;

//the list of imports

import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
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
public class CentralizedAgent implements CentralizedBehavior, AuctionBehavior {

    private static final int ITERATIONS_MAX = 5000; //max SLS iterations
    private static final float CHOICE_PROBABILITY = 0.4f; //for localChoice
    private static final float EPSILON = 0.01f; //cost comparison
    private Topology topology;
    private TaskSet wonTasks;
    private Agent agent;

    private long currentCost = 0;
    private long winCost = 0;
    private long timeout_setup;
    private long timeout_plan;

    public static final Random rand = new Random(42);


    @Override
    public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_auction.xml");
        } catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

        System.out.println("Timeout: "+timeout_plan);

        this.topology = topology;
        this.agent = agent;

    }

    @Override
    public Long askPrice(Task task) {
        TaskSet tasks = wonTasks.clone();
        tasks.add(task);
        List<Plan> plans = plan(agent.vehicles(), tasks);
        winCost = 0;
        for (int i = 0; i < plans.size(); i++) {
            winCost += plans.get(i).totalDistanceUnits() * agent.vehicles().get(i).costPerKm();
        }
        return Math.min(0, winCost - currentCost) + 1;
    }

    @Override
    public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
        if (lastWinner == this.agent.id()) {
            wonTasks.add(lastTask);
            currentCost = winCost;
            //TODO: something because we won
        } else {
            //TODO: something because we lost
        }

    }

    private CSP selectInitialSolution(List<Vehicle> vehicles, TaskSet tasks) {
        List<Vehicle> vehicleList = new ArrayList<>(vehicles);
        vehicleList.sort(Comparator.comparingInt(Vehicle::capacity).reversed());
        ArrayList<CentralizedAction> actionsList = new ArrayList<>();
        HashMap<Vehicle, List<CentralizedAction>> actions = new HashMap<>();
        for (Task t : tasks) {

            CentralizedPickupAction tmpP = new CentralizedPickupAction(t);
            CentralizedDeliveryAction tmpD = new CentralizedDeliveryAction(tmpP, t);
            tmpP.setTwin(tmpD);

            actionsList.add(tmpP);
            actionsList.add(tmpD);
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
            vi = old.vehiclesList.get(rand.nextInt(old.vehiclesList.size()));
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
        List<CSP> cspList = old.changingTaskOrder(vi);
        neighbours.addAll(cspList);

        return neighbours;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        CSP csp = selectInitialSolution(vehicles, tasks);

        long start = System.currentTimeMillis();

        CSP bestCSP = csp;

        for (int i = 0; i < ITERATIONS_MAX && System.currentTimeMillis() - start < 0.95 * timeout_plan; i++) {
            CSP old = csp;
            List<CSP> neighbours = chooseNeighbours(old);
            csp = localChoice(neighbours, old);

            if (csp.totalCompanyCost() < bestCSP.totalCompanyCost()) {
                bestCSP = csp;
                System.out.println("New minimum found: " + bestCSP.totalCompanyCost());
            }

        }

        System.out.println("Final cost of best solution: " + csp.totalCompanyCost());


        return csp.toPlan(vehicles);
    }

    private CSP localChoice(List<CSP> neighbours, CSP old) {
        List<CSP> best = new ArrayList<>();
        double bestCost = Double.POSITIVE_INFINITY;

        for (CSP csp : neighbours) {
            double cost = csp.totalCompanyCost();
            if (Math.abs(bestCost - cost) < EPSILON) {
                best.add(csp);
            } else if (bestCost > cost) {
                bestCost = cost;
                best.clear();
                best.add(csp);
            }
        }

        return rand.nextFloat() > CHOICE_PROBABILITY ? old : best.get(rand.nextInt(best.size()));
    }

}
