

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 *
 * @author
 */

import uchicago.src.sim.space.Object2DGrid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class RabbitsGrassSimulationSpace {

    private Object2DGrid grassSpace;
    private Object2DGrid agentSpace;
    private int grassEnergy;
    private int grassGrowthRate;
    private Random random = new Random();

    private static BufferedImage ogImage = null;


    public RabbitsGrassSimulationSpace(int xSize, int ySize, int grassEnergy, int grassGrowthRate) {

        try {
            ogImage = ImageIO.read(new File("rabbit_logo.png"));
        } catch (IOException e) {
            //e.printStackTrace();
        }

        grassSpace = new Object2DGrid(xSize, ySize);
        agentSpace = new Object2DGrid(xSize, ySize);
        this.grassEnergy = grassEnergy;
        this.grassGrowthRate = grassGrowthRate;
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                grassSpace.putObjectAt(i, j, new Integer(0));
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
        return (agentSpace.getObjectAt(x, y) != null);
    }

    public boolean addAgent(RabbitsGrassSimulationAgent agent) {

        ArrayList<int[]> noRabbitsCells = new ArrayList<>();

        for (int i = 0; i < agentSpace.getSizeX(); i++) {
            for (int j = 0; j < agentSpace.getSizeY(); j++) {
                if (!isCellOccupied(i, j)) {
                    noRabbitsCells.add(new int[]{i, j});
                }
            }
        }

        if (noRabbitsCells.isEmpty()) {
            return false;
        }

        int index = random.nextInt(noRabbitsCells.size());

        int[] xy = noRabbitsCells.get(index);

        int i = xy[0];
        int j = xy[1];

        agentSpace.putObjectAt(i, j, agent);

        agentSpace.putObjectAt(i, j, agent);
        agent.setXY(i, j);
        agent.setRabbitGrassSimulationSpace(this);

        return true;

    }

    public void regrowGrass() {


        ArrayList<int[]> noGrassCells = new ArrayList<>();

        for (int i = 0; i < grassSpace.getSizeX(); i++) {
            for (int j = 0; j < grassSpace.getSizeY(); j++) {
                if (getGrassAt(i, j) == 0) {
                    noGrassCells.add(new int[]{i, j});
                }
            }
        }


        for (int toGrow = grassGrowthRate; toGrow > 0 && !noGrassCells.isEmpty(); toGrow--) {
            int index = random.nextInt(noGrassCells.size());
            int[] xy = noGrassCells.get(index);

            grassSpace.putObjectAt(xy[0], xy[1], new Integer(1));

            noGrassCells.remove(index);
        }


    }

    public void removeAgentAt(int x, int y) {
        agentSpace.putObjectAt(x, y, null);
    }

    public int eatGrassAt(int x, int y) {
        int energy = getGrassAt(x, y) * grassEnergy;
        grassSpace.putObjectAt(x, y, new Integer(0));
        return energy;
    }

    public boolean moveAgentAt(int x, int y, int newX, int newY) {
        boolean retVal = false;
        if (!isCellOccupied(newX, newY)) {
            RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) agentSpace.getObjectAt(x, y);
            removeAgentAt(x, y);
            rgsa.setXY(newX, newY);
            agentSpace.putObjectAt(newX, newY, rgsa);
            retVal = true;
        }
        return retVal;
    }

    public BufferedImage getImage() {

        return ogImage;

    }


}
