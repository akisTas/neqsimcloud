/*
 * Copyright 2018 ESOL.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 /*
 * PHflash.java
 *
 * Created on 8. mars 2001, 10:56
 */
package neqsim.thermodynamicOperations.flashOps;

import neqsim.thermo.system.SystemInterface;

/**
 *
 * @author even solbraa
 * @version
 */
public class PHflash extends Flash implements java.io.Serializable {

    private static final long serialVersionUID = 1000;

    double Hspec = 0;
    Flash tpFlash;
    int type = 0;

    /**
     * Creates new PHflash
     */
    public PHflash() {
    }

    public PHflash(SystemInterface system, double Hspec, int type) {
        this.system = system;
        this.tpFlash = new TPflash(system);
        this.Hspec = Hspec;
        this.type = type;
    }

    public double calcdQdTT() {
        double dQdTT = -system.getTemperature() * system.getTemperature() * system.getCp();
        return dQdTT;
    }

    public double calcdQdT() {
        double dQ = system.getEnthalpy() - Hspec;
        return dQ;
    }

    public double solveQ() {
        double oldTemp = 1.0 / system.getTemperature(), nyTemp = 1.0 / system.getTemperature();
        double iterations = 1;
        double error = 1.0, erorOld = 1.0e10;
        double factor = 0.8;
        double newCorr = 1.0;
        system.init(2);
        boolean correctFactor = true;
        do {
            if (error > erorOld && factor > 0.1 && correctFactor) {
                factor *= 0.5;
            } else if (error < erorOld && correctFactor) {
                factor = iterations/(iterations+1.0)*1.0;
            }
            iterations++;
            oldTemp = nyTemp;
            newCorr = factor * calcdQdT() / calcdQdTT();
            nyTemp = oldTemp - newCorr;
            if (Math.abs(system.getTemperature() - 1.0 / nyTemp) > 10.0) {
                nyTemp = 1.0 / (system.getTemperature() - Math.signum(system.getTemperature() - 1.0 / nyTemp) * 10.0);
                correctFactor = false;
            } else if (nyTemp < 0) {
                nyTemp = Math.abs(1.0 / (system.getTemperature() + 10.0));
                correctFactor = false;
            } else if (Double.isNaN(nyTemp)) {
                nyTemp = oldTemp + 0.1;
                correctFactor = false;
            } else {
                correctFactor = true;
            }
            system.setTemperature(1.0 / nyTemp);
            tpFlash.run();
            system.init(2);
            erorOld = error;
            error = Math.abs(calcdQdT());
           // error = Math.abs((1.0 / nyTemp - 1.0 / oldTemp) / (1.0 / oldTemp));
           // System.out.println("temp " + system.getTemperature() + " iter "+ iterations + " error "+ error + " correction " + newCorr + " factor "+ factor);

        } while (((error+erorOld) > 1e-8 || iterations < 3) && iterations < 200);

        return 1.0 / nyTemp;
    }

    public void run() {
        tpFlash.run();
        //System.out.println("enthalpy start: " + system.getEnthalpy());
        if (type == 0) {
            solveQ();
        } else {
            sysNewtonRhapsonPHflash secondOrderSolver = new sysNewtonRhapsonPHflash(system, 2, system.getPhases()[0].getNumberOfComponents(), 0);
            secondOrderSolver.setSpec(Hspec);
            secondOrderSolver.solve(1);

        }
        //System.out.println("enthalpy: " + system.getEnthalpy());
//        System.out.println("Temperature: " + system.getTemperature());
    }

    public org.jfree.chart.JFreeChart getJFreeChart(String name) {
        return null;
    }

}
