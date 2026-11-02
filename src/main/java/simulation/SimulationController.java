package simulation;

import java.io.IOException;
import java.util.*;

import clustering.ClusteringAlgorithm;
import clustering.DEALClustering;
import clustering.LEACHClustering;
import clustering.SFCHClustering;
import config.SimulationConfig;
import energy.EnergyModel;
import network.Cluster;
import network.Network;
import network.Node;
import routing.DERouting;
import routing.GARouting;
import routing.PSORouting;
import routing.Route;
import routing.RoutingAlogrithm;
import util.ChartUtil;
import util.DataGenerator;
import util.ExcelWriter;
import util.Logger;
import util.RandomManager;
import util.Visualizer;

public class SimulationController {

    private SimulationConfig config;
    private Network baseNetwork;
    private final Statistics stats = new Statistics();
    private final List<ExcelWriter.RunDetail> runDetails = new ArrayList<>();

    // 🔥 THÊM: lưu energy để vẽ gộp
    private final Map<String, List<Double>> energyCompare = new HashMap<>();

    private static final String LOG_FILE = "resultWSN.txt";
    private static final int MAX_SAFE_ROUND = 10000;

    private final Scanner sc;

    public SimulationController(Scanner sc) {
        this.sc = sc;
    }

    public void loadFromKeyboard() {
        config = new SimulationConfig();

        System.out.print("L = "); config.L = sc.nextInt();
        System.out.print("W = "); config.W = sc.nextInt();
        System.out.print("N = "); config.N = sc.nextInt();
        System.out.print("Sink X = "); config.sinkX = sc.nextDouble();
        System.out.print("Sink Y = "); config.sinkY = sc.nextDouble();
        System.out.print("E0 = "); config.E0 = sc.nextDouble();
        System.out.print("k = "); config.k = sc.nextInt();
        System.out.print("Eelec = "); config.Eelec = sc.nextDouble();
        System.out.print("epsFS = "); config.epsFS = sc.nextDouble();
        System.out.print("epsMP = "); config.epsMP = sc.nextDouble();
        config.d0 = Math.sqrt(config.epsFS / config.epsMP);

        askExperimentOptions(config);
        baseNetwork = DataGenerator.generateRandomNetwork(config);
        baseNetwork.datasetName = "Keyboard";
        prepareLogging();
    }

    public void loadRandom() {
        config = SimulationConfig.defaultConfig();
        askExperimentOptions(config);
        baseNetwork = DataGenerator.generateRandomNetwork(config);
        baseNetwork.datasetName = "Random";
        prepareLogging();
    }

    public void loadFromFile(String path) throws IOException {
        baseNetwork = DataGenerator.loadFromFile(path);
        config = baseNetwork.config;
        askExperimentOptions(config);
        baseNetwork.datasetName = path;
        prepareLogging();
    }

    private void askExperimentOptions(SimulationConfig c) {
        sc.nextLine();
        System.out.print("So lan lap cho moi to hop [mac dinh " + c.experimentRuns + "]: ");
        String runsInput = sc.nextLine().trim();
        if (!runsInput.isEmpty()) c.experimentRuns = Math.max(1, Integer.parseInt(runsInput));

        System.out.print("Seed bat dau [mac dinh " + c.seed + "]: ");
        String seedInput = sc.nextLine().trim();
        if (!seedInput.isEmpty()) c.seed = Long.parseLong(seedInput);

        System.out.print("Co xuat chart khong? (y/n, mac dinh " + (c.exportCharts ? "y" : "n") + "): ");
        String chartInput = sc.nextLine().trim().toLowerCase();
        if (chartInput.equals("y") || chartInput.equals("yes")) c.exportCharts = true;
        else if (chartInput.equals("n") || chartInput.equals("no")) c.exportCharts = false;

        if (c.exportCharts) {
            System.out.print("Ve chart moi bao nhieu round [mac dinh " + c.chartEveryNRounds + "]: ");
            String everyInput = sc.nextLine().trim();
            if (!everyInput.isEmpty()) c.chartEveryNRounds = Math.max(1, Integer.parseInt(everyInput));
        }
    }

    private void prepareLogging() {
        runDetails.clear();
        energyCompare.clear(); //  reset
        Logger.resetLog(LOG_FILE);
        Logger.logInput(LOG_FILE, config, baseNetwork.datasetName, MAX_SAFE_ROUND);
    }

    // public List<ExcelWriter.Result> runAllAndGetResults() {
    //     List<ExcelWriter.Result> results = new ArrayList<>();
    //     ClusteringAlgorithm[] clusteringList = { new LEACHClustering(), new DEALClustering(), new SFCHClustering() };
    //     RoutingAlogrithm[] routingList = { new GARouting(), new DERouting(), new PSORouting() };

    //     for (ClusteringAlgorithm ca : clusteringList) {
    //         for (RoutingAlogrithm ra : routingList) {
    //             List<RunOutcome> outcomes = new ArrayList<>();

    //             for (int run = 1; run <= config.experimentRuns; run++) {
    //                 Network simNet = buildNetworkForRun(run);
    //                 long seedForRun = config.seed + (run - 1);
    //                 RandomManager.setBaseSeed(seedForRun);

    //                 RunOutcome outcome = runOneSimulationAndGetResult(simNet, ca, ra, run, run == 1);
    //                 outcomes.add(outcome);

    //                 // LƯU ENERGY (run 1)
    //                 if (run == 1) {
    //                     String key = ca.getName() + "+" + ra.getName();
    //                     energyCompare.put(key, new ArrayList<>(stats.getEnergyHistory()));
    //                 }

    //                 runDetails.add(new ExcelWriter.RunDetail(
    //                         ca.getName(), ra.getName(), run,
    //                         outcome.FND, outcome.HND, outcome.LND,
    //                         outcome.fitness, outcome.remainEnergy, outcome.runtimeMs));

    //                 Logger.logRunResult(LOG_FILE, simNet.datasetName, ca.getName(), ra.getName(), run,
    //                         outcome.fitness, outcome.remainEnergy,
    //                         outcome.FND, outcome.HND, outcome.LND, outcome.runtimeMs);
    //             }

    //             ExcelWriter.Result summary = summarize(ca.getName(), ra.getName(), outcomes);
    //             results.add(summary);

    //             Logger.logSummary(LOG_FILE, ca.getName(), ra.getName(),
    //                     summary.avgFitness, summary.avgRemainEnergy,
    //                     summary.avgFND, summary.avgHND, summary.avgLND,
    //                     summary.avgRuntimeMs);

    //             printSummary(summary);
    //         }
    //     }

    //     // VẼ BIỂU ĐỒ GỘP
    //     if (config.exportCharts) {
    //         try {
    //             ChartUtil.saveMultiEnergyChart(
    //                     energyCompare,
    //                     "compare_energy_all_algorithms.png"
    //             );
    //             System.out.println("✔ Da ve bieu do so sanh!");
    //         } catch (Exception e) {
    //             System.out.println("❌ Loi ve bieu do tong");
    //             e.printStackTrace();
    //         }
    //     }

    //     return results;
    // }

        public List<ExcelWriter.Result> runAllAndGetResults() {
        List<ExcelWriter.Result> results = new ArrayList<>();

        ClusteringAlgorithm[] clusteringList = {
                new LEACHClustering(),
                new DEALClustering(),
                new SFCHClustering()
        };

        RoutingAlogrithm[] routingList = {
                new GARouting(),
                new DERouting(),
                new PSORouting()
        };

        for (ClusteringAlgorithm ca : clusteringList) {
            for (RoutingAlogrithm ra : routingList) {

                List<RunOutcome> outcomes = new ArrayList<>();

                for (int run = 1; run <= config.experimentRuns; run++) {

                    Network simNet = buildNetworkForRun(run);
                    long seedForRun = config.seed + (run - 1);
                    RandomManager.setBaseSeed(seedForRun);

                RunOutcome outcome = runOneSimulationAndGetResult(
                        simNet, ca, ra, run, run == 1
                );

                outcomes.add(outcome);

                if (run == 1) {

                    List<Double> energyCopy = new ArrayList<>(stats.getEnergyHistory());

                    if (energyCopy.isEmpty()) {
                        System.out.println("⚠ ENERGY RONG: " 
                            + ca.getClass().getSimpleName() + "+" 
                            + ra.getClass().getSimpleName());
                    }

                    String key = ca.getClass().getSimpleName() + "+" + ra.getClass().getSimpleName();

                    energyCompare.put(key, energyCopy);

                    System.out.println("SAVE KEY: " + key + " | size=" + energyCopy.size());
                }
                    runDetails.add(new ExcelWriter.RunDetail(
                            ca.getName(), ra.getName(), run,
                            outcome.FND, outcome.HND, outcome.LND,
                            outcome.fitness, outcome.remainEnergy, outcome.runtimeMs));

                    Logger.logRunResult(LOG_FILE, simNet.datasetName,
                            ca.getName(), ra.getName(), run,
                            outcome.fitness, outcome.remainEnergy,
                            outcome.FND, outcome.HND, outcome.LND, outcome.runtimeMs);
                }

                ExcelWriter.Result summary = summarize(ca.getName(), ra.getName(), outcomes);
                results.add(summary);

                Logger.logSummary(LOG_FILE, ca.getName(), ra.getName(),
                        summary.avgFitness, summary.avgRemainEnergy,
                        summary.avgFND, summary.avgHND, summary.avgLND,
                        summary.avgRuntimeMs);

                printSummary(summary);
            }
        }

        // ================= VẼ 3 BIỂU ĐỒ =================
        if (config.exportCharts) {

            String[] routingNames = {"GA", "DE", "PSO"};

            for (String routing : routingNames) {

            Map<String, List<Double>> energyMap = new HashMap<>();

            for (String key : energyCompare.keySet()) {

                String[] parts = key.split("\\+");
                String clusteringName = parts[0];
                String routingName = parts[1];

                if (routingName.equals(routing + "Routing")) {

                    String clustering = clusteringName.replace("Clustering", "");

                    energyMap.put(clustering, energyCompare.get(key));
                }
            }

            System.out.println("Routing " + routing + " -> " + energyMap.keySet());

            String fileName = "compare_energy_" + routing + ".png";

            ChartUtil.saveMultiEnergyChart(energyMap, fileName);

            System.out.println("✔ Da ve: " + fileName);
        }
    }
        return results;
    }
    private Network buildNetworkForRun(int run) {
        if (baseNetwork.datasetName.equals("Random") || baseNetwork.datasetName.equals("Keyboard")
                || baseNetwork.datasetName.endsWith("_randomFallback")) {
            return DataGenerator.generateRandomNetwork(config, config.seed + (run - 1));
        }
        Network simNet = baseNetwork.cloneNetwork();
        simNet.datasetName = baseNetwork.datasetName;
        return simNet;
    }

    private RunOutcome runOneSimulationAndGetResult(Network network, ClusteringAlgorithm ca,
            RoutingAlogrithm ra, int runIndex, boolean exportChartsThisRun) {

        stats.reset();
        network.resetEnergy();
        network.resetDeadRounds();
        network.resetCHs();
        network.resetRound();
        network.getClusters().clear();

        double totalEnergyConsumed = 0;
        int round = 0;
        long start = System.nanoTime();

        while (round < MAX_SAFE_ROUND && !stats.isStopConditionReached()) {
            round++;
            network.incrementRound();
            double roundEnergy = 0;

            network.resetCHs();
            List<Node> CHs = ca.selectCHs(network, config);
            CHs.removeIf(ch -> !ch.isAlive());

            if (CHs.isEmpty()) {
                System.out.println("⚠ KHONG CO CH: " + ca.getName() + " + " + ra.getName());
                break;
            }
            List<Cluster> clusters = new ArrayList<>();
            for (Node ch : CHs) {
                Cluster cl = new Cluster(ch);
                ch.cluster = cl;
                clusters.add(cl);
            }
            network.getClusters().clear();
            network.getClusters().addAll(clusters);
            network.assignMembers(CHs);

            for (Cluster cl : network.getClusters()) {
                Node ch = cl.CH;
                if (ch == null || !ch.isAlive()) continue;
                for (Node m : cl.members) {
                    if (m == null || !m.isAlive()) continue;
                    double d = m.distanceTo(ch);
                    double eTx = EnergyModel.txEnergy(config.k, d, config);
                    double eRx = EnergyModel.rxEnergy(config.k, config);
                    m.energy -= eTx;
                    ch.energy -= eRx;
                    roundEnergy += (eTx + eRx);
                }
            }

            for (Node ch : CHs) {
                if (!ch.isAlive()) continue;
                Route rt = ra.findRoute(ch, network.sink, CHs, config);
                ch.route = rt;
                if (rt != null && rt.path != null) {
                    for (int i = 0; i < rt.path.size() - 1; i++) {
                        Node u = rt.path.get(i);
                        Node v = rt.path.get(i + 1);
                        double d = u.distanceTo(v);
                        double eTx = EnergyModel.txEnergy(config.k, d, config);
                        double eRx = EnergyModel.rxEnergy(config.k, config);
                        u.energy -= eTx;
                        if (!v.isSink) v.energy -= eRx;
                        roundEnergy += (eTx + eRx);
                    }
                }
            }

            totalEnergyConsumed += roundEnergy;
            stats.update(network, round);

            if (exportChartsThisRun && config.exportCharts && round % config.chartEveryNRounds == 0) {
                try {
                    Visualizer.saveChart(network,
                            ca.getName() + "_" + ra.getName() + "_run" + runIndex + "_round_" + round);
                } catch (Exception e) {
                    System.out.println("Canh bao: khong ve duoc chart o round " + round);
                }
            }

            stats.recordEnergy(network);
        }

        double avgFitness = round > 0 ? totalEnergyConsumed / round : 0;
        double remainEnergy = network.totalEnergy();
        long runtimeMs = Math.round((System.nanoTime() - start) / 1_000_000.0);

        if (exportChartsThisRun) {
            ChartUtil.saveEnergyChart(
                    stats.getEnergyHistory(),
                    "energy_" + ca.getName() + "_" + ra.getName() + "_run" + runIndex + ".png"
            );
        }

        return new RunOutcome(stats.FND, stats.HND, stats.LND, avgFitness, remainEnergy, runtimeMs);
    }

    private ExcelWriter.Result summarize(String clustering, String routing, List<RunOutcome> outcomes) {
        double[] fnd = outcomes.stream().mapToDouble(o -> o.FND).toArray();
        double[] hnd = outcomes.stream().mapToDouble(o -> o.HND).toArray();
        double[] lnd = outcomes.stream().mapToDouble(o -> o.LND).toArray();
        double[] fit = outcomes.stream().mapToDouble(o -> o.fitness).toArray();
        double[] energy = outcomes.stream().mapToDouble(o -> o.remainEnergy).toArray();
        double[] runtime = outcomes.stream().mapToDouble(o -> o.runtimeMs).toArray();

        return new ExcelWriter.Result(clustering, routing, outcomes.size(),
                mean(fnd), mean(hnd), mean(lnd),
                stddev(fnd), stddev(hnd), stddev(lnd),
                mean(fit), stddev(fit),
                mean(energy), stddev(energy),
                mean(runtime));
    }

    private void printSummary(ExcelWriter.Result summary) {
        System.out.println("================ TONG HOP ================");
        System.out.println("Clustering : " + summary.clustering);
        System.out.println("Routing    : " + summary.routing);
        System.out.println("Runs       : " + summary.runs);
        System.out.printf("Avg FND    : %.2f (std %.2f)%n", summary.avgFND, summary.stdFND);
        System.out.printf("Avg HND    : %.2f (std %.2f)%n", summary.avgHND, summary.stdHND);
        System.out.printf("Avg LND    : %.2f (std %.2f)%n", summary.avgLND, summary.stdLND);
        System.out.printf("Avg Fit    : %.6e (std %.6e)%n", summary.avgFitness, summary.stdFitness);
        System.out.printf("Avg Energy : %.6f (std %.6f)%n", summary.avgRemainEnergy, summary.stdRemainEnergy);
        System.out.printf("Avg Time   : %.2f ms%n", summary.avgRuntimeMs);
        System.out.println("==========================================");
    }

    private double mean(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return values.length == 0 ? 0 : sum / values.length;
    }

    private double stddev(double[] values) {
        if (values.length <= 1) return 0;
        double mean = mean(values);
        double sumSq = 0;
        for (double v : values) {
            double d = v - mean;
            sumSq += d * d;
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }

    public List<ExcelWriter.RunDetail> getRunDetails() {
        return new ArrayList<>(runDetails);
    }

    public String getDatasetName() {
        return baseNetwork.datasetName;
    }

    private static class RunOutcome {
        int FND, HND, LND;
        double fitness, remainEnergy;
        long runtimeMs;

        RunOutcome(int FND, int HND, int LND, double fitness, double remainEnergy, long runtimeMs) {
            this.FND = FND;
            this.HND = HND;
            this.LND = LND;
            this.fitness = fitness;
            this.remainEnergy = remainEnergy;
            this.runtimeMs = runtimeMs;
        }
    }
}