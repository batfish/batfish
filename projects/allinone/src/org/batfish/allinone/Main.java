package org.batfish.allinone;

import java.util.Arrays;
import java.util.LinkedList;

import org.batfish.allinone.Settings;
import org.batfish.client.Client;
import org.batfish.common.BatfishLogger;

public class Main {

	static Client _client;
	static BatfishLogger _logger;
	static Settings _settings = null;			      

	public static void main(String[] args) {
	   try {
	      _settings = new Settings(args);
	   }
	   catch (Exception e) {
	      System.err.println("org.batfish.allinone: Initialization failed: "
	            + e.getMessage());
	      System.exit(1);
	   }

	   String argString = String.format("%s -%s %s -%s %s -%s %s", 
			  _settings.getClientArgs(),			      
	         org.batfish.client.Settings.ARG_COORDINATOR_HOST, "localhost",
	         org.batfish.client.Settings.ARG_LOG_LEVEL, _settings.getLogLevel(),
	         org.batfish.client.Settings.ARG_RUN_MODE, _settings.getRunMode());

	   if (_settings.getLogFile() != null)
	      argString += String.format(" -%s %s",
	            org.batfish.client.Settings.ARG_LOG_FILE, _settings.getLogFile());

	   if (_settings.getCommandFile() != null)
	      argString += String.format(" -%s %s",
	            org.batfish.client.Settings.ARG_COMMAND_FILE, _settings.getCommandFile());

	   if (_settings.getTestrigDir() != null)
		      argString += String.format(" -%s %s",
		            org.batfish.client.Settings.ARG_TESTRIG_DIR, _settings.getTestrigDir());
	   
	   String[] argArray = getArgArrayFromString(argString);
			
	   try {
	      _client = new Client(argArray);
	      _logger = _client.getLogger();
	      _logger.debugf("Started client with args: %s\n",
	            Arrays.toString(argArray));      
	   }
	   catch (Exception e) {
	      System.err.printf("Client initialization failed with args: %s\nExceptionMessage: %s\n",
	            argString, e.getMessage());
	      System.exit(1);
	   }

	   runCoordinator();
	   runBatfish();

		_client.run(new LinkedList<String>());
		
		//The program does not terminate without it in case the user misses the quit command
		System.exit(0);
	}

	private static String[] getArgArrayFromString(String argString) {
		 if (argString == null || argString == "")
			return new String[0];		
		 return argString.trim().split("\\s+");	
	}
	
	private static void runBatfish() {

	   String batfishArgs = String.format("%s -%s -%s %s -%s %s", _settings.getBatfishArgs(), 
	         org.batfish.main.Settings.ARG_SERVICE_MODE, 
	         org.batfish.main.Settings.ARG_COORDINATOR_REGISTER, "true",
	         org.batfish.main.Settings.ARG_COORDINATOR_HOST, "localhost");
	         
		final String[] argArray = getArgArrayFromString(batfishArgs);
		_logger.debugf("Starting batfish worker with args: %s\n",
				Arrays.toString(argArray));
		
		Thread thread = new Thread("batfishThread") {
			public void run() {
			   try {
			      org.batfish.main.Driver.main(argArray, _logger);
            }
            catch (Exception e) {
               _logger.errorf("Initialization of batfish failed with args: %s\nExceptionMessage: %s\n",
                     Arrays.toString(argArray), e.getMessage());
            }
			}
		};

		thread.start();
	}

	private static void runCoordinator() {

		final String[] argArray = getArgArrayFromString(_settings.getCoordinatorArgs());
		_logger.debugf("Starting coordinator with args: %s\n" , 
				Arrays.toString(argArray));		

		Thread thread = new Thread("coordinatorThread") {
			public void run() {
			   try {
			      org.batfish.coordinator.Main.main(argArray, _logger);
			   }
			   catch (Exception e) {
			      _logger.errorf("Initialization of coordinator failed with args: %s\nExceptionMessage: %s\n",
			            Arrays.toString(argArray), e.getMessage());
			   }
			}
		};

		thread.start();
	}

}
