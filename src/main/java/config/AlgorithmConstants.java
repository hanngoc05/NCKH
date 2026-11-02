package config;

public class AlgorithmConstants {
	private AlgorithmConstants() {}
	public SimulationConfig c;
    // Tỷ lệ CH (LEACH-like)
    public static final double P = 0.05;

    // Fitness weights (theo paper)
    public static final double LAMBDA1 = 0.4;   // Erest
    public static final double LAMBDA2 = 0.3;   // node degree
    public static final double LAMBDA3 = -0.3;  // distance penalty
    public static final double LAMBDA4 = 0.1;   // rotation / fairness

 // Tham số mô hình
    public static final double ALPHA = 0.6;
    public static final double BETA  = 0.4;
    public static final double OMEGA_R = 0.2;

    // GA
    public static final int GA_POP = 20;
    public static final int GA_GEN = 50;
    public static final double GA_CR = 0.8;
    public static final double GA_MR = 0.1;

    // DE
    public static final double DE_F = 0.5;
    public static final double DE_CR = 0.9;

    // PSO
    public static final double PSO_W = 0.7;
    public static final double PSO_C1 = 1.5;
    public static final double PSO_C2 = 1.5;
}
