package org.batfish.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

public abstract class BaseSettings {

   private static final int HELP_WIDTH = 80;

   protected final FileConfiguration _config;

   private CommandLine _line;

   protected final Options _options;

   public BaseSettings(Path configFile) {
      _options = new Options();
      _config = new PropertiesConfiguration();
      _config.setFile(configFile.toFile());
      try {
         _config.load();
      }
      catch (ConfigurationException e) {
         throw new BatfishException("Error loading configuration", e);
      }
   }

   protected final void addBooleanOption(String key, String description) {
      _options.addOption(Option.builder().argName("true|false").hasArg()
            .optionalArg(true).desc(description).longOpt(key).build());
   }

   protected final void addListOption(String key, String description,
         String argName) {
      _options.addOption(Option.builder().argName(argName).hasArgs()
            .valueSeparator(',').desc(description).longOpt(key).build());
   }

   protected final void addOption(String key, String description,
         String argName) {
      _options.addOption(Option.builder().argName(argName).hasArg()
            .desc(description).longOpt(key).build());
   }

   protected final boolean getBooleanOptionValue(String key) {
      if (_line.hasOption(key)) {
         String value = _line.getOptionValue(key);
         if (value == null || value.compareToIgnoreCase("true") == 0) {
            return true;
         }
         else if (value.compareToIgnoreCase("false") == 0) {
            return false;
         }
         else {
            throw new CleanBatfishException(
                  "Error parsing command line: Invalid boolean value: \""
                        + value + "\"");
         }
      }
      else {
         return _config.getBoolean(key);
      }
   }

   protected final Integer getIntegerOptionValue(String key) {
      String valueStr = _line.getOptionValue(key);
      if (valueStr == null) {
         return _config.getInteger(key, null);
      }
      else {
         return Integer.parseInt(valueStr);
      }
   }

   protected final int getIntOptionValue(String key) {
      String valueStr = _line.getOptionValue(key);
      if (valueStr == null) {
         return _config.getInt(key);
      }
      else {
         return Integer.parseInt(valueStr);
      }
   }

   protected final Long getLongOptionValue(String key) {
      String valueStr = _line.getOptionValue(key);
      if (valueStr == null) {
         return _config.getLong(key, null);
      }
      else {
         return Long.parseLong(valueStr);
      }
   }

   protected final List<Path> getPathListOptionValue(String key) {
      if (_line.hasOption(key)) {
         String[] optionValues = _line.getOptionValues(key);
         if (optionValues == null) {
            return Collections.<Path> emptyList();
         }
         else {
            return Arrays.stream(optionValues)
                  .map(optionValue -> nullablePath(optionValue))
                  .collect(Collectors.toList());
         }
      }
      else {
         return Arrays.stream(_config.getStringArray(key))
               .map(optionValue -> nullablePath(optionValue))
               .collect(Collectors.toList());
      }
   }

   protected final Path getPathOptionValue(String key) {
      String value = _line.getOptionValue(key, _config.getString(key));
      return nullablePath(value);
   }

   protected final List<String> getStringListOptionValue(String key) {
      if (_line.hasOption(key)) {
         String[] optionValues = _line.getOptionValues(key);
         if (optionValues == null) {
            return Collections.<String> emptyList();
         }
         else {
            return Arrays.asList(optionValues);
         }
      }
      else {
         return Arrays.asList(_config.getStringArray(key));
      }
   }

   protected final String getStringOptionValue(String key) {
      String value = _line.getOptionValue(key, _config.getString(key));
      return value;
   }

   protected final void initCommandLine(String[] args) {
      CommandLineParser parser = new DefaultParser();

      // parse the command line arguments
      try {
         _line = parser.parse(_options, args);
      }
      catch (ParseException e) {
         throw new BatfishException("Could not parse command line", e);
      }

   }

   private final Path nullablePath(String s) {
      return (s != null) ? Paths.get(s) : null;
   }

   protected final void printHelp(String executableName) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.setLongOptPrefix("-");
      formatter.setWidth(HELP_WIDTH);
      formatter.printHelp(executableName, _options);
   }

   protected final void setDefaultProperty(String key, Object value) {
      if (_config.getProperty(key) == null) {
         _config.setProperty(key, value);
      }
   }

}
