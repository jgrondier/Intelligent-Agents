package template;


import logist.simulation.Vehicle;
import logist.task.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSP {

    private HashMap<Vehicle, List<Action>> actions;
    private List<Vehicle> vehiclesList;

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

        if (aL.size() < 1)
            return null;

        return aL.get(0);
    }

    Action nextTask(Action a) {

        Vehicle v = vehicle.get(a);

        List<Action> aL = actions.get(v);

        int indexOfAction = aL.indexOf(a);

        if (indexOfAction >= aL.size())
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

}


