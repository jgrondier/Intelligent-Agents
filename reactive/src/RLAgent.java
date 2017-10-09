import logist.behavior.ReactiveBehavior;
import logist.topology.Topology;
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;

public class RLAgent implements ReactiveBehavior {


    @Override
    public void setup(Topology topology, TaskDistribution distribution, logist.agent.Agent agent) {

    }

    @Override
    public Action act(Vehicle vehicle, Task availableTask) {
        return null;
    }
}
