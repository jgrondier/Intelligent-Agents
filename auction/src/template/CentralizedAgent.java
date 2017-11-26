package template;

//the list of imports

import logist.LogistPlatform;
import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.*;

@SuppressWarnings("unused")
public class CentralizedAgent implements AuctionBehavior {

    private static final int ITERATIONS_MAX = 50000; //max SLS iterations
    private static final float CHOICE_PROBABILITY = 0.4f; //for localChoice
    private static final float EPSILON = 0.01f; //cost comparison
    private Set<Task> wonTasks;
    private Agent agent;
    private CSP currentPlans;
    private CSP winPlans;

    private long currentCost = 0;
    private long winCost = 0;
    private long timeout_setup;
    private long timeout_plan;
    private long timeout_bid;

    public static final Random rand = new Random(42);


    @Override
    public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

        timeout_plan = LogistPlatform.getSettings().get(LogistSettings.TimeoutKey.PLAN);
        timeout_bid = LogistPlatform.getSettings().get(LogistSettings.TimeoutKey.BID);

        System.out.println("Timeout for plan phase: " + timeout_plan);
        System.out.println("Timeout for bid phase: " + timeout_bid);

        wonTasks = new HashSet<>();

        this.agent = agent;

    }

    @Override
    public Long askPrice(Task task) {
        Set<Task> tasks = new HashSet<>(wonTasks);
        tasks.add(task);
        winPlans = planAsSet(agent.vehicles(), tasks, timeout_bid);

        winCost = (long) winPlans.totalCompanyCost();
        System.out.println("New Task would cost us " + (winCost - currentCost));

        return Math.max(0, winCost - currentCost) + 1;
    }
    
    @Override
    public void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers) {
        if (lastWinner == this.agent.id()) {
            wonTasks.add(lastTask);
            currentCost = winCost;
            currentPlans = new CSP(winPlans.actions, winPlans.vehiclesList);
            System.out.println("New task won, new cost is " + currentCost);
        }
    }

    private CSP selectInitialSolution(List<Vehicle> vehicles, Set<Task> tasks) {
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
            actions.put(vehicleList.get(i), new ArrayList<>());
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
        CSP tmp = planAsSet(vehicles, tasks, timeout_plan);
        return tmp.toPlan(vehicles);
    }

    private <A> boolean compareSet(Set<A> a, Set<A> b) {
        if (a.size() != b.size()) //not needed, but faster if a is larger than b
            return false;
        return a.containsAll(b) && b.containsAll(a);
    }

    public CSP planAsSet(List<Vehicle> vehicles, Set<Task> tasks, long timeout) {
        CSP csp = selectInitialSolution(vehicles, tasks);

        long start = System.currentTimeMillis();

        CSP bestCSP = csp;

        for (int i = 0; i < ITERATIONS_MAX && System.currentTimeMillis() - start < 0.95 * timeout; i++) {
            CSP old = csp;
            List<CSP> neighbours = chooseNeighbours(old);
            csp = localChoice(neighbours, old);

            if (csp.totalCompanyCost() < bestCSP.totalCompanyCost()) {
                bestCSP = csp;
                System.out.println("New minimum found: " + bestCSP.totalCompanyCost());
            }

        }

        System.out.println("Final cost of best solution: " + bestCSP.totalCompanyCost());


        return bestCSP;
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
