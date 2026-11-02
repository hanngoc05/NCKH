package simulation;

import network.Network;
import network.Node;
import java.util.List;
import java.util.ArrayList;

public class Statistics {
    private static final double DEAD_RATIO_STOP = 0.95;

    private List<Double> energyHistory = new ArrayList<>();
    public int FND = -1;
    public int HND = -1;
    public int LND = -1;

    private int totalNodes;
    private int deadNodes;
    private boolean stop = false;

    public void reset() {
        FND = -1;
        HND = -1;
        LND = -1;
        stop = false;
        totalNodes = 0;
        deadNodes = 0;
        energyHistory.clear();
    }

    public void update(Network net, int round) {
        if (totalNodes == 0) totalNodes = net.nodes.size();
        deadNodes = 0;

        for (Node n : net.nodes) {
            if (!n.isAlive()) {
                deadNodes++;
                if (n.deadRound < 0) n.deadRound = round;
            }
        }

        if (deadNodes >= 1 && FND == -1) FND = round;
        if (deadNodes >= Math.max(1, totalNodes / 2) && HND == -1) HND = round;
        if (deadNodes >= DEAD_RATIO_STOP * totalNodes && LND == -1) {
            LND = round;
            stop = true;
        }
    }

    public boolean isStopConditionReached() {
        return stop;
    }

    public void print(String dataset, String clustering, String routing, double avgFitness, double remainEnergy) {
        System.out.println("==================KET QUA========================");
        System.out.println("Dataset     : " + dataset);
        System.out.println("Clustering  : " + clustering);
        System.out.println("Routing     : " + routing);
        System.out.println("------------------------------------------");
        System.out.printf("Avg Fitness : %.6e%n", avgFitness);
        System.out.printf("Remain Ener.: %.6f%n", remainEnergy);
        System.out.println("------------------------------------------");
        System.out.println("FND : " + FND);
        System.out.println("HND : " + HND);
        System.out.println("LND : " + LND);
        System.out.println("==========================================");
    }
    public void recordEnergy(Network net) {
    double total = 0;
    for (Node n : net.nodes) {
        total += n.energy;
    }
    energyHistory.add(total);
    }

    public List<Double> getEnergyHistory() {
    return new ArrayList<>(energyHistory);
}
}
