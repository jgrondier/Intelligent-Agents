import logist.topology.Topology.City;

abstract public class RLAction {
    
    
    private Type type = Type.NONE;
    private City destination;
    
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
}

enum Type {
    PICKUP, MOVE, NONE
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