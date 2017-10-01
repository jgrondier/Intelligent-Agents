package RabbitGrass;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.*;
import java.util.ArrayList;


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
        x = -1;
        y = -1;
        RabbitsGrassSimulationAgent.birthCost = birthCost;
        this.energy = startingEnergy;
        IDNumber++;
        ID = IDNumber;
        setVxVy();

    }

    public RabbitsGrassSimulationAgent(int x, int y, int birthCost, int startingEnergy) {
        this(birthCost, startingEnergy);
        this.x = x;
        this.y = y;
    }

    private void setVxVy() {
        vX = 0;
        vY = 0;
        while ((vX == 0) && (vY == 0)) {
            vX = (int) Math.floor(Math.random() * 3) - 1;
            vY = (int) Math.floor(Math.random() * 3) - 1;
        }
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

        System.out.println(getID() +
                " at " +
                x + ", " + y +
                " has " +
                getEnergy() + " energy ");

    }

    public void draw(SimGraphics arg0) {
        if (energy > birthCost) {
            arg0.drawFastRoundRect(Color.WHITE);
        } else {
            arg0.drawFastRoundRect(Color.GRAY);
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

    public ArrayList<int[]> tryGivingBirth() {

        ArrayList n = rgsSpace.getEmptyNeighbours(x, y);

        if (energy <= birthCost || n.size() < 1) {
            return new ArrayList<>();
        }

        energy -= birthCost;
        return rgsSpace.getEmptyNeighbours(x, y);


    }

}
