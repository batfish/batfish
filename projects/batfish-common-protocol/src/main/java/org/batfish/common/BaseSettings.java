package org.batfish.common;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public abstract class BaseSettings {

  private static final int HELP_WIDTH = 80;

  private static Configuration loadFileConfiguration(File configFile) {
    try {
      return new Configurations().properties(configFile);
    } catch (ConfigurationException e) {
      throw new BatfishException(
          "Error loading configuration from " + configFile.getAbsolutePath(), e);
    }
  }

  protected final Configuration _config;

  private CommandLine _line;

  private final Options _options;

  /**
   * Initialize settings from an existing configuration
   *
   * @param config {@link Configuration} containing the desired settings
   */
  public BaseSettings(Configuration config) {
    _options = new Options();
    _config = ConfigurationUtils.cloneConfiguration(config);
  }

  /**
   * Initialize settings from a Java properties file
   *
   * @param configFile configuration file
   */
  public BaseSettings(Path configFile) {
    this(loadFileConfiguration(configFile.toFile()));
  }

  protected final void addBooleanOption(String key, String description) {
    _options.addOption(
        Option.builder()
            .argName("true|false")
            .hasArg()
            .optionalArg(true)
            .desc(description)
            .longOpt(key)
            .build());
  }

  protected final void addListOption(String key, String description, String argName) {
    _options.addOption(
        Option.builder()
            .argName(argName)
            .hasArgs()
            .valueSeparator(',')
            .desc(description)
            .longOpt(key)
            .build());
  }

  protected final void addOption(String key, String description, String argName) {
    _options.addOption(
        Option.builder().argName(argName).hasArg().desc(description).longOpt(key).build());
  }

  protected final boolean getBooleanOptionValue(String key) {
    if (_line.hasOption(key)) {
      String value = _line.getOptionValue(key);
      boolean b;
      if (value == null || value.trim().equalsIgnoreCase("true")) {
        b = true;
      } else if (value.trim().equalsIgnoreCase("false")) {
        b = false;
      } else {
        throw new CleanBatfishException(
            "Error parsing command line: Invalid boolean value: \"" + value + "\"");
      }
      _config.setProperty(key, b);
    }
    return _config.getBoolean(key);
  }

  public Configuration getConfiguration() {
    return _config;
  }

  protected final Integer getIntegerOptionValue(String key) {
    String valueStr = _line.getOptionValue(key);
    if (valueStr == null) {
      return _config.getInteger(key, null);
    } else {
      return Integer.parseInt(valueStr);
    }
  }

  /**
   * Get integer value of an integer setting from the command line (or return default).
   *
   * @param key argument name
   */
  protected final int getIntOptionValue(String key) {
    String valueStr = _line.getOptionValue(key);
    if (valueStr != null) {
      _config.setProperty(key, Integer.parseInt(valueStr));
    }
    return _config.getInt(key);
  }

  protected final Long getLongOptionValue(String key) {
    String valueStr = _line.getOptionValue(key);
    if (valueStr != null) {
      return Long.parseLong(valueStr);
    }
    return _config.getLong(key, null);
  }

  protected final List<Path> getPathListOptionValue(String key) {
    if (_line.hasOption(key)) {
      String[] optionValues = _line.getOptionValues(key);
      if (optionValues != null) {
        _config.setProperty(
            key,
            Arrays.stream(optionValues)
                .map(BaseSettings::nullablePath)
                .filter(Objects::nonNull)
                .map(Path::toString)
                .toArray());
      }
    }
    String[] s = _config.getStringArray(key);
    return s == null
        ? Collections.emptyList()
        : Arrays.stream(s).map(Paths::get).collect(Collectors.toList());
  }

  @Nullable
  protected final Path getPathOptionValue(String key) {
    String valueStr = _line.getOptionValue(key);
    if (valueStr != null) {
      _config.setProperty(key, valueStr);
    }
    return nullablePath(_config.getString(key));
  }

  protected final List<String> getStringListOptionValue(String key) {
    if (_line.hasOption(key)) {
      String[] optionValues = _line.getOptionValues(key);
      if (optionValues == null) {
        _config.setProperty(key, Collections.emptyList());
      } else {
        _config.setProperty(key, Arrays.asList(optionValues));
      }
    }
    return _config.getList(String.class, key);
  }

  protected final String getStringOptionValue(String key) {
    String valueStr = _line.getOptionValue(key);
    if (valueStr != null) {
      _config.setProperty(key, valueStr);
    }
    return _config.getString(key);
  }

  protected final void initCommandLine(String[] args) {
    CommandLineParser parser = new DefaultParser();

    // parse the command line arguments
    try {
      _line = parser.parse(_options, args);
    } catch (ParseException e) {
      throw new BatfishException("Could not parse command line", e);
    }
  }

  @Nullable
  protected static Path nullablePath(String s) {
    return (s != null) ? Paths.get(s) : null;
  }

  protected final void printHelp(String executableName) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setLongOptPrefix("-");
    formatter.setWidth(HELP_WIDTH);
    formatter.printHelp(executableName, _options);
  }

  protected final void setDefaultProperty(String key, @Nullable Object value) {
    if (_config.getProperty(key) == null) {
      _config.setProperty(key, value);
    }
  }
}
