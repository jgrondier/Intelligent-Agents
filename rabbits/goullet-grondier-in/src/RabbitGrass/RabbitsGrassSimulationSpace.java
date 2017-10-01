package RabbitGrass;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 *
 * @author
 */

import uchicago.src.sim.space.Object2DGrid;

public class RabbitsGrassSimulationSpace {

    private Object2DGrid grassSpace;
    private Object2DGrid agentSpace;
    private int grassEnergy;
    private int grassGrowthRate;

    public RabbitsGrassSimulationSpace(int xSize, int ySize, int grassEnergy, int grassGrowthRate) {
        grassSpace = new Object2DGrid(xSize, ySize);
        agentSpace = new Object2DGrid(xSize, ySize);
        this.grassEnergy = grassEnergy;
        this.grassGrowthRate = grassGrowthRate;
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                grassSpace.putObjectAt(i, j, new Integer(1));
            }
        }
    }

    public int getGrassAt(int x, int y) {
        int i;
        if (grassSpace.getObjectAt(x, y) != null) {
            i = ((Integer) grassSpace.getObjectAt(x, y)).intValue();
        } else {
            i = 0;
        }

        return i;
    }

    public Object2DGrid getCurrentGrassSpace() {
        return grassSpace;
    }

    public Object2DGrid getCurrentAgentSpace() {
        return agentSpace;
    }

    public boolean isCellOccupied(int x, int y) {
        boolean retVal = false;
        if (agentSpace.getObjectAt(x, y) != null) retVal = true;
        return retVal;
    }

    public boolean addAgent(RabbitsGrassSimulationAgent agent) {
        boolean retVal = false;
        int count = 0;
        int countLimit = 10 * agentSpace.getSizeX() * agentSpace.getSizeY();

        while ((retVal == false) && (count < countLimit)) {
            int x = (int) (Math.random() * (agentSpace.getSizeX()));
            int y = (int) (Math.random() * (agentSpace.getSizeY()));
            if (isCellOccupied(x, y) == false) {
                agentSpace.putObjectAt(x, y, agent);
                agent.setXY(x, y);
                agent.setRabbitGrassSimulationSpace(this);
                retVal = true;
            }
            count++;
        }
        return retVal;
    }


    public boolean hasCellGrass(int x, int y) {

        return ((Integer) grassSpace.getObjectAt(x, y)).intValue() == (new Integer(1)).intValue();

    }

    public void regrowGrass() {


        int countLimit = 10 * agentSpace.getSizeX() * agentSpace.getSizeY();


        for (int count = 0, regrownGrass = 0; count < countLimit && regrownGrass < grassGrowthRate; count++, regrownGrass++) {

            int x = (int) (Math.random() * (agentSpace.getSizeX()));
            int y = (int) (Math.random() * (agentSpace.getSizeY()));

            if (!hasCellGrass(x, y)) {
                grassSpace.putObjectAt(x, y, new Integer(1));
            }

        }


    }

    public void removeAgentAt(int x, int y) {
        agentSpace.putObjectAt(x, y, null);
    }

    public int eatGrassAt(int x, int y) {
        int grass = getGrassAt(x, y) * grassEnergy;
        grassSpace.putObjectAt(x, y, new Integer(0));
        return grass;
    }

    public boolean moveAgentAt(int x, int y, int newX, int newY) {
        boolean retVal = false;
        if (!isCellOccupied(newX, newY)) {
            RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent) agentSpace.getObjectAt(x, y);
            removeAgentAt(x, y);
            cda.setXY(newX, newY);
            agentSpace.putObjectAt(newX, newY, cda);
            retVal = true;
        }
        return retVal;
    }


}
