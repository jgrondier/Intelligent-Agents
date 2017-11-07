package template;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSP {

    private HashMap<Vehicle, List<CentralizedAction>> actions;
    List<Vehicle> vehiclesList;

    private HashMap<CentralizedAction, Vehicle> vehicle = new HashMap<>();
    private HashMap<CentralizedAction, Integer> time = new HashMap<>();
    private List<Plan> plans;

    public CSP(HashMap<Vehicle, List<CentralizedAction>> actions, List<Vehicle> vehiclesList) {

        this.actions = actions;
        this.vehiclesList = vehiclesList;

        for (Vehicle v : vehiclesList) {
            List<CentralizedAction> aL = actions.get(v);
            for (int i = 0; i < aL.size(); i++) {
                CentralizedAction a = aL.get(i);
                vehicle.put(a, v);
                time.put(a, i + 1);
            }
        }

        this.plans = buildPlans();

    }

    CentralizedAction nextTask(Vehicle v) {

        List<CentralizedAction> aL = actions.get(v);

        if (aL.isEmpty())
            return null;

        return aL.get(0);
    }

    CentralizedAction nextTask(CentralizedAction a) {

        Vehicle v = vehicle.get(a);

        List<CentralizedAction> aL = actions.get(v);

        int indexOfAction = aL.indexOf(a);

        if (indexOfAction < 0 || indexOfAction + 1 >= aL.size())
            return null;

        return aL.get(indexOfAction + 1);

    }

    double dist(CentralizedAction a1, CentralizedAction a2) {

        if (a2 == null || a1 == null)
            return 0.0;

        if (a1 instanceof CentralizedDeliveryAction && a2 instanceof CentralizedDeliveryAction)
            return a1.task.deliveryCity.distanceTo(a2.task.deliveryCity);

        if (a1 instanceof CentralizedDeliveryAction && a2 instanceof CentralizedPickupAction)
            return a1.task.deliveryCity.distanceTo(a2.task.pickupCity);

        if (a1 instanceof CentralizedPickupAction && a2 instanceof CentralizedDeliveryAction)
            return a1.task.pickupCity.distanceTo(a2.task.deliveryCity);

        if (a1 instanceof CentralizedPickupAction && a2 instanceof CentralizedPickupAction)
            return a1.task.pickupCity.distanceTo(a2.task.pickupCity);

        return Double.MAX_VALUE;
    }

    double dist(Vehicle v, CentralizedAction a) {

        if (a == null)
            return 0.0;

        return v.getCurrentCity().distanceTo(a.task.pickupCity);
    }

    double length(CentralizedAction a) {

        if (a == null)
            return 0.0;

        return a.task.pickupCity.distanceTo(a.task.deliveryCity);

    }

    double totalCompanyCost() {

        double C = 0.0;

        for (Vehicle v : vehiclesList) {
            C += (dist(v, nextTask(v)) + length(nextTask(v))) * (double) v.costPerKm();
            for (CentralizedAction a : actions.get(v)) {
                C += (dist(a, nextTask(a)) + length(nextTask(a))) * (double) vehicle.get(a).costPerKm();
            }
        }
        return C;
    }

    public CSP changingVehicle(Vehicle vi, Vehicle vj) {
        HashMap<Vehicle, List<CentralizedAction>> actions = new HashMap<>();
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
        HashMap<Vehicle, List<CentralizedAction>> actions = new HashMap<>();
        List<Vehicle> vehiclesList = new ArrayList<>();
        for (Vehicle vehicle : this.vehiclesList) {
            vehiclesList.add(vehicle);
        }
        for (Vehicle vehicle : this.actions.keySet()) {
            actions.put(vehicle, new ArrayList<>(this.actions.get(vehicle)));
        }

        List<CentralizedAction> viActions = actions.get(vi); // reference to the arraylist at vi

        CentralizedAction a1 = viActions.get(tIdx1);
        CentralizedAction a2 = viActions.get(tIdx2);

        // check we don't invert pickup/delivery
        for (int i = tIdx1 + 1; i < tIdx2; i++)
            if (viActions.get(i).task.equals(a1.task) || viActions.get(i).task.equals(a2.task))
                return null;

        viActions.set(tIdx2, a1);
        viActions.set(tIdx1, a2);

        // Check weight constraint is kept
        int weight = 0;
        for (CentralizedAction action : viActions) {
            if (action instanceof CentralizedPickupAction) {
                weight += action.task.weight;
            } else if (action instanceof CentralizedDeliveryAction) {
                weight -= action.task.weight;
            }

            if (weight > vi.capacity()) {
                return null;
            }
        }

        return new CSP(actions, vehiclesList);
    }

    private List<CentralizedAction> removeTask(List<CentralizedAction> actionList, Task t) {
        ArrayList<CentralizedAction> ret = new ArrayList<>();

        for (int i = 0; i < actionList.size(); i++) {
            CentralizedAction action = actionList.get(i);
            if (action.task.equals(t)) {
                ret.add(action);
            }
        }

        actionList.removeAll(ret);

        return ret;
    }


    private List<Plan> buildPlans() {

        ArrayList<Plan> planList = new ArrayList<>();

        for (Vehicle v : vehiclesList) {

            ArrayList<Action> tmp = new ArrayList<>();

            for (CentralizedAction a : actions.get(v)) {
                tmp.add(a.toLogistAction());
            }

            planList.add(new Plan(v.homeCity(), tmp));
        }


        return planList;
    }

    List<Plan> toPlan() {
        return plans;
    }


}
