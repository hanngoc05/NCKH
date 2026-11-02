package routing;

import java.util.List;

import network.Node;

public class Route {
    public List<Node> path;
    public int hopCount;
    public double energyCost;
    public double totalDistance;
    public double bottleneckEnergy;

    public Route(List<Node> path) {
        this.path = path;
        this.hopCount = Math.max(0, path.size() - 1);
    }
}
