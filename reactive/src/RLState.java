import logist.topology.Topology.City;

public class RLState {
    private City currentCity;
    private City destinationCity;
    private boolean hasTask;

    public RLState(City currentCity, City destinationCity) {
        this.currentCity = currentCity;
        this.destinationCity = destinationCity;
        this.hasTask = true;
    }

    public RLState(City currentCity) {
        this.currentCity = currentCity;
        this.hasTask = false;
    }


    @Override
    public int hashCode() {
        return currentCity.hashCode() + destinationCity.hashCode() + (hasTask ? 0 : 1);
    }


    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        RLState o = (RLState) obj;
        return currentCity.id == o.getCurrentCity().id &&
                destinationCity.id == o.getDestinationCity().id &&
                hasTask == o.isHasTask();
    }


    public City getCurrentCity() {
        return currentCity;
    }

    public City getDestinationCity() {
        return destinationCity;
    }

    public boolean isHasTask() {
        return hasTask;
    }


}
