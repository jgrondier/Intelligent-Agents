package template;

import logist.simulation.Vehicle;
import logist.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSP {

    private HashMap<Vehicle, List<Action>> actions;
    List<Vehicle> vehiclesList;

    private HashMap<Action, Vehicle> vehicle = new HashMap<>();
    private HashMap<Action, Integer> time = new HashMap<>();

    public CSP(HashMap<Vehicle, List<Action>> actions, List<Vehicle> vehiclesList) {

        this.actions = actions;
        this.vehiclesList = vehiclesList;

        for (Vehicle v : vehiclesList) {
            List<Action> aL = actions.get(v);
            for (int i = 0; i < aL.size(); i++) {
                Action a = aL.get(i);
                vehicle.put(a, v);
                time.put(a, i + 1);
            }
        }
    }

    Action nextTask(Vehicle v) {

        List<Action> aL = actions.get(v);

        if (aL.isEmpty())
            return null;

        return aL.get(0);
    }

    Action nextTask(Action a) {

        Vehicle v = vehicle.get(a);

        List<Action> aL = actions.get(v);

        int indexOfAction = aL.indexOf(a);

        if (indexOfAction < 0 || indexOfAction + 1 >= aL.size())
            return null;

        return aL.get(indexOfAction + 1);

    }

    double dist(Action a1, Action a2) {

        if (a2 == null)
            return 0.0;

        return a1.task.deliveryCity.distanceTo(a2.task.pickupCity);
    }

    double dist(Vehicle v, Action a) {

        if (a == null)
            return 0.0;

        return v.getCurrentCity().distanceTo(a.task.pickupCity);
    }

    double length(Action a) {

        if (a == null)
            return 0.0;

        return a.task.pickupCity.distanceTo(a.task.deliveryCity);

    }

    double TotalCompanyCost() {

        double C = 0.0;

        for (Vehicle v : vehiclesList) {
            C += (dist(v, nextTask(v)) + length(nextTask(v))) * (double) v.costPerKm();
            for (Action a : actions.get(v)) {
                C += (dist(a, nextTask(a)) + length(nextTask(a))) * (double) vehicle.get(a).costPerKm();
            }
        }
        return C;
    }

    public CSP changingVehicle(Vehicle vi, Vehicle vj) {
        HashMap<Vehicle, List<Action>> actions = new HashMap<>();
        List<Vehicle> vehiclesList = new ArrayList<>();
        for (Vehicle vehicle : this.vehiclesList) {
            vehiclesList.add(vehicle);
        }
        for (Vehicle vehicle : this.actions.keySet()) {
            actions.put(vehicle, new ArrayList<>(this.actions.get(vehicle)));
        }
        Task t = actions.get(vi).get(0).task;
        actions.get(vj).addAll(0, removeTask(actions.get(vi), t));
        return new CSP(actions, vehiclesList);
    }

    public CSP changingTaskOrder(Vehicle vi, int tIdx1, int tIdx2) {
        HashMap<Vehicle, List<Action>> actions = new HashMap<>();
        List<Vehicle> vehiclesList = new ArrayList<>();
        for (Vehicle vehicle : this.vehiclesList) {
            vehiclesList.add(vehicle);
        }
        for (Vehicle vehicle : this.actions.keySet()) {
            actions.put(vehicle, new ArrayList<>(this.actions.get(vehicle)));
        }
        
        List<Action> viActions = actions.get(vi); //reference to the arraylist at vi

        Action a1 = viActions.get(tIdx1);
        Action a2 = viActions.get(tIdx2);
        
        //check we don't invert pickup/delivery
        for (int i = tIdx1 + 1; i < tIdx2; i++)
            if (viActions.get(i).task.equals(a1.task) || viActions.get(i).task.equals(a2.task))
                return null;

        viActions.set(tIdx2, a1);
        viActions.set(tIdx1, a2);
        
        // Check weight constraint is kept
        int weight = 0;
        for (Action action : viActions) {
            if (action instanceof PickupAction) {
                weight += action.task.weight;
            } else if (action instanceof DeliveryAction) {
                weight -= action.task.weight;
            }

            if (weight > vi.capacity()) {
                return null;
            }
        }
        
        return new CSP(actions, vehiclesList);
    }

    private List<Action> removeTask(List<Action> actionList, Task t) {
        ArrayList<Action> ret = new ArrayList<>();
        for (Action action : actionList) {
            if (action.task.equals(t)) {
                actionList.remove(action);
                ret.add(action);
            }
        }
        return ret;
    }
}
