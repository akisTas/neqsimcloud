/*
 * Class.java
 *
 * Created on 19. november 2001, 11:43
 */
package neqsim.thermo.component;

import neqsim.thermo.phase.PhaseInterface;
import org.apache.log4j.Logger;

/**
 *
 * @author  esol
 * @version
 */
public class ComponentHydrateKluda extends Component {

    private static final long serialVersionUID = 1000;

    double par1_struc1 = 17.44;
    double par2_struc1 = -6003.9;
    double par1_struc2 = 17.332;
    double par2_struc2 = -6017.6;
    int hydrateStructure = 0;
    double coordNumb[][][] = new double[3][2][2]; //[structure][cavitytype]
    double cavRadius[][][] = new double[3][2][2]; //[structure][cavitytype]
    double cavNumb[][] = new double[2][2]; //[structure][cavitytype]
    double cavprwat[][] = new double[2][2]; //[structure][cavitytype]
    double reffug[] = new double[20];
    static Logger logger = Logger.getLogger(ComponentHydrateKluda.class);


    /** Creates new Class */
    public ComponentHydrateKluda() {
    }

    public ComponentHydrateKluda(String component_name, double moles, double molesInPhase, int compnumber) {
        super(component_name, moles, molesInPhase, compnumber);
        coordNumb[0][0][0] = 20.0;
        coordNumb[0][0][1] = 24.0;
        cavRadius[0][0][0] = 3.906;
        cavRadius[0][0][1] = 4.326;
        coordNumb[1][0][0] = 20.0;
        coordNumb[1][0][1] = 24.0;
        cavRadius[1][0][0] = 6.593;
        cavRadius[1][0][1] = 7.078;
        coordNumb[2][0][0] = 50.0;
        coordNumb[2][0][1] = 50.0;
        cavRadius[2][0][0] = 8.086;
        cavRadius[2][0][1] = 8.285;
        cavNumb[0][0] = 2.0;
        cavNumb[0][1] = 6.0;
        cavprwat[0][0] = 1.0 / 23.0;
        cavprwat[0][1] = 3.0 / 23.0;

        coordNumb[0][1][0] = 20.0;
        coordNumb[0][1][1] = 28.0;
        cavRadius[0][1][0] = 3.902;
        cavRadius[0][1][1] = 4.683;
        cavNumb[1][0] = 16.0;
        cavNumb[1][1] = 8.0;
        cavprwat[1][0] = 2.0 / 17.0;
        cavprwat[1][1] = 1.0 / 17.0;
    }

    public double fugcoef(PhaseInterface phase) {
        return fugcoef(phase, phase.getNumberOfComponents(), phase.getTemperature(), phase.getPressure());
    }

    public void setStructure(int structure) {
        this.hydrateStructure = structure;
    }

    public double fugcoef(PhaseInterface phase, int numberOfComps, double temp, double pres) {

        if (componentName.equals("water")) {
            double val = 1.0;
            double tempy = 1.0;
            double fugold = 0.0;
            do {
                val = 0.0;
                tempy = 0.0;
                fugold = fugasityCoeffisient;
                for (int cavType = 0; cavType < 2; cavType++) {
                    tempy = 0.0;
                    for (int j = 0; j < phase.getNumberOfComponents(); j++) {
                        //System.out.println(phase.getComponent(j));
                        tempy += ((ComponentHydrateKluda) phase.getComponent(j)).calcYKI(hydrateStructure, cavType, phase);
                        logger.info("tempny " + tempy);
                        //System.out.println("temp ny " + this);//phase.getComponent(j));
                    }
                    val += cavprwat[hydrateStructure][cavType] * Math.log(1.0 - tempy);
                }
                logger.info("val " + (val));
                logger.info("fugasityCoeffisient bef " + fugasityCoeffisient);
                double solvol = 1.0 / 906.0 * getMolarMass();
                fugasityCoeffisient = Math.exp(val) * getEmptyHydrateStructureVapourPressure(hydrateStructure, temp) * Math.exp(solvol / (R * temp) * ((pres - getEmptyHydrateStructureVapourPressure(hydrateStructure, temp))) * 1e5) / pres;
                //fugasityCoeffisient = getAntoineVaporPressure(temp)/pres;
                //logFugasityCoeffisient = Math.log(fugasityCoeffisient);
                //logFugasityCoeffisient += val*boltzmannConstant/R;

                //fugasityCoeffisient = Math.exp(logFugasityCoeffisient);
                logger.info("fugasityCoeffisient " + fugasityCoeffisient);
            } while (Math.abs((fugasityCoeffisient - fugold) / fugold) > 1e-8);
        } else {
            fugasityCoeffisient = 1e5;
        }
        //System.out.println("fug " + fugasityCoeffisient);
        return fugasityCoeffisient;
    }

    public double dfugdt(PhaseInterface phase, int numberOfComps, double temp, double pres) {
        if (componentName.equals("water")) {
            double solvol = 1.0 / getPureComponentSolidDensity(getMeltingPointTemperature()) * molarMass;
            dfugdt = Math.log((getEmptyHydrateStructureVapourPressuredT(hydrateStructure, temp)) / pres);
        } else {
            dfugdt = 0;
        }
        return dfugdt;
    }

    public double getEmptyHydrateStructureVapourPressure(int type, double temperature) {
        double par1_struc1 = 4.6477;
        double par2_struc1 = -5242.979;
        double par3_struc1 = 2.7789;
        double par4_struc1 = -8.7156e-3;
        if (type == 0) {
            return Math.exp(par1_struc1 * Math.log(temperature) + par2_struc1 / temperature + par3_struc1 + par4_struc1 * temperature) / 1.0e5;
        }
        if (type == 1) {
            return Math.exp(par1_struc2 + par2_struc2 / temperature) * 1.01325;
        } else {
            return 0.0;
        }
    }

    public double getEmptyHydrateStructureVapourPressuredT(int type, double temperature) {

        if (type == 0) {
            return -par2_struc1 / (temperature * temperature) * Math.exp(par1_struc1 + par2_struc1 / temperature);
        }
        if (type == 1) {
            return -par2_struc2 / (temperature * temperature) * Math.exp(par1_struc2 + par2_struc2 / temperature);
        } else {
            return 0.0;
        }
    }

    public double calcYKI(int stucture, int cavityType, PhaseInterface phase) {
        if (componentName.equals("methane")) {
            double yki = calcCKI(stucture, cavityType, phase) * reffug[componentNumber];
            double temp = 1.0;
            for (int i = 0; i < phase.getNumberOfComponents(); i++) {
                if (phase.getComponent(i).isHydrateFormer()) {
                    temp += ((ComponentHydrateKluda) phase.getComponent(i)).calcCKI(stucture, cavityType, phase) * reffug[i];
                }

            }
            return yki / temp;
        } else {
            return 0.0;
        }
    }

    public double calcCKI(int stucture, int cavityType, PhaseInterface phase) {
        double cki = 4.0 * pi / (boltzmannConstant * phase.getTemperature()) * (potIntegral(0, stucture, cavityType, phase));//+0*potIntegral(1,stucture, cavityType,phase)+0*potIntegral(2,stucture, cavityType,phase));
        //System.out.println("cki " + cki);
        return cki;
    }

    public void setRefFug(int compNumbm, double val) {
        reffug[compNumbm] = val;
    }

    public double potIntegral(int intnumb, int stucture, int cavityType, PhaseInterface phase) {
        double val = 0.0;
        double endval = cavRadius[intnumb][stucture][cavityType] - getSphericalCoreRadius();
        double x = 0.0, step = endval / 100.0;
        x = step;
        for (int i = 1; i < 100; i++) {
            //System.out.println("x" +x);
            //System.out.println("pot " + getPot(x,stucture,cavityType,phase));
            val += step * ((getPot(intnumb, x, stucture, cavityType, phase) + 4 * getPot(intnumb, (x + 0.5 * step), stucture, cavityType, phase) + getPot(intnumb, x + step, stucture, cavityType, phase)) / 6.0);
            x = i * step;
        }
        return val / 100000.0;
    }

    public double getPot(int intnumb, double radius, int struccture, int cavityType, PhaseInterface phase) {
        double lenjonsenergy = Math.sqrt(this.getLennardJonesEnergyParameter() * phase.getComponent("water").getLennardJonesEnergyParameter());
        double diam = (this.getLennardJonesMolecularDiameter() + phase.getComponent("water").getLennardJonesMolecularDiameter()) / 2.0;
        double corerad = (this.getSphericalCoreRadius() + phase.getComponent("water").getSphericalCoreRadius()) / 2.0;

        double pot = 2.0 * coordNumb[intnumb][struccture][cavityType] * lenjonsenergy * ((Math.pow(diam, 12.0) / (Math.pow(cavRadius[intnumb][struccture][cavityType], 11.0) * radius) * (delt(intnumb, 10.0, radius, struccture, cavityType, phase) + corerad / cavRadius[intnumb][struccture][cavityType] * delt(intnumb, 11.0, radius, struccture, cavityType, phase))) -
                (Math.pow(diam, 6.0) / (Math.pow(cavRadius[intnumb][struccture][cavityType], 5.0) * radius) * (delt(intnumb, 4.0, radius, struccture, cavityType, phase) + corerad / cavRadius[intnumb][struccture][cavityType] * delt(intnumb, 5.0, radius, struccture, cavityType, phase))));
//        
//        intnumb++;
//        pot += 2.0*coordNumb[intnumb][struccture][cavityType]*lenjonsenergy*(
//        (Math.pow(diam,12.0)/(Math.pow(cavRadius[intnumb][struccture][cavityType],11.0)* radius)*(delt(intnumb,10.0,radius,struccture,cavityType,phase)+corerad/cavRadius[intnumb][struccture][cavityType]*delt(intnumb,11.0,radius,struccture,cavityType,phase)))
//        -
//        (Math.pow(diam,6.0)/(Math.pow(cavRadius[intnumb][struccture][cavityType],5.0)* radius)*(delt(intnumb, 4.0,radius,struccture,cavityType,phase)+corerad/cavRadius[intnumb][struccture][cavityType]*delt(intnumb,5.0,radius,struccture,cavityType,phase)))
//        );
//        
//        intnumb++;
//        pot += 2.0*coordNumb[intnumb][struccture][cavityType]*lenjonsenergy*(
//        (Math.pow(diam,12.0)/(Math.pow(cavRadius[intnumb][struccture][cavityType],11.0)* radius)*(delt(intnumb,10.0,radius,struccture,cavityType,phase)+corerad/cavRadius[intnumb][struccture][cavityType]*delt(intnumb,11.0,radius,struccture,cavityType,phase)))
//        -
//        (Math.pow(diam,6.0)/(Math.pow(cavRadius[intnumb][struccture][cavityType],5.0)* radius)*(delt(intnumb, 4.0,radius,struccture,cavityType,phase)+corerad/cavRadius[intnumb][struccture][cavityType]*delt(intnumb,5.0,radius,struccture,cavityType,phase)))
//        );

        //System.out.println("lenjones " +this.getLennardJonesMolecularDiameter() );
        //System.out.println("pot bef " + pot);
        pot = Math.exp(-pot / (phase.getTemperature())) * radius * radius / 1.0e20;
        //System.out.println("pot " + pot);
        return pot;
    }

    public double delt(int intnumb, double n, double radius, int struccture, int cavityType, PhaseInterface phase) {
        double lenjonsenergy = Math.sqrt(this.getLennardJonesEnergyParameter() * phase.getComponent("water").getLennardJonesEnergyParameter());
        double diam = (this.getLennardJonesMolecularDiameter() + phase.getComponent("water").getLennardJonesMolecularDiameter()) / 2.0;
        double corerad = (this.getSphericalCoreRadius() + phase.getComponent("water").getSphericalCoreRadius()) / 2.0;

        double delt = 1.0 / n * (Math.pow(1.0 - radius / cavRadius[intnumb][struccture][cavityType] - corerad / cavRadius[intnumb][struccture][cavityType], -n) -
                Math.pow(1.0 + radius / cavRadius[intnumb][struccture][cavityType] - corerad / cavRadius[intnumb][struccture][cavityType], -n));

        //System.out.println("delt " + delt);
        return delt;
    }
}
