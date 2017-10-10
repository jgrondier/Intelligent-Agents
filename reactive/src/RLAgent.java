import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class RLAgent implements ReactiveBehavior {

    public static int ID = 0;


    private ArrayList<RLState> states;

    private HashMap<RLState, Action> optimalAction = new HashMap<>();

    //Legacy variables to make testing the setup work
    //TODO: not forget to remove these
    private Random random;
    private double pPickup;
    private int numActions;
    private Agent myAgent;


    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {

        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
        Double discount = agent.readProperty("discount-factor", Double.class,
                0.95);

        this.random = new Random();
        this.pPickup = discount;
        this.numActions = 0;
        this.myAgent = agent;

        //We generate the states list
        for (City c1 : topology.cities()) {
            //State of having no task in that city
            states.add(new RLState(c1));
            for (City c2 : topology.cities()) {
                //Can't deliver to the same city
                if (c2 != c1) {
                    states.add(new RLState(c1, c2));
                }
            }
        }


        //TODO: Continue working


    }

    @Override
    public Action act(Vehicle vehicle, Task availableTask) {
        Action action;

        if (availableTask == null || random.nextDouble() > pPickup) {
            City currentCity = vehicle.getCurrentCity();
            action = new Move(currentCity.randomNeighbor(random));
        } else {
            action = new Pickup(availableTask);
        }

        if (numActions >= 1) {
            System.out.println("The total profit after " + numActions + " actions is " + myAgent.getTotalProfit() + " (average profit: " + (myAgent.getTotalProfit() / (double) numActions) + ")");
        }
        numActions++;

        return action;
    }
}
