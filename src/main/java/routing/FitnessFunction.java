package routing;

public class FitnessFunction {

    public static double evaluate(Route route) {
        if (route == null || route.path == null || route.path.size() < 2) return 0;

        double energy = Math.max(route.energyCost, 1e-9);
        int hop = Math.max(route.hopCount, 1);
        double avgDist = route.totalDistance / hop;
        double bottleneck = Math.max(route.bottleneckEnergy, 1e-9);

        double alpha = 0.45;
        double beta = 0.20;
        double gamma = 0.20;
        double delta = 0.15;

        double score = alpha / energy + beta / hop + gamma / Math.max(avgDist, 1e-9) + delta * bottleneck;
        return score;
    }
}
