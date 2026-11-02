package config;

public class SimulationConfig {
    public int L;
    public int W;
    public int N;

    public double sinkX;
    public double sinkY;

    public double E0;
    public int k;
    public double Eelec;
    public double epsFS;
    public double epsMP;

    public double d0;

    public long seed = 42L;
    public int experimentRuns = 3;
    public boolean exportCharts = true;
    public int chartEveryNRounds = 10;

    public static SimulationConfig defaultConfig() {
        SimulationConfig c = new SimulationConfig();

        c.L = 500;
        c.W = 500;
        c.N = 100;

        c.sinkX = 50;
        c.sinkY = 150;

        c.E0 = 0.2;
        c.k = 4000;
        c.Eelec = 50e-9;
        c.epsFS = 10e-12;
        c.epsMP = 0.0013e-12;
        c.d0 = Math.sqrt(c.epsFS / c.epsMP);

        c.seed = 42L;
        c.experimentRuns = 3;
        c.exportCharts = true;
        c.chartEveryNRounds = 10;
        return c;
    }
}
