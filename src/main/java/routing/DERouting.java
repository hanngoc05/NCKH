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

public class DERouting implements RoutingAlogrithm {

    @Override
    public Route findRoute(Node src, Node sink, List<Node> CHs, SimulationConfig config) {
        Random rand = RandomManager.createRandom("DE_" + src.id + "_" + CHs.size());
        List<List<Node>> pop = initPopulation(src, sink, CHs, rand);

        for (int gen = 0; gen < AlgorithmConstants.GA_GEN; gen++) {
            List<List<Node>> next = new ArrayList<>();
            for (int i = 0; i < pop.size(); i++) {
                List<Node> target = pop.get(i);
                List<Node> a = pop.get(rand.nextInt(pop.size()));
                List<Node> b = pop.get(rand.nextInt(pop.size()));
                List<Node> trial = crossover(src, sink, a, b, target, CHs, rand);
                next.add(fitness(trial, config) >= fitness(target, config) ? trial : target);
            }
            pop = next;
        }

        List<Node> best = pop.stream().max(Comparator.comparingDouble(p -> fitness(p, config))).orElse(pop.get(0));
        return buildRoute(best, config);
    }

    private List<List<Node>> initPopulation(Node src, Node sink, List<Node> CHs, Random rand) {
        List<List<Node>> pop = new ArrayList<>();
        List<Node> candidates = candidates(src, sink, CHs);
        for (int i = 0; i < AlgorithmConstants.GA_POP; i++) {
            List<Node> p = new ArrayList<>();
            p.add(src);
            if (!candidates.isEmpty() && rand.nextBoolean()) p.add(candidates.get(rand.nextInt(candidates.size())));
            p.add(sink);
            pop.add(p);
        }
        return pop;
    }

    private List<Node> crossover(Node src, Node sink, List<Node> a, List<Node> b, List<Node> target, List<Node> CHs, Random rand) {
        List<Node> candidates = candidates(src, sink, CHs);
        List<Node> trial = new ArrayList<>();
        trial.add(src);

        Node chosen = null;
        if (rand.nextDouble() < AlgorithmConstants.DE_CR) {
            if (a.size() == 3 && b.size() == 2) chosen = a.get(1);
            else if (a.size() == 2 && b.size() == 3) chosen = b.get(1);
            else if (a.size() == 3 && b.size() == 3) chosen = rand.nextBoolean() ? a.get(1) : b.get(1);
        } else if (target.size() == 3) {
            chosen = target.get(1);
        }

        if (chosen == null && !candidates.isEmpty() && rand.nextDouble() < AlgorithmConstants.DE_F) {
            chosen = candidates.get(rand.nextInt(candidates.size()));
        }

        if (chosen != null && chosen != src && !chosen.isSink) trial.add(chosen);
        trial.add(sink);
        return trial;
    }

    private List<Node> candidates(Node src, Node sink, List<Node> CHs) {
        List<Node> result = new ArrayList<>();
        double direct = src.distanceTo(sink);
        for (Node ch : CHs) {
            if (ch == src || !ch.isAlive()) continue;
            if (src.distanceTo(ch) + ch.distanceTo(sink) <= direct * 1.4) result.add(ch);
        }
        return result;
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
}
