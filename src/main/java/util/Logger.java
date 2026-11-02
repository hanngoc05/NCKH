package util;

import java.io.FileWriter;
import java.io.IOException;

import config.SimulationConfig;

public class Logger {

    public static void resetLog(String file) {
        try (FileWriter fw = new FileWriter(file, false)) {
            fw.write("WSN Simulation Log\n");
            fw.write("==================\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logInput(String file, SimulationConfig c, String dataset, int maxRound) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write("===== INPUT =====\n");
            fw.write("Dataset      : " + dataset + "\n");
            fw.write("L            : " + c.L + "\n");
            fw.write("W            : " + c.W + "\n");
            fw.write("N            : " + c.N + "\n");
            fw.write("Sink         : (" + c.sinkX + ", " + c.sinkY + ")\n");
            fw.write("E0           : " + c.E0 + "\n");
            fw.write("k            : " + c.k + "\n");
            fw.write("Eelec        : " + c.Eelec + "\n");
            fw.write("epsFS        : " + c.epsFS + "\n");
            fw.write("epsMP        : " + c.epsMP + "\n");
            fw.write("d0           : " + c.d0 + "\n");
            fw.write("Seed         : " + c.seed + "\n");
            fw.write("Runs         : " + c.experimentRuns + "\n");
            fw.write("Max rounds   : " + maxRound + "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logRunResult(String file, String dataset, String clustering, String routing, int runIndex,
            double fitness, double energyLeft, int FND, int HND, int LND, long runtimeMs) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write("===== RUN RESULT =====\n");
            fw.write("Dataset     : " + dataset + "\n");
            fw.write("Clustering  : " + clustering + "\n");
            fw.write("Routing     : " + routing + "\n");
            fw.write("Run         : " + runIndex + "\n");
            fw.write(String.format("Fitness     : %.6f%n", fitness));
            fw.write(String.format("Energy left : %.6f%n", energyLeft));
            fw.write("FND         : " + FND + "\n");
            fw.write("HND         : " + HND + "\n");
            fw.write("LND         : " + LND + "\n");
            fw.write("Runtime(ms) : " + runtimeMs + "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logSummary(String file, String clustering, String routing, double avgFitness, double avgEnergy,
            double avgFND, double avgHND, double avgLND, double avgRuntimeMs) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write("===== SUMMARY =====\n");
            fw.write("Clustering  : " + clustering + "\n");
            fw.write("Routing     : " + routing + "\n");
            fw.write(String.format("Avg fitness : %.6f%n", avgFitness));
            fw.write(String.format("Avg energy  : %.6f%n", avgEnergy));
            fw.write(String.format("Avg FND     : %.2f%n", avgFND));
            fw.write(String.format("Avg HND     : %.2f%n", avgHND));
            fw.write(String.format("Avg LND     : %.2f%n", avgLND));
            fw.write(String.format("Avg runtime : %.2f ms%n%n", avgRuntimeMs));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
