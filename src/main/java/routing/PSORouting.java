package routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import config.AlgorithmConstants;
import config.SimulationConfig;
import energy.EnergyModel;
import network.Node;
import util.RandomManager;

public class PSORouting implements RoutingAlogrithm {

    @Override
    public Route findRoute(Node src, Node sink, List<Node> CHs, SimulationConfig config) {
        Random rand = RandomManager.createRandom("PSO_" + src.id + "_" + CHs.size());
        List<Node> candidates = candidates(src, sink, CHs);
        List<Particle> swarm = new ArrayList<>();

        for (int i = 0; i < AlgorithmConstants.GA_POP; i++) {
            Particle p = new Particle();
            p.useRelay = !candidates.isEmpty() && rand.nextBoolean();
            p.relay = p.useRelay ? candidates.get(rand.nextInt(candidates.size())) : null;
            p.bestUseRelay = p.useRelay;
            p.bestRelay = p.relay;
            swarm.add(p);
        }

        Particle gBest = swarm.stream().max(Comparator.comparingDouble(p -> fitness(buildPath(src, sink, p), config))).orElse(swarm.get(0));

        for (int iter = 0; iter < AlgorithmConstants.GA_GEN; iter++) {
            for (Particle p : swarm) {
                double currentFit = fitness(buildPath(src, sink, p), config);
                double personalFit = fitness(buildPath(src, sink, p.bestUseRelay, p.bestRelay), config);
                if (currentFit > personalFit) {
                    p.bestUseRelay = p.useRelay;
                    p.bestRelay = p.relay;
                }

                double inertia = AlgorithmConstants.PSO_W;
                double cognitive = rand.nextDouble() * AlgorithmConstants.PSO_C1;
                double social = rand.nextDouble() * AlgorithmConstants.PSO_C2;
                double activation = inertia * (p.useRelay ? 1 : 0) + cognitive * (p.bestUseRelay ? 1 : 0)
                        + social * (gBest.useRelay ? 1 : 0);
                p.useRelay = !candidates.isEmpty() && activation > 1.2;

                if (p.useRelay) {
                    if (rand.nextDouble() < 0.5 && p.bestRelay != null) p.relay = p.bestRelay;
                    else if (rand.nextDouble() < 0.5 && gBest.relay != null) p.relay = gBest.relay;
                    else p.relay = candidates.get(rand.nextInt(candidates.size()));
                } else {
                    p.relay = null;
                }
            }

            gBest = swarm.stream().max(Comparator.comparingDouble(p -> fitness(buildPath(src, sink, p), config))).orElse(gBest);
        }

        return buildRoute(buildPath(src, sink, gBest), config);
    }

    private List<Node> candidates(Node src, Node sink, List<Node> CHs) {
        List<Node> result = new ArrayList<>();
        double direct = src.distanceTo(sink);
        for (Node ch : CHs) {
            if (ch == src || !ch.isAlive()) continue;
            if (src.distanceTo(ch) < direct && ch.distanceTo(sink) < direct * 1.2) result.add(ch);
        }
        return result;
    }

    private List<Node> buildPath(Node src, Node sink, Particle p) {
        return buildPath(src, sink, p.useRelay, p.relay);
    }

    private List<Node> buildPath(Node src, Node sink, boolean useRelay, Node relay) {
        List<Node> path = new ArrayList<>();
        path.add(src);
        if (useRelay && relay != null && relay != src && !relay.isSink) path.add(relay);
        path.add(sink);
        return path;
    }

    private double fitness(List<Node> path, SimulationConfig config) {
        return FitnessFunction.evaluate(buildRoute(path, config));
    }

    private Route buildRoute(List<Node> path, SimulationConfig config) {
        Route route = new Route(path);
        double energy = 0;
        double totalDistance = 0;
        double bottleneck = Double.MAX_VALUE;
        for (int i = 0; i < path.size() - 1; i++) {
            Node u = path.get(i);
            Node v = path.get(i + 1);
            double d = u.distanceTo(v);
            totalDistance += d;
            energy += EnergyModel.txEnergy(config.k, d, config);
            if (!u.isSink) bottleneck = Math.min(bottleneck, u.energy / Math.max(u.initialEnergy, 1e-9));
        }
        route.energyCost = energy;
        route.totalDistance = totalDistance;
        route.bottleneckEnergy = bottleneck == Double.MAX_VALUE ? 1.0 : bottleneck;
        return route;
    }

    private static class Particle {
        boolean useRelay;
        Node relay;
        boolean bestUseRelay;
        Node bestRelay;
    }
}
