package neqsim.processSimulation.util.example;

import java.io.ObjectInputStream.GetField;

import neqsim.processSimulation.processEquipment.absorber.SimpleTEGAbsorber;
import neqsim.processSimulation.processEquipment.absorber.WaterStripperColumn;
import neqsim.processSimulation.processEquipment.distillation.Condenser;
import neqsim.processSimulation.processEquipment.distillation.DistillationColumn;
import neqsim.processSimulation.processEquipment.distillation.Reboiler;
import neqsim.processSimulation.processEquipment.heatExchanger.Cooler;
import neqsim.processSimulation.processEquipment.heatExchanger.Heater;
import neqsim.processSimulation.processEquipment.mixer.StaticMixer;
import neqsim.processSimulation.processEquipment.mixer.StaticNeqMixer;
import neqsim.processSimulation.processEquipment.mixer.StaticPhaseMixer;
import neqsim.processSimulation.processEquipment.pump.Pump;
import neqsim.processSimulation.processEquipment.stream.EnergyStream;
import neqsim.processSimulation.processEquipment.stream.Stream;
import neqsim.processSimulation.processEquipment.util.Calculator;
import neqsim.processSimulation.processEquipment.util.Recycle;
import neqsim.processSimulation.processEquipment.util.StreamSaturatorUtil;
import neqsim.processSimulation.processEquipment.valve.ThrottlingValve;
import neqsim.processSimulation.processSystem.ProcessSystem;
import neqsim.thermodynamicOperations.flashOps.SaturateWithWater;
import neqsim.processSimulation.processEquipment.separator.GasScrubber;
import neqsim.processSimulation.processEquipment.separator.Separator;

public class MasstransferMeOH {

	public static void main(String[] args) {

		// Create the input fluid to the TEG process and saturate it with water at
		// scrubber conditions
		neqsim.thermo.system.SystemInterface feedGas = new neqsim.thermo.system.SystemSrkCPAstatoil(273.15 + 42.0,
				10.00);
		
		feedGas.addComponent("methane", 83.88);
		feedGas.addComponent("water", 0.0);
		feedGas.addComponent("methanol", 0);
		feedGas.createDatabase(true);
		feedGas.setMixingRule(10);
		feedGas.setMultiPhaseCheck(true);

		Stream dryFeedGas = new Stream("dry feed gas", feedGas);
		dryFeedGas.setFlowRate(1.23, "MSm3/day");
		dryFeedGas.setTemperature(10.4, "C");
		dryFeedGas.setPressure(52.21, "bara");
		
		StreamSaturatorUtil saturatedFeedGas = new StreamSaturatorUtil(dryFeedGas);
		saturatedFeedGas.setName("water saturator");
		
		Stream waterSaturatedFeedGas = new Stream(saturatedFeedGas.getOutStream());
		waterSaturatedFeedGas.setName("water saturated feed gas");
		
		neqsim.thermo.system.SystemInterface feedMeOH = (neqsim.thermo.system.SystemInterface) feedGas.clone();
		feedMeOH.setMolarComposition(new double[] { 0.0, 0.0, 1.0});

		Stream MeOHFeed = new Stream("lean TEG to absorber", feedMeOH);
		MeOHFeed.setFlowRate(680.5, "kg/hr");
		MeOHFeed.setTemperature(10.4, "C");
		MeOHFeed.setPressure(52.21, "bara");

		StaticMixer mainMixer = new StaticPhaseMixer("gas MeOH mixer");
		mainMixer.addStream(waterSaturatedFeedGas);
		mainMixer.addStream(MeOHFeed);
		
		neqsim.processSimulation.processEquipment.util.NeqSimUnit pipeline = new neqsim.processSimulation.processEquipment.util.NeqSimUnit(mainMixer.getOutStream(), "pipeline", "stratified");
		pipeline.setLength(123.01);
		
		GasScrubber scrubber = new GasScrubber("gas scrub",pipeline.getOutStream());
		
		Stream gasFromScrubber = new Stream(scrubber.getGasOutStream());

		neqsim.processSimulation.processSystem.ProcessSystem operations = new neqsim.processSimulation.processSystem.ProcessSystem();
		operations.add(dryFeedGas);
		operations.add(saturatedFeedGas);
		operations.add(waterSaturatedFeedGas);
		operations.add(MeOHFeed);
		operations.add(mainMixer);
		operations.add(pipeline);
		operations.add(pipeline);
		operations.add(scrubber);
		operations.add(gasFromScrubber);
		operations.run();
		//operations.run();
		
		operations.save("c:/temp/MeOhmasstrans.neqsim");
		///operations = ProcessSystem.open("c:/temp/TEGprocess.neqsim");
		//((DistillationColumn)operations.getUnit("TEG regeneration column")).setTopPressure(1.2);
		//operations.run();
		//((DistillationColumn)operations.getUnit("TEG regeneration column")).setNumberOfTrays(2);
		System.out.println("water in wet gas [kg/MSm3] " + ((Stream)operations.getUnit("water saturated feed gas")).getFluid().getPhase(0).getComponent("water").getz()*1.0e6*0.01802*101325.0/(8.314*288.15));
		//mainMixer.getFluid().display();
		//scrubber.getGasOutStream().displayResult();
		System.out.println("hydt " + gasFromScrubber.getHydrateEquilibriumTemperature());
		
	
	}
}