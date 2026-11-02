package network;

import routing.Route;

public class Node implements Cloneable {

    public int id;
    public double x, y;
    public double energy;
    public int deadRound = -1;
    public boolean isCH = false;
    public boolean isSink = false;

    public Cluster cluster;
    public int lastCHRound = -1;
    public double initialEnergy;
    public Route route;

    public Node(int id, double x, double y, double energy) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.energy = energy;
        this.initialEnergy = energy;
    }

    public boolean isAlive() {
        if (energy <= 0) {
            energy = 0;
            return false;
        }
        return true;
    }

    public double distanceTo(Node other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public Node clone() {
        Node n = new Node(id, x, y, initialEnergy);
        n.energy = this.energy;
        n.deadRound = this.deadRound;
        n.isCH = this.isCH;
        n.isSink = this.isSink;
        n.lastCHRound = this.lastCHRound;
        n.route = this.route;
        return n;
    }
}
