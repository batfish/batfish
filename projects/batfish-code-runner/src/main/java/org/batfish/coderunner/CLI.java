package org.batfish.coderunner;

import static org.batfish.main.Batfish.flatten;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishParseException;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.vendor.VendorConfiguration;

public class CLI {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: <snapshot-dir> <code-file>");
      System.exit(1);
    }

    String snapshotDir = args[0];
    String codeFile = args[1];

    try {
      String code = Files.readString(Paths.get(codeFile));
      Map<String, DeviceData> devices = parseSnapshot(snapshotDir);
      BatfishCodeRunner runner = new BatfishCodeRunner();

      System.out.println("hostname,result");
      for (Map.Entry<String, DeviceData> entry : devices.entrySet()) {
        String hostname = entry.getKey();
        DeviceData data = entry.getValue();
        try {
          String result =
              runner.runCode(
                  data.viConfig, data.vendorConfig, data.originalText, data.processedText, code);
          System.out.println(escapeCsv(hostname) + "," + escapeCsv(result));
        } catch (Exception e) {
          System.err.println("Error processing " + hostname + ": " + e.getMessage());
        }
      }
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  private static Map<String, DeviceData> parseSnapshot(String snapshotDir) throws IOException {
    Path configsDir = Paths.get(snapshotDir, "configs");
    if (!Files.isDirectory(configsDir)) {
      throw new IllegalArgumentException("No configs directory found in snapshot");
    }

    Settings settings = new Settings(new String[] {});
    BatfishLogger logger = new BatfishLogger("output", false);
    settings.setLogger(logger);

    Map<String, DeviceData> devices = new TreeMap<>();

    try (Stream<Path> paths = Files.list(configsDir)) {
      paths
          .filter(Files::isRegularFile)
          .forEach(
              path -> {
                try {
                  String filename = path.toString();
                  String originalText = Files.readString(path);

                  Warnings warnings = new Warnings(true, true, true);
                  ConfigurationFormat format =
                      VendorConfigurationFormatDetector.identifyConfigurationFormat(originalText);

                  String processedText = originalText;
                  if (format == ConfigurationFormat.JUNIPER
                      || format == ConfigurationFormat.PALO_ALTO_NESTED
                      || format == ConfigurationFormat.VYOS) {
                    try {
                      processedText =
                          flatten(originalText, logger, settings, warnings, format, "")
                              .getFlattenedConfigurationText();
                    } catch (BatfishParseException e) {
                      processedText = originalText;
                    }
                  }

                  ParseVendorConfigurationJob job =
                      new ParseVendorConfigurationJob(
                          settings,
                          null,
                          ImmutableMap.of(filename, originalText),
                          Warnings.Settings.fromLogger(logger),
                          format,
                          null);
                  ParseVendorConfigurationResult result = job.call();

                  if (result.getVendorConfiguration() != null) {
                    VendorConfiguration vc = result.getVendorConfiguration();
                    vc.setWarnings(warnings);
                    List<Configuration> viConfigs = vc.toVendorIndependentConfigurations();
                    if (!viConfigs.isEmpty()) {
                      Configuration viConfig = viConfigs.get(0);
                      devices.put(
                          viConfig.getHostname(),
                          new DeviceData(viConfig, vc, originalText, processedText));
                    }
                  }
                } catch (Exception e) {
                  System.err.println(
                      "Failed to parse " + path.getFileName() + ": " + e.getMessage());
                }
              });
    }

    return devices;
  }

  private static class DeviceData {
    final Configuration viConfig;
    final VendorConfiguration vendorConfig;
    final String originalText;
    final String processedText;

    DeviceData(
        Configuration viConfig,
        VendorConfiguration vendorConfig,
        String originalText,
        String processedText) {
      this.viConfig = viConfig;
      this.vendorConfig = vendorConfig;
      this.originalText = originalText;
      this.processedText = processedText;
    }
  }
}
