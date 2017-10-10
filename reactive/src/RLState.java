import java.util.Objects;

import logist.topology.Topology.City;

public class RLState {
    final private City currentCity;
    final private City destinationCity;
    final private boolean hasTask;

    public RLState(City currentCity, City destinationCity) {
        this.currentCity = currentCity;
        this.destinationCity = destinationCity;
        this.hasTask = true;
    }

    public RLState(City currentCity) {
        this.currentCity = currentCity;
        this.destinationCity = null;
        this.hasTask = false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCity, destinationCity, hasTask);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        RLState o = (RLState) obj;
        return currentCity.id == o.getCurrentCity().id && (destinationCity == null ? -1 : destinationCity.id) == (o.getDestinationCity() == null ? -1 : o.getDestinationCity().id)
                && hasTask == o.HasTask();
    }

    public City getCurrentCity() {
        return currentCity;
    }

    public City getDestinationCity() {
        return destinationCity;
    }

    public boolean HasTask() {
        return hasTask;
    }

}
