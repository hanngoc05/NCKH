package clustering;

import java.util.ArrayList;
import java.util.List;

import config.AlgorithmConstants;
import config.SimulationConfig;
import network.Cluster;
import network.Network;
import network.Node;

public class SFCHClustering implements ClusteringAlgorithm {

    @Override
    public List<Node> selectCHs(Network net, SimulationConfig c) {
        List<Node> CHs = new ArrayList<>();
        List<Node> alive = new ArrayList<>();
        for (Node n : net.nodes) if (n.isAlive()) alive.add(n);

        int N = alive.size();
        if (N == 0) return CHs;

        double Q = Math.sqrt((N * c.d0) / (2 * Math.PI));
        int numCH = Math.max(1, (int) Math.round(Q));
        double S = c.L * c.W;
        double R = Math.sqrt(S / (Math.PI * Math.max(Q, 1e-9)));
        double E_max = alive.stream().mapToDouble(a -> a.energy).max().orElse(1.0);
        double maxDistToBS = alive.stream().mapToDouble(a -> a.distanceTo(net.sink)).max().orElse(1.0);

        int cycle = Math.max(1, (int) (1.0 / AlgorithmConstants.P));
        int currentRound = net.getRound();

        List<Node> candidates = new ArrayList<>();
        for (Node n : alive) {
            n.isCH = false;
            n.cluster = null;
            if (n.lastCHRound < 0 || (currentRound - n.lastCHRound) >= cycle) {
                candidates.add(n);
            }
        }
        if (candidates.isEmpty()) candidates = alive;

        while (CHs.size() < numCH && !candidates.isEmpty()) {
            Node best = null;
            double bestZ = -Double.MAX_VALUE;

            for (Node n : candidates) {
                double Erest = n.energy / Math.max(E_max, 1e-9);
                int ni = 0;
                for (Node m : alive) if (m != n && n.distanceTo(m) <= R) ni++;
                double dBS = n.distanceTo(net.sink);
                double vi = 1.0 - (dBS / Math.max(maxDistToBS, 1e-9));
                double wr = 1.0;
                double Z = AlgorithmConstants.LAMBDA1 * Erest + AlgorithmConstants.LAMBDA2 * ni
                        + AlgorithmConstants.LAMBDA3 * vi + AlgorithmConstants.LAMBDA4 * wr;

                if (Z > bestZ && satisfyConstraint(n, alive, numCH)) {
                    bestZ = Z;
                    best = n;
                }
            }

            if (best == null) {
                best = candidates.stream().max((a, b) -> Double.compare(a.energy, b.energy)).orElse(null);
                if (best == null) break;
            }

            best.isCH = true;
            best.lastCHRound = currentRound;
            best.cluster = new Cluster(best);
            CHs.add(best);
            candidates.remove(best);
        }

        if (CHs.isEmpty()) {
            alive.stream().max((n1, n2) -> Double.compare(n1.energy, n2.energy)).ifPresent(f -> {
                f.isCH = true;
                f.lastCHRound = currentRound;
                f.cluster = new Cluster(f);
                CHs.add(f);
            });
        }

        return CHs;
    }

    private double estimateCA(Node n, List<Node> aliveNodes) {
        double radius = 20;
        int count = 0;
        for (Node m : aliveNodes) if (n.distanceTo(m) <= radius) count++;
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
        return "SFCH";
    }
}
