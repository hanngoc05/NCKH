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

public class DEALClustering implements ClusteringAlgorithm {

    @Override
    public List<Node> selectCHs(Network net, SimulationConfig c) {
        Random rand = RandomManager.createRandom("DEAL_" + net.getRound());
        List<Node> CHs = new ArrayList<>();
        double p = AlgorithmConstants.P;
        int r = net.getRound();

        List<Node> alive = net.nodes.stream().filter(Node::isAlive).toList();
        int N = alive.size();
        if (N == 0) return CHs;

        int k = Math.max(1, (int) Math.ceil(p * N));
        int cycle = Math.max(1, (int) Math.round(1.0 / p));
        double Eavg = alive.stream().mapToDouble(n -> n.energy).average().orElse(1.0);
        double davg = alive.stream().mapToDouble(n -> n.distanceTo(net.sink)).average().orElse(1.0);

        for (Node n : alive) {
            if (n.lastCHRound >= 0 && (r - n.lastCHRound) < cycle) continue;

            double denominator = N - k * (r % cycle);
            if (denominator <= 0) denominator = N;
            double Gp = (double) k / denominator;
            double Ei = n.energy / Math.max(Eavg, 1e-9);
            double di = Math.max(0.0001, n.distanceTo(net.sink));
            double distanceFactor = davg / di;
            double CSp = Ei + distanceFactor;
            double Ti = Math.min(1.0, ((1 - p) * Gp + p * CSp) * 0.5);

            if (rand.nextDouble() < Ti && satisfyConstraint(n, alive, k)) {
                n.isCH = true;
                n.lastCHRound = r;
                n.cluster = new Cluster(n);
                CHs.add(n);
            } else {
                n.isCH = false;
            }
        }

        if (CHs.isEmpty()) {
            alive.stream().max((n1, n2) -> Double.compare(n1.energy, n2.energy)).ifPresent(f -> {
                f.isCH = true;
                f.lastCHRound = r;
                f.cluster = new Cluster(f);
                CHs.add(f);
            });
        }

        return CHs;
    }

    private double estimateCA(Node n, List<Node> aliveNodes) {
        double radius = 20;
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
        return "DEAL_Standard";
    }
}
