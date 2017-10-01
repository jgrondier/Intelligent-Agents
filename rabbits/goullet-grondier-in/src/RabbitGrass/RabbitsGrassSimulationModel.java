package RabbitGrass;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {

    // Default Values
    private static final int WORLDXSIZE = 20;
    private static final int WORLDYSIZE = 20;
    private static final int NUMRABBITS = 20;
    private static final int BIRTHCOST = 5;
    private static final int GRASSGROWTHRATE = 10;
    private static final int STARTINGENERGY = 10;
    private static final int GRASSENERGY = 4;


    private static int worldXSize = WORLDXSIZE;
    private static int worldYSize = WORLDYSIZE;
    private static int numRabbits = NUMRABBITS;
    private static int birthCost = BIRTHCOST;
    private static int grassGrowthRate = GRASSGROWTHRATE;
    private static int startingEnergy = STARTINGENERGY;
    private static int grassEnergy = GRASSENERGY;

    private static Schedule schedule;

    private RabbitsGrassSimulationSpace rgsSpace;

    private DisplaySurface displaySurf;

    private ArrayList<RabbitsGrassSimulationAgent> agentList;


    public static void main(String[] args) {

        System.out.println("Rabbit skeleton");

        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        init.loadModel(model, "", false);

    }

    public void setup() {
        System.out.println("Running setup");
        rgsSpace = null;
        agentList = new ArrayList();
        schedule = new Schedule(1);

        if (displaySurf != null) {
            displaySurf.dispose();
        }
        displaySurf = null;
        displaySurf = new DisplaySurface(this, "Rabbits Grass Simulation Model Window 1");
        registerDisplaySurface("Rabbits Grass Simulation Model Window 1", displaySurf);

    }


    public void begin() {
        buildModel();
        buildSchedule();
        buildDisplay();

        displaySurf.display();
    }

    public void buildModel() {
        System.out.println("Running BuildModel");
        rgsSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize, grassEnergy, grassGrowthRate);

        for (int i = 0; i < numRabbits; i++) {
            addNewAgent();
        }


        for (RabbitsGrassSimulationAgent rgsa : agentList) {
            rgsa.report();
        }

    }

    public void buildSchedule() {
        System.out.println("Running BuildSchedule");

        class RabbitGrassStep extends BasicAction {
            public void execute() {
                SimUtilities.shuffle(agentList);

                rgsSpace.regrowGrass();

                for (int i = 0; i < agentList.size(); i++) {
                    RabbitsGrassSimulationAgent rgsa = agentList.get(i);

                    rgsa.step();

                    ArrayList<int[]> l = rgsa.tryGivingBirth();

                    if (l.size() > 0) {

                        Random random = new Random();
                        int[] a = l.get(random.nextInt(l.size()));
                        addNewAgent(a[0], a[1]);
                    }

                }

                reapDeadRabbits();

                displaySurf.updateDisplay();

            }
        }

        schedule.scheduleActionBeginning(0, new RabbitGrassStep());

        class RabbitGrassCountLiving extends BasicAction {
            public void execute() {
                countLivingAgents();
            }
        }

        schedule.scheduleActionAtInterval(10, new RabbitGrassCountLiving());


    }

    public void buildDisplay() {
        System.out.println("Running BuildDisplay");

        ColorMap map = new ColorMap();

        map.mapColor(0, Color.ORANGE);
        map.mapColor(1, Color.GREEN);

        Value2DDisplay displayGrass = new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);

        displaySurf.addDisplayableProbeable(displayGrass, "Grass");

        Object2DDisplay displayAgents = new Object2DDisplay(rgsSpace.getCurrentAgentSpace());
        displayAgents.setObjectList(agentList);

        displaySurf.addDisplayableProbeable(displayAgents, "Agents");


    }

    public String[] getInitParam() {
        String[] initParams = {"WorldXSize", "WorldYSize", "NumRabbits", "BirthCost", "StartingEnergy", "GrassGrowthRate", "GrassEnergy"};
        return initParams;
    }

    public String getName() {
        return "Rabbit Grass Simulation";
    }

    public Schedule getSchedule() {
        return schedule;
    }

    private void addNewAgent() {
        RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(birthCost, startingEnergy);
        agentList.add(a);
        rgsSpace.addAgent(a);
    }

    private void addNewAgent(int x, int y) {
        RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(x, y, birthCost, startingEnergy);
        agentList.add(a);
    }

    private int reapDeadRabbits() {

        int count = 0;
        for (int i = (agentList.size() - 1); i >= 0; i--) {
            RabbitsGrassSimulationAgent rgsa = agentList.get(i);
            if (rgsa.getEnergy() < 1) {
                rgsSpace.removeAgentAt(rgsa.getX(), rgsa.getY());
                agentList.remove(i);
                count++;
            }
        }
        return count;


    }

    private int countLivingAgents() {
        int livingAgents = 0;
        for (RabbitsGrassSimulationAgent rgsa : agentList) {
            if (rgsa.getEnergy() > 0) {
                livingAgents++;
            }
        }

        System.out.println("Number of living rabbits is: " + livingAgents);

        return livingAgents;
    }

    public int getGrassEnergy() {
        return RabbitsGrassSimulationModel.grassEnergy;
    }

    public void setGrassEnergy(int i) {
        RabbitsGrassSimulationModel.grassEnergy = i;
    }

    public int getBirthCost() {
        return birthCost;
    }

    public void setBirthCost(int birthCost) {
        RabbitsGrassSimulationModel.birthCost = birthCost;
    }


    public int getNumRabbits() {
        return numRabbits;
    }

    public void setNumRabbits(int numRabbits) {
        RabbitsGrassSimulationModel.numRabbits = numRabbits;
    }


    public int getGrassGrowthRate() {
        return grassGrowthRate;
    }

    public void setGrassGrowthRate(int grassGrowthRate) {
        RabbitsGrassSimulationModel.grassGrowthRate = grassGrowthRate;
    }

    public int getWorldXSize() {
        return worldXSize;
    }

    public void setWorldXSize(int worldXSize) {
        RabbitsGrassSimulationModel.worldXSize = worldXSize;
    }

    public int getWorldYSize() {
        return worldYSize;
    }

    public void setWorldYSize(int worldYSize) {
        RabbitsGrassSimulationModel.worldYSize = worldYSize;
    }

    public int getStartingEnergy() {
        return startingEnergy;
    }

    public void setStartingEnergy(int startingEnergy) {
        RabbitsGrassSimulationModel.startingEnergy = startingEnergy;
    }

}

