package energy;

import config.SimulationConfig;

public class EnergyModel {

	    public static double txEnergy(int k, double d, SimulationConfig c) {
	        if (d < c.d0) {
	            return k * (c.Eelec + c.epsFS * d * d);
	        } else {
	            return k * (c.Eelec + c.epsMP * Math.pow(d, 4));
	        }
	    }

	    public static double rxEnergy(int k, SimulationConfig c) {
	        return k * c.Eelec;
	    }
}
