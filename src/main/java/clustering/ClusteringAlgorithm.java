package clustering;

import java.util.List;

import config.SimulationConfig;
import network.Network;
import network.Node;

public interface ClusteringAlgorithm {
	 /**
     * Chon cac Cluster Head (CH)
     *
     * @param network mang WSN
     * @param config   tham so mo phong
     * @return danh sach cac CH
     */
    List<Node> selectCHs(Network network, SimulationConfig config);

    /**
     * Ten thuat toan phan cum
     * (dung de in thong ke & bieu do)
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
