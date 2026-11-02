package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import config.SimulationConfig;
import network.Network;
import network.Node;

public class DataGenerator {

    private static final long DEFAULT_SEED = 42L;

    public static Network generateRandomNetwork(SimulationConfig config) {
        return generateRandomNetwork(config, config.seed);
    }

    public static Network generateRandomNetwork(SimulationConfig config, long seed) {
        List<Node> nodes = new ArrayList<>();
        Random rand = new Random(seed);

        for (int i = 0; i < config.N; i++) {
            double x = rand.nextDouble() * config.L;
            double y = rand.nextDouble() * config.W;
            Node n = new Node(i, x, y, config.E0);
            n.deadRound = -1;
            nodes.add(n);
        }

        Network net = new Network(nodes, config);
        net.datasetName = "Random";
        return net;
    }

    public static Network loadFromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.contains("//")) {
                    line = line.substring(0, line.indexOf("//")).trim();
                }
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }

            if (lines.size() < 10) {
                throw new IOException("File input phai co it nhat 10 dong cau hinh.");
            }

            int idx = 0;
            SimulationConfig c = new SimulationConfig();
            c.L = Integer.parseInt(lines.get(idx++));
            c.W = Integer.parseInt(lines.get(idx++));
            c.N = Integer.parseInt(lines.get(idx++));
            c.sinkX = Double.parseDouble(lines.get(idx++));
            c.sinkY = Double.parseDouble(lines.get(idx++));
            c.E0 = Double.parseDouble(lines.get(idx++));
            c.k = Integer.parseInt(lines.get(idx++));
            c.Eelec = Double.parseDouble(lines.get(idx++));
            c.epsFS = Double.parseDouble(lines.get(idx++));
            c.epsMP = Double.parseDouble(lines.get(idx++));
            c.d0 = Math.sqrt(c.epsFS / c.epsMP);
            c.seed = DEFAULT_SEED;
            c.experimentRuns = 3;
            c.exportCharts = true;
            c.chartEveryNRounds = 10;

            List<Node> nodes = new ArrayList<>();

            if (lines.size() >= 10 + c.N) {
                for (int i = 0; i < c.N; i++) {
                    String[] parts = lines.get(idx++).trim().split("\\s+");
                    if (parts.length < 2) {
                        throw new IOException("Dong toa do node khong hop le tai node " + i);
                    }
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    Node n = new Node(i, x, y, c.E0);
                    n.deadRound = -1;
                    nodes.add(n);
                }
                Network net = new Network(nodes, c);
                net.datasetName = path;
                return net;
            }

            Network net = generateRandomNetwork(c, c.seed);
            net.datasetName = path + "_randomFallback";
            return net;
        }
    }
}
