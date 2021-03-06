import logist.task.Task;

import java.util.Comparator;

public class StateComparator implements Comparator<DelState> {

	int costPerKm;

	public StateComparator(int costPerKm) {
		super();

		this.costPerKm = costPerKm;
	}

	@Override
	public int compare(DelState t1, DelState t2) {
		return (int) (f(t1) - f(t2));
	}

	public double f(DelState t) {

		return t.getTotalCost() + h(t);

	}

	public double h(DelState t) {

		double max = 0;
		if (!t.getWorldTasks().isEmpty())
			for (Task tmp : t.getWorldTasks()) {
				double distance = t.getLocation().distanceTo(tmp.pickupCity) + tmp.deliveryCity.distanceTo(tmp.pickupCity);
				if (distance > max) {
					max = distance;
				}
			}
		else
			for (Task tmp : t.getPickedTasks()) {
				double distance = t.getLocation().distanceTo(tmp.deliveryCity);
				if (distance > max) {
					max = distance;
				}
			}
		return (max * costPerKm);
	}

}
