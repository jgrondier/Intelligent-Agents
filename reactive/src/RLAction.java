import logist.topology.Topology.City;

abstract class RLAction {
    
    protected enum Type {
        PICKUP, MOVE, NONE
    }
    
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