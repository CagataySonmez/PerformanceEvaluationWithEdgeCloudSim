/*
 * Title:        EdgeCloudSim - Sample Application
 * 
 * Description:  Sample application for Vehicular App
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.applications.cmpe523;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

public class Cmpe523MainApp {

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		//disable console output of cloudsim library
		Log.disable();

		//enable console output and file output of this application
		SimLogger.enablePrintLog();

		String configFile = "";
		String outputFolder = "";
		String edgeDevicesFile = "";
		String applicationsFile = "";
		if (args.length == 5){
			configFile = args[0];
			edgeDevicesFile = args[1];
			applicationsFile = args[2];
			outputFolder = args[3];
			int iterationNumber = Integer.parseInt(args[4]);
			
			//load settings from configuration file
			loadSettings(configFile, edgeDevicesFile, applicationsFile);
			
			mainHelper(outputFolder,  iterationNumber, true);
		}
		else{
			SimLogger.printLine("Simulation setting file, output folder and iteration number are not provided! Using default ones...");

			int numOfIteration=25;
			
			for(int s=1; s<=3; s++){
				configFile = "scripts/cmpe523/config/default_config" + s + ".properties";
				applicationsFile = "scripts/cmpe523/config/applications" + s + ".xml";
				edgeDevicesFile = "scripts/cmpe523/config/edge_devices" + s + ".xml";

				//load settings from configuration file
				loadSettings(configFile, edgeDevicesFile, applicationsFile);

				for(int i=1; i<=numOfIteration; i++){
					outputFolder = "sim_results/ite" + i;
					mainHelper(outputFolder,  i, s == 1);
				}
			}
		}
	}
	
	private static void loadSettings(String configFile, String edgeDevicesFile, String applicationsFile){
		SimSettings.initialize();
		SimSettings SS = SimSettings.getInstance();
		if(SS.initialize(configFile, edgeDevicesFile, applicationsFile) == false) {
			SimLogger.printLine("cannot initialize simulation settings!");
			System.exit(1);
		}
	}

	private static void mainHelper(String outputFolder,  int iterationNumber, Boolean cleanFolder){
		SimSettings SS = SimSettings.getInstance();

		if(SS.getFileLoggingEnabled())
			SimLogger.enableFileLog();
		
		if(cleanFolder)
			SimUtils.cleanOutputFolder(outputFolder);

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date SimulationStartDate = Calendar.getInstance().getTime();
		SimLogger.printLine("Simulation started at " + df.format(SimulationStartDate));
		SimLogger.printLine("----------------------------------------------------------------------");
		
		for(int i=SS.getMinNumOfMobileDev(); i<=SS.getMaxNumOfMobileDev(); i+=SS.getMobileDevCounterSize()){
			for(int s=0; s<SS.getSimulationScenarios().length; s++){
				for(int p=0; p<SS.getOrchestratorPolicies().length; p++){
					Date ScenarioStartDate = Calendar.getInstance().getTime();
			
					SimLogger.printLine("Scenario started at " + df.format(ScenarioStartDate));
					SimLogger.printLine("Scenario: " + SS.getSimulationScenarios()[s] + " - Policy: " + SS.getOrchestratorPolicies()[p] + " - #iteration: " + iterationNumber);
					SimLogger.printLine("Duration: " + SS.getSimulationTime()/60 + " min (warm up period: "+ SS.getWarmUpPeriod()/60 +" min) - #devices: " + i);
					SimLogger.getInstance().simStarted(outputFolder, "SIMRESULT_" + SS.getSimulationScenarios()[s] + "_"  + SS.getOrchestratorPolicies()[p] + "_" + i + "DEVICES");
			
					try
					{
						// First step: Initialize the CloudSim package. It should be called
						// before creating any entities.
						int num_user = 2;   // number of grid users
						Calendar calendar = Calendar.getInstance();
						boolean trace_flag = false;  // mean trace events
			
						// Initialize the CloudSim library
						CloudSim.init(num_user, calendar, trace_flag, 0.01);
			
						// Generate EdgeCloudsim Scenario Factory
						ScenarioFactory scenarioFactory = new Cmpe523ScenarioFactory(i, SS.getSimulationTime(), SS.getOrchestratorPolicies()[p], SS.getSimulationScenarios()[s]);
			
						// Generate EdgeCloudSim Simulation Manager
						SimManager manager = new SimManager(scenarioFactory, i, SS.getSimulationScenarios()[s], SS.getOrchestratorPolicies()[p]);
			
						// Start simulation
						manager.startSimulation();
					}
					catch (Exception e)
					{
						SimLogger.printLine("The simulation has been terminated due to an unexpected error");
						e.printStackTrace();
						System.exit(1);
					}
			
					Date ScenarioEndDate = Calendar.getInstance().getTime();
					SimLogger.printLine("Scenario finished at " + df.format(ScenarioEndDate) +  ". It took " + SimUtils.getTimeDifference(ScenarioStartDate,ScenarioEndDate));
					SimLogger.printLine("----------------------------------------------------------------------");
			
					//suggest garbage collector to run in order to decrease heap memory
					System.gc();
				}
			}
		}
		
		Date SimulationEndDate = Calendar.getInstance().getTime();
		SimLogger.printLine("Simulation finished at " + df.format(SimulationEndDate) +  ". It took " + SimUtils.getTimeDifference(SimulationStartDate,SimulationEndDate));
	}//End of scenarios loop
}
