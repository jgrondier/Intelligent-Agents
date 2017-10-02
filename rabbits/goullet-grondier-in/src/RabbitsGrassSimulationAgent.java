import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Random;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 *
 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {

    private int x;
    private int y;
    private int vX;
    private int vY;
    private static int birthCost;
    private int energy;
    private static int IDNumber = 0;
    private int ID;
    private RabbitsGrassSimulationSpace rgsSpace;

    public RabbitsGrassSimulationAgent(int birthCost, int startingEnergy) {
        RabbitsGrassSimulationAgent.birthCost = birthCost;
        this.energy = startingEnergy;
        IDNumber++;
        ID = IDNumber;
        setVxVy();
    }

    private void setVxVy() {
        ArrayList<int[]> l = new ArrayList<int[]>();

        l.add(new int[]{-1, 0});
        l.add(new int[]{+1, 0});
        l.add(new int[]{0, -1});
        l.add(new int[]{0, +1});

        Random randomizer = new Random();
        int[] random = l.get(randomizer.nextInt(l.size()));

        vX = random[0];
        vY = random[1];

    }

    public void setRabbitGrassSimulationSpace(RabbitsGrassSimulationSpace a) {
        rgsSpace = a;
    }

    public void setXY(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public String getID() {
        return "Rabbit-" + ID;
    }

    public int getEnergy() {
        return energy;
    }

    public void report() {

        System.out.println(getID() + " at " + x + ", " + y + " has " + getEnergy() + " energy ");

    }

    public void draw(SimGraphics arg0) {

        Image img = rgsSpace.getImage();

        if (img != null) {
            arg0.drawImageToFit(rgsSpace.getImage());
        } else {
            arg0.drawFastRoundRect(Color.WHITE);
        }


    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void step() {

        int newX = x + vX;
        int newY = y + vY;

        Object2DGrid grid = rgsSpace.getCurrentAgentSpace();
        newX = (newX + grid.getSizeX()) % grid.getSizeX();
        newY = (newY + grid.getSizeY()) % grid.getSizeY();

        if (tryMove(newX, newY)) {
            energy += rgsSpace.eatGrassAt(x, y);
        }

        setVxVy();

        energy--;
    }

    private boolean tryMove(int newX, int newY) {
        return rgsSpace.moveAgentAt(x, y, newX, newY);
    }

    public boolean canGiveBirth() {
        return energy > birthCost;
    }

    public boolean tryGiveBirth() {
        if (canGiveBirth()) {
            energy -= birthCost;
            return true;
        }
        return false;
    }

}
