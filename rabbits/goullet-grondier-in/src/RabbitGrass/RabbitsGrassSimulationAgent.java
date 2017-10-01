package RabbitGrass;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


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

        ArrayList<int[]> l = new ArrayList();

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

        if (ID == 1) {
            arg0.drawFastRoundRect(Color.RED);
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

    public boolean tryGivingBirth() {

        return energy > birthCost;

    }

}
