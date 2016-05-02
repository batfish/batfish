package org.batfish.allinone;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
			if (_settings.getTestrigDir() == null) {
				System.err.println("org.batfish.client: Testrig directory not specified");
				System.exit(1);
			}

			_logger = new BatfishLogger(_settings.getLogLevel(), false,
					_settings.getLogFile(), false, true);

			runCoordinator();
			runBatfish();
			
			_client = runClient();
		}
		catch (Exception e) {
			System.err.println("org.batfish.allinone: Initialization failed: "
					+ e.getMessage());
			System.exit(1);
		}

		doWork();
	}

	private static void doWork() {

		List<String> commands = getCommands();

		String initTestrigCommand = "init-testrig -nodataplane " 
					+ _settings.getTestrigDir();

		commands.add(0, initTestrigCommand);
		
		_client.runBatchMode(commands);
	}

	private static String[] getArgArrayFromString(String argString) {
		if (argString == null)
			return new String[0];
		
		return argString.split(" ");	
	}
	
	private static List<String> getCommands() {
		
		List<String> commands = null;
		
		if (_settings.getCommandFile() != null) {
			try {
				commands = Files.readAllLines(Paths.get(_settings.getCommandFile()));
			} catch (IOException e) {
				_logger.errorf("Could not read command file %s: %s\n", 
						_settings.getCommandFile(), e.getMessage());
				System.exit(1);
			}
		}
		else {
			commands = new LinkedList<String>();
			commands.add("echo hallelujah");
			commands.add("prompt");
			commands.add("echo hallelujah2");
		}
		return commands;		
	}

	private static void runBatfish() {

		String[] argArray = getArgArrayFromString(_settings.getBatfishArgs());
		_logger.debugf("Starting batfish worker with args: %s\n",
				Arrays.toString(argArray));
		
		Thread thread = new Thread("batfishThread") {
			public void run() {
				org.batfish.main.Driver.main(argArray);
			}
		};

		thread.start();
	}

	private static Client runClient() throws Exception {
		String[] argArray = getArgArrayFromString(_settings.getClientArgs());
		_logger.debugf("Starting batfish client with args: %s\n",
				Arrays.toString(argArray));		
		return new Client(argArray);
	}

	private static void runCoordinator() {

		String[] argArray = getArgArrayFromString(_settings.getCoordinatorArgs());
		_logger.debugf("Starting coordinator with args: %s\n" , 
				Arrays.toString(argArray));		

		Thread thread = new Thread("coordinatorThread") {
			public void run() {
				org.batfish.coordinator.Main.main(argArray);
			}
		};

		thread.start();
	}

}
