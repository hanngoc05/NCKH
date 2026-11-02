package util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

public class ExcelWriter {

    public static class Result {
        public String clustering;
        public String routing;
        public int runs;
        public double avgFND;
        public double avgHND;
        public double avgLND;
        public double stdFND;
        public double stdHND;
        public double stdLND;
        public double avgFitness;
        public double stdFitness;
        public double avgRemainEnergy;
        public double stdRemainEnergy;
        public double avgRuntimeMs;

        public Result(String clustering, String routing, int runs, double avgFND, double avgHND, double avgLND,
                double stdFND, double stdHND, double stdLND, double avgFitness, double stdFitness,
                double avgRemainEnergy, double stdRemainEnergy, double avgRuntimeMs) {
            this.clustering = clustering;
            this.routing = routing;
            this.runs = runs;
            this.avgFND = avgFND;
            this.avgHND = avgHND;
            this.avgLND = avgLND;
            this.stdFND = stdFND;
            this.stdHND = stdHND;
            this.stdLND = stdLND;
            this.avgFitness = avgFitness;
            this.stdFitness = stdFitness;
            this.avgRemainEnergy = avgRemainEnergy;
            this.stdRemainEnergy = stdRemainEnergy;
            this.avgRuntimeMs = avgRuntimeMs;
        }
    }

    public static class RunDetail {
        public String clustering;
        public String routing;
        public int runIndex;
        public int FND;
        public int HND;
        public int LND;
        public double fitness;
        public double remainEnergy;
        public long runtimeMs;

        public RunDetail(String clustering, String routing, int runIndex, int FND, int HND, int LND,
                double fitness, double remainEnergy, long runtimeMs) {
            this.clustering = clustering;
            this.routing = routing;
            this.runIndex = runIndex;
            this.FND = FND;
            this.HND = HND;
            this.LND = LND;
            this.fitness = fitness;
            this.remainEnergy = remainEnergy;
            this.runtimeMs = runtimeMs;
        }
    }

    public static void write(String baseFileName, List<Result> summaryResults, List<RunDetail> runDetails) {
        String summaryFile = withSuffix(baseFileName, "_summary.csv");
        String detailFile = withSuffix(baseFileName, "_perrun.csv");
        writeSummary(summaryFile, summaryResults);
        writeDetails(detailFile, runDetails);
        System.out.println("Da ghi file ket qua: " + summaryFile);
        System.out.println("Da ghi file ket qua: " + detailFile);
    }

    private static void writeSummary(String fileName, List<Result> results) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), java.nio.charset.StandardCharsets.UTF_8), true)) {
            out.println("Clustering,Routing,Runs,AvgFND,StdFND,AvgHND,StdHND,AvgLND,StdLND,AvgFitness,StdFitness,AvgRemainEnergy,StdRemainEnergy,AvgRuntimeMs");
            for (Result r : results) {
                out.printf(Locale.US,
                        "%s,%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.8f,%.8f,%.8f,%.8f,%.4f%n",
                        r.clustering, r.routing, r.runs, r.avgFND, r.stdFND, r.avgHND, r.stdHND,
                        r.avgLND, r.stdLND, r.avgFitness, r.stdFitness, r.avgRemainEnergy,
                        r.stdRemainEnergy, r.avgRuntimeMs);
            }
        } catch (IOException e) {
            throw new RuntimeException("Khong ghi duoc file summary CSV", e);
        }
    }

    private static void writeDetails(String fileName, List<RunDetail> details) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), java.nio.charset.StandardCharsets.UTF_8), true)) {
            out.println("Clustering,Routing,Run,FND,HND,LND,Fitness,RemainEnergy,RuntimeMs");
            for (RunDetail d : details) {
                out.printf(Locale.US, "%s,%s,%d,%d,%d,%d,%.8f,%.8f,%d%n",
                        d.clustering, d.routing, d.runIndex, d.FND, d.HND, d.LND,
                        d.fitness, d.remainEnergy, d.runtimeMs);
            }
        } catch (IOException e) {
            throw new RuntimeException("Khong ghi duoc file per-run CSV", e);
        }
    }

    private static String withSuffix(String baseFileName, String suffix) {
        int dot = baseFileName.lastIndexOf('.');
        String base = dot >= 0 ? baseFileName.substring(0, dot) : baseFileName;
        return base + suffix;
    }
}
