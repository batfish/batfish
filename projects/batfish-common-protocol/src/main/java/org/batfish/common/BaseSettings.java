package org.batfish.common;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public abstract class BaseSettings {
  protected static final String DEPRECATED_ARG_DESC =
      "(ignored, provided for backwards compatibility)";

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
    this(config, new Options());
  }

  /**
   * Initialize settings from an existing configuration
   *
   * @param config {@link Configuration} containing the desired settings
   * @param options {@link Options} containing the desired options
   */
  public BaseSettings(Configuration config, Options options) {
    _options = options;
    _config = ConfigurationUtils.cloneConfiguration(config);
  }

  protected static Configuration getConfig(
      String overridePropertyName,
      String defaultPropertyFilename,
      Class<?> defaultPropertyLocatorClass) {
    String overriddenPath = System.getProperty(overridePropertyName);
    URL propertiesUrl;
    if (overriddenPath != null) {
      // The user provided an override, so look up that configuration instead.
      try {
        propertiesUrl = new URL(new URL("file://"), overriddenPath);
      } catch (MalformedURLException e) {
        throw new BatfishException(
            "Error treating " + overriddenPath + " as a path to a properties file", e);
      }
    } else {
      // Find the default properties file.
      @SuppressWarnings("PMD.UseProperClassLoader") // intentional
      URL url = defaultPropertyLocatorClass.getClassLoader().getResource(defaultPropertyFilename);
      propertiesUrl = url;
    }
    try {
      return new Configurations().properties(propertiesUrl);
    } catch (Exception e) {
      throw new BatfishException("Error loading configuration from " + overriddenPath, e);
    }
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
      /*
       * We don't use Boolean#parseBoolean here because that does not do format detection,
       * and silently defaults to "false" which is not what we want.
       */
      if (value == null || value.equalsIgnoreCase("true")) {
        b = true;
      } else if (value.equalsIgnoreCase("false")) {
        b = false;
      } else {
        throw new CleanBatfishException(
            String.format(
                "Error parsing command line: Invalid boolean value for key \"%s\": \"%s\"",
                key, value));
      }
      _config.setProperty(key, b);
    }
    return _config.getBoolean(key);
  }

  protected final Integer getIntegerOptionValue(String key) {
    String valueStr = _line.getOptionValue(key);
    if (valueStr != null) {
      _config.setProperty(key, Integer.parseInt(valueStr));
    }
    return _config.getInteger(key, null);
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
                .collect(ImmutableList.toImmutableList()));
      }
    }
    List<String> s = _config.getList(String.class, key);
    return s == null
        ? ImmutableList.of()
        : s.stream().map(Paths::get).collect(ImmutableList.toImmutableList());
  }

  protected final @Nullable Path getPathOptionValue(String key) {
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
        _config.setProperty(key, ImmutableList.of());
      } else {
        _config.setProperty(key, ImmutableList.of(Arrays.asList(optionValues)));
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

  protected static @Nullable Path nullablePath(String s) {
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

  public ImmutableConfiguration getImmutableConfiguration() {
    return ConfigurationUtils.unmodifiableConfiguration(_config);
  }
}
