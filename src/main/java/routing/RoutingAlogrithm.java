package routing;

import java.util.List;

import config.SimulationConfig;
import energy.EnergyModel;
import network.Node;

public interface RoutingAlogrithm {

    /* ===============================
       FIND ROUTE (GA / DE / PSO)
       =============================== */
    Route findRoute(
            Node sourceCH,
            Node sink,
            List<Node> CHs,
            SimulationConfig config
    );

    /* ===============================
       SEND ONE HOP (ENERGY MODEL)
       =============================== */
    default double send(
            Node sender,
            Node receiver,
            SimulationConfig config
    ) {
        if (!sender.isAlive() || !receiver.isAlive())
            return Double.MAX_VALUE;

        double d = sender.distanceTo(receiver);

        double eTx = EnergyModel.txEnergy(config.k, d, config);
        double eRx = EnergyModel.rxEnergy(config.k, config);

        sender.energy -= eTx;
        receiver.energy -= eRx;

        return eTx + eRx;
    }

    /* ===============================
       SEND MULTI-HOP ROUTE
       =============================== */
    default double sendRoute(
            Route route,
            SimulationConfig config
    ) {
        if (route == null || route.path == null || route.path.size() < 2)
            return Double.MAX_VALUE;

        double total = 0;

        for (int i = 0; i < route.path.size() - 1; i++) {
            total += send(route.path.get(i), route.path.get(i + 1), config);
        }

        route.energyCost = total;
        return total;
    }

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
