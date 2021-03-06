/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neqsim.thermodynamicOperations.flashOps.saturationOps;

import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 *
 * @author ESOL
 */
public class HydrateEquilibriumLine extends constantDutyTemperatureFlash {

    private static final long serialVersionUID = 1000;

    double[][] hydratePoints = null;
    double minPressure = 1.0, maxPressure = 200.0;
    int numberOfPoints = 10;

    public HydrateEquilibriumLine(SystemInterface system, double minPres, double maxPres) {
        super(system);
        minPressure = minPres;
        maxPressure = maxPres;
    }

    @Override
	public void run() {

        SystemInterface system = (SystemInterface) this.system.clone();
        hydratePoints = new double[2][numberOfPoints];
        system.setHydrateCheck(true);
        ThermodynamicOperations ops = new ThermodynamicOperations(system);

        system.setPressure(minPressure);
        double dp = (maxPressure - minPressure) / (numberOfPoints - 1.0);
        for (int i = 0; i < numberOfPoints; i++) {

            system.setPressure(minPressure + dp * i);
            try {
                ops.hydrateFormationTemperature();
            } catch (Exception e) {
                // logger.error("error",e);
            }
            hydratePoints[0][i] = system.getTemperature();
            hydratePoints[1][i] = system.getPressure();
            // system.display();

        }
    }

    @Override
	public double[][] getPoints(int i) {
        return hydratePoints;
    }

}
