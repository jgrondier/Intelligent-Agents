package template;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

import java.util.*;

public class CSP {

    final HashMap<Vehicle, List<CentralizedAction>> actions;
    final List<Vehicle> vehiclesList;

    private final static Random rnd = CentralizedAgent.rand;

    private HashMap<CentralizedAction, Vehicle> vehicle = new HashMap<>();
    private HashMap<CentralizedAction, Integer> time = new HashMap<>();
    private List<Plan> plans;

    private double cost = 0.0;

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


    public double totalCompanyCost() {


        this.toPlan(this.vehiclesList);

        return this.cost;

    }

    public CSP changingVehicle(Vehicle vi, Vehicle vj) {


        if (vi.id() == vj.id()) {
            return this;
        }

        HashMap<Vehicle, List<CentralizedAction>> actions = new HashMap<>();
        List<Vehicle> vehiclesList = new ArrayList<>(this.vehiclesList);


        for (Vehicle vehicle : this.actions.keySet()) {
            actions.put(vehicle, new ArrayList<>(this.actions.get(vehicle)));
        }

        if (actions.get(vi).size() == 0) {
            return this;
        }
        CentralizedAction t = actions.get(vi).get(CentralizedAgent.rand.nextInt(actions.get(vi).size()));

        if (vj.capacity() >= t.task.weight) {

            List<CentralizedAction> ret = removeTask(actions.get(vi), t);


            if (actions.get(vj).size() == 0) {
                actions.get(vj).addAll(0, ret);
            } else {
                actions.get(vj).addAll(CentralizedAgent.rand.nextInt(actions.get(vj).size()), ret);
            }

            actions.get(vi).removeAll(ret);
            return new CSP(actions, vehiclesList);
        }

        return this;

    }

    public List<CSP> changingTaskOrder(Vehicle vi) {

        ArrayList<CSP> cspList = new ArrayList<>();

        List<CentralizedAction> aL = this.actions.get(vi);

        for (int i = 0; i < aL.size() - 1; i++) {


            inner:
            for (int j = i + 1; j < aL.size(); j++) {


                CentralizedAction ai = aL.get(i);
                CentralizedAction aj = aL.get(j);


                List<CentralizedAction> aLCopy = new ArrayList<>(aL);


                HashMap<Vehicle, List<CentralizedAction>> _actions = new HashMap<>();
                for (Vehicle vehicle : this.actions.keySet()) {
                    _actions.put(vehicle, new ArrayList<>(this.actions.get(vehicle)));
                }

                Collections.swap(aLCopy, i, j);


                if (ai.type == Type.PickUp && aLCopy.indexOf(ai.twin) <= j) {
                    aLCopy.remove(ai.twin);
                    aLCopy.add(rnd.nextInt(aLCopy.size() - j + 1) + j, ai.twin);
                }

                if (aj.type == Type.Delivery && aLCopy.indexOf(aj.twin) >= i) {
                    aLCopy.remove(aj.twin);
                    aLCopy.add(rnd.nextInt(i + 1), aj.twin);
                }


                double weight = 0;
                for (CentralizedAction ak : aLCopy) {

                    if (weight > vi.capacity()) {
                        continue inner;
                    }


                    switch (ak.type) {
                        case PickUp:
                            weight += ak.task.weight;
                            break;
                        case Delivery:
                            weight -= ak.task.weight;
                            break;
                    }
                }


                _actions.put(vi, aLCopy);

                cspList.add(new CSP(_actions, vehiclesList));

            }

        }


        return cspList;

    }


    private List<CentralizedAction> removeTask(List<CentralizedAction> actionList, CentralizedAction t) {
        ArrayList<CentralizedAction> ret = new ArrayList<>();


        if (t.type == Type.PickUp) {
            ret.add(t);
            ret.add(t.twin);
        } else {
            ret.add(t.twin);
            ret.add(t);
        }

        actionList.removeAll(ret);

        return ret;
    }


    private List<Plan> buildPlans(List<Vehicle> vehiclesList) {

        ArrayList<Plan> planList = new ArrayList<>();


        for (Vehicle v : vehiclesList) {

            ArrayList<Action> tmp = new ArrayList<>();

            List<CentralizedAction> aL = actions.get(v);

            if (aL.size() < 1) {
                planList.add(new Plan(v.getCurrentCity(), tmp));
                continue;
            }

            for (Topology.City c : v.getCurrentCity().pathTo(aL.get(0).getDestinationCity())) {
                tmp.add(new Action.Move(c));
            }
            cost += v.getCurrentCity().distanceTo(aL.get(0).getDestinationCity()) * v.costPerKm();

            for (int i = 0; i < aL.size() - 1; i++) {

                CentralizedAction a1 = aL.get(i);
                Topology.City c1 = a1.getDestinationCity();

                CentralizedAction a2 = aL.get(i + 1);
                Topology.City c2 = a2.getDestinationCity();


                tmp.add(a1.toLogistAction());


                for (Topology.City c : c1.pathTo(c2)) {
                    tmp.add(new Action.Move(c));
                }
                cost += c1.distanceTo(c2) * v.costPerKm();


            }

            CentralizedAction ai = aL.get(aL.size() - 1);
            tmp.add(ai.toLogistAction());


            planList.add(new Plan(v.getCurrentCity(), tmp));
        }


        return planList;
    }

    List<Plan> toPlan(List<Vehicle> v) {
        if (plans == null)
            this.plans = buildPlans(v);
        return plans;
    }


}
