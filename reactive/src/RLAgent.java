import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    
    private ArrayList<RLState> states = new ArrayList<RLState>();
    private HashMap<RLState, Tuple<RLAction, Double>> optimalAction = new HashMap<RLState, Tuple<RLAction, Double>>();
    private HashMap<City, ArrayList<RLState>> statesPerCity = new HashMap<City, ArrayList<RLState>>();
    
    // Legacy variables to make testing the setup work
    // TODO: not forget to remove these
    private Random random;
    private double pPickup;
    private int numActions;
    private Agent myAgent;
    
    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        
        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
        Double discount = agent.readProperty("discount-factor", Double.class, 0.95);
        
        this.random = new Random();
        this.pPickup = discount;
        this.numActions = 0;
        this.myAgent = agent;
        
        double costPerKm = agent.vehicles().get(0).costPerKm();
        
        // We generate the states list
        for (City c1 : topology.cities()) {
            ArrayList<RLState> cityStates = new ArrayList<RLState>();
            // State of having no task in that city
            cityStates.add(new RLState(c1));
            for (City c2 : topology.cities()) {
                // Can't have a delivery to the same city
                if (c2 != c1) {
                    cityStates.add(new RLState(c1, c2));
                }
            }
            states.addAll(cityStates);
            statesPerCity.put(c1, cityStates);
        }
        
        HashMap<RLState, List<Tuple<RLAction, Double>>> possibleActions = new HashMap<RLState, List<Tuple<RLAction, Double>>>();
        
        // We build a hashmap of the possibles actions of each state
        for (RLState state : states) {
            
            City currentCity = state.getCurrentCity();
            
            ArrayList<Tuple<RLAction, Double>> tmp = new ArrayList<Tuple<RLAction, Double>>();
            
            if (state.hasTask()) {
                tmp.add(new Tuple<RLAction, Double>(new RLPickup(state.getDestinationCity()),
                        (double) td.reward(currentCity, state.getDestinationCity())
                                - costPerKm * currentCity.distanceTo(state.getDestinationCity())));
            }
            
            for (City c : currentCity.neighbors()) {
                tmp.add(new Tuple<RLAction, Double>(new RLMove(c), -costPerKm * currentCity.distanceTo(c)));
            }
            
            possibleActions.put(state, tmp);
        }
        
        // Initialize optimal action arbitrarily
        for (RLState s : states) {
            optimalAction.put(s, possibleActions.get(s).get(0));
        }
        
        boolean hasChanged = false;
        do {
            for (RLState rlState : states) {
                ArrayList<Tuple<RLAction, Double>> q = new ArrayList<Tuple<RLAction, Double>>();
                for (Tuple<RLAction, Double> tuple : possibleActions.get(rlState)) {
                    double sum = 0;
                    // Only valid future states are the ones inside the city we'll be at after that
                    // action
                    for (RLState rlState2 : statesPerCity.get(tuple._1.getDestination())) {
                        // "no task" states have null as destination city, so td.probability works here
                        sum += optimalAction.get(rlState2)._2
                                * td.probability(rlState2.getCurrentCity(), rlState2.getDestinationCity());
                    }
                    q.add(new Tuple<RLAction, Double>(tuple._1, tuple._2 + discount * sum));
                }
                
                Tuple<RLAction, Double> max = Collections.max(q, new Comparator<Tuple<RLAction, Double>>() {
                    @Override
                    public int compare(Tuple<RLAction, Double> o1, Tuple<RLAction, Double> o2) {
                        return Double.compare(o1._2, o2._2);
                    }
                });
                
                if (max.equals(optimalAction.get(rlState)))
                    hasChanged = true;
                else
                    optimalAction.put(rlState, max);
                
            }
        } while (!hasChanged);
    }
    
    @Override
    public Action act(Vehicle vehicle, Task availableTask) {
        if (numActions >= 1) {
            System.out.println("The total profit after " + numActions + " actions is " + myAgent.getTotalProfit()
                    + " (average profit: " + (myAgent.getTotalProfit() / (double) numActions) + ")");
        }
        numActions++;
        
        RLState s;
        if (availableTask == null)
            s = new RLState(vehicle.getCurrentCity());
        else
            s = new RLState(vehicle.getCurrentCity(), availableTask.deliveryCity);
        
        RLAction bestAction = optimalAction.get(s)._1;
        
        switch (bestAction.getType()) {
            case MOVE:
                return new Move(bestAction.getDestination());
            case PICKUP:
                return new Pickup(availableTask);
            case NONE:
            default:
                throw new IllegalStateException("Undefined best action for state " + s);
        }
    }
}

class Tuple<X, Y> {
    public final X _1;
    public final Y _2;
    
    public Tuple(X _1, Y _2) {
        this._1 = _1;
        this._2 = _2;
    }
    
    @Override
    public String toString() {
        return "(" + _1.toString() + ", " + _2.toString() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass() || ((Tuple<?, ?>) obj)._1.getClass() != _1.getClass()
                || ((Tuple<?, ?>) obj)._2.getClass() != _2.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked") // is actually checked just above
        Tuple<X, Y> o = (Tuple<X, Y>) obj;
        return this._1.equals((o._1)) && this._2.equals((o._2));
    }
}
