package clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import config.AlgorithmConstants;
import config.SimulationConfig;
import network.Cluster;
import network.Network;
import network.Node;
import util.RandomManager;

public class LEACHClustering implements ClusteringAlgorithm {

    @Override
    public List<Node> selectCHs(Network net, SimulationConfig c) {
        Random rand = RandomManager.createRandom("LEACH_" + net.getRound());
        List<Node> CHs = new ArrayList<>();

        double p = AlgorithmConstants.P;
        int r = net.getRound();
        int cycle = Math.max(1, (int) (1.0 / p));

        for (Node n : net.nodes) {
            n.isCH = false;
            n.cluster = null;
        }

        List<Node> aliveNodes = net.nodes.stream().filter(Node::isAlive).toList();
        int k = Math.max(1, (int) Math.ceil(p * aliveNodes.size()));

        for (Node n : aliveNodes) {
            if (n.lastCHRound >= 0 && (r - n.lastCHRound) < cycle) continue;

            int mod = r % cycle;
            double threshold = p / Math.max(1e-9, (1.0 - p * mod));
            threshold = Math.min(1.0, threshold);

            if (rand.nextDouble() < threshold && satisfyConstraint(n, aliveNodes, k)) {
                n.isCH = true;
                n.lastCHRound = r;
                n.cluster = new Cluster(n);
                CHs.add(n);
            }
        }

        if (CHs.isEmpty() && !aliveNodes.isEmpty()) {
            Node fallback = aliveNodes.stream().max((a, b) -> Double.compare(a.energy, b.energy)).orElse(null);
            if (fallback != null) {
                fallback.isCH = true;
                fallback.lastCHRound = r;
                fallback.cluster = new Cluster(fallback);
                CHs.add(fallback);
            }
        }

        return CHs;
    }

    private double estimateCA(Node n, List<Node> aliveNodes) {
        double radius = 20.0;
        int count = 0;
        for (Node m : aliveNodes) {
            if (n.distanceTo(m) <= radius) count++;
        }
        return count;
    }

    private boolean satisfyConstraint(Node n, List<Node> aliveNodes, int k) {
        double Eav = aliveNodes.stream().mapToDouble(x -> x.energy).average().orElse(1.0);
        if (Eav <= 0) return true;

        double eta = 0.5;
        double Ej = n.energy;
        double Nav = (double) aliveNodes.size() / Math.max(1, k);
        double lower = Nav + eta * (Ej - Eav) * (Nav / Eav);
        double upper = Nav + eta * (Ej - Eav) * Nav;
        double CAj = estimateCA(n, aliveNodes);
        return CAj >= lower && CAj <= upper;
    }

    @Override
    public String getName() {
        return "LEACH_Standard";
    }
}
