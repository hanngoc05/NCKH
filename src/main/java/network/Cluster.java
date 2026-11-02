package network;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    public Node CH;
    public List<Node> members = new ArrayList<>();

    public Cluster(Node ch) {
        this.CH = ch;
    }
}
