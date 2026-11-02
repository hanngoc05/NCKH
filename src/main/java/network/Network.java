package network;

import java.util.ArrayList;
import java.util.List;

import config.SimulationConfig;

public class Network {

    public SimulationConfig config;
    public List<Node> nodes = new ArrayList<>();
    public Node sink;

    public String datasetName = "Unknown";

    private List<Cluster> clusters = new ArrayList<>();
    private int currentRound = 0;

    public Network(List<Node> nodes, SimulationConfig config) {
        this.config = config;
        this.nodes = nodes;
        this.sink = new Node(-1, config.sinkX, config.sinkY, Double.MAX_VALUE);
        this.sink.isSink = true;
    }

    public void resetCHs() {
        clusters.clear();
        for (Node n : nodes) {
            n.isCH = false;
            n.cluster = null;
            n.route = null;
        }
    }

    public void resetEnergy() {
        for (Node n : nodes) {
            n.energy = n.initialEnergy;
        }
    }

    public void assignMembers(List<Node> CHs) {
        for (Node ch : CHs) {
            if (ch.cluster != null) {
                ch.cluster.members.clear();
            }
        }

        for (Node n : nodes) {
            if (!n.isAlive() || n.isCH) continue;

            Node bestCH = null;
            double bestScore = Double.MAX_VALUE;

            for (Node ch : CHs) {
                if (!ch.isAlive() || ch.cluster == null) continue;

                double distance = n.distanceTo(ch);
                double energyBonus = 1.0 - (ch.energy / Math.max(ch.initialEnergy, 1e-9));
                double loadPenalty = 0.15 * ch.cluster.members.size();
                double score = distance + 10.0 * energyBonus + loadPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    bestCH = ch;
                }
            }

            if (bestCH != null) {
                n.cluster = bestCH.cluster;
                bestCH.cluster.members.add(n);
            }
        }
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public double totalEnergy() {
        double sum = 0;
        for (Node n : nodes) {
            if (n.isAlive()) sum += n.energy;
        }
        return sum;
    }

    public int countAlive() {
        int c = 0;
        for (Node n : nodes) {
            if (n.isAlive()) c++;
        }
        return c;
    }

    public Network cloneNetwork() {
        List<Node> newNodes = new ArrayList<>();
        for (Node n : nodes) {
            newNodes.add(n.clone());
        }
        Network net = new Network(newNodes, config);
        net.datasetName = datasetName;
        net.currentRound = this.currentRound;
        return net;
    }

    public void resetDeadRounds() {
        for (Node n : nodes) {
            n.deadRound = -1;
        }
    }

    public int getRound() {
        return currentRound;
    }

    public void incrementRound() {
        currentRound++;
    }

    public void resetRound() {
        currentRound = 0;
    }
}
