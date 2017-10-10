import logist.topology.Topology.City;

abstract public class RLAction {
    
    
    private final Type type;
    private final City destination;
    
    protected RLAction(Type type, City destination) {
        this.type = type;
        this.destination = destination;
    }
    
    public Type getType() {
        return type;
    }
    
    public City getDestination() {
        return destination;
    }
    
    @Override
    public String toString() {
        return type.toString() + " to " + destination.name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        RLAction o = (RLAction) obj;
        return o.type == this.type && o.getDestination() == this.getDestination();
    }
}

enum Type {
    PICKUP, MOVE
}

class RLPickup extends RLAction {
    
    public RLPickup(City destination) {
        super(Type.PICKUP, destination);
    }
}

class RLMove extends RLAction {
    
    public RLMove(City destination) {
        super(Type.MOVE, destination);
    }
}