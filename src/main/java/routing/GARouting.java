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

public class GARouting implements RoutingAlogrithm {

    @Override
    public Route findRoute(Node src, Node sink, List<Node> CHs, SimulationConfig config) {
        Random rand = RandomManager.createRandom("GA_" + src.id + "_" + CHs.size());
        List<List<Node>> population = initPopulation(src, sink, CHs, rand);

        for (int gen = 0; gen < AlgorithmConstants.GA_GEN; gen++) {
            population = evolve(population, CHs, sink, config, rand);
        }

        return buildRoute(select(population, config), config);
    }

    private List<List<Node>> initPopulation(Node src, Node sink, List<Node> CHs, Random rand) {
        List<List<Node>> pop = new ArrayList<>();
        List<Node> candidates = intermediateCandidates(src, sink, CHs);

        for (int i = 0; i < AlgorithmConstants.GA_POP; i++) {
            List<Node> path = new ArrayList<>();
            path.add(src);
            if (!candidates.isEmpty() && rand.nextBoolean()) {
                path.add(candidates.get(rand.nextInt(candidates.size())));
            }
            path.add(sink);
            pop.add(path);
        }
        return pop;
    }

    private List<List<Node>> evolve(List<List<Node>> pop, List<Node> CHs, Node sink, SimulationConfig config, Random rand) {
        List<List<Node>> sorted = new ArrayList<>(pop);
        sorted.sort(Comparator.comparingDouble((List<Node> p) -> fitness(p, config)).reversed());

        List<List<Node>> next = new ArrayList<>();
        next.add(new ArrayList<>(sorted.get(0)));

        while (next.size() < pop.size()) {
            List<Node> p1 = tournament(sorted, config, rand);
            List<Node> p2 = tournament(sorted, config, rand);
            List<Node> child = crossover(p1, p2, rand);
            mutate(child, p1.get(0), sink, CHs, rand);
            next.add(child);
        }
        return next;
    }

    private List<Node> tournament(List<List<Node>> pop, SimulationConfig config, Random rand) {
        List<Node> a = pop.get(rand.nextInt(pop.size()));
        List<Node> b = pop.get(rand.nextInt(pop.size()));
        return fitness(a, config) >= fitness(b, config) ? a : b;
    }

    private List<Node> crossover(List<Node> a, List<Node> b, Random rand) {
        if (rand.nextDouble() > AlgorithmConstants.GA_CR) {
            return new ArrayList<>(fitnessByLength(a) >= fitnessByLength(b) ? a : b);
        }

        List<Node> child = new ArrayList<>();
        child.add(a.get(0));
        Node middle = null;
        if (a.size() == 3 && b.size() == 3) {
            middle = rand.nextBoolean() ? a.get(1) : b.get(1);
        } else if (a.size() == 3) {
            middle = a.get(1);
        } else if (b.size() == 3) {
            middle = b.get(1);
        }
        if (middle != null && middle != a.get(0) && !middle.isSink) child.add(middle);
        child.add(a.get(a.size() - 1));
        return child;
    }

    private void mutate(List<Node> child, Node src, Node sink, List<Node> CHs, Random rand) {
        if (rand.nextDouble() > AlgorithmConstants.GA_MR) return;
        List<Node> candidates = intermediateCandidates(src, sink, CHs);
        child.clear();
        child.add(src);
        if (!candidates.isEmpty() && rand.nextBoolean()) {
            child.add(candidates.get(rand.nextInt(candidates.size())));
        }
        child.add(sink);
    }

    private List<Node> intermediateCandidates(Node src, Node sink, List<Node> CHs) {
        List<Node> candidates = new ArrayList<>();
        double direct = src.distanceTo(sink);
        for (Node ch : CHs) {
            if (ch == src || !ch.isAlive()) continue;
            if (src.distanceTo(ch) < direct && ch.distanceTo(sink) < direct * 1.2) {
                candidates.add(ch);
            }
        }
        return candidates;
    }

    private List<Node> select(List<List<Node>> pop, SimulationConfig config) {
        return pop.stream().max(Comparator.comparingDouble(p -> fitness(p, config))).orElse(pop.get(0));
    }

    private double fitness(List<Node> path, SimulationConfig config) {
        return FitnessFunction.evaluate(buildRoute(path, config));
    }

    private double fitnessByLength(List<Node> path) {
        return -path.size();
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
