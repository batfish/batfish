package org.batfish.vendor.sonic.representation;

import static org.batfish.common.BfConsts.RELPATH_SONIC_CONFIGDB_DIR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.batfish.common.Warning;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.vendor.VendorSupplementalInformation;
import org.batfish.vendor.sonic.representation.ConfigDb.Data;

/** Represents information in Sonic configdb.json files, for all nodes */
public class SonicConfigDbs implements VendorSupplementalInformation {

  /** Returns the {@link ConfigDb} object for the host. */
  public Optional<ConfigDb> getHostConfigDb(String hostname) {
    return Optional.ofNullable(_configDbs.get(hostname));
  }

  public SonicConfigDbs(Map<String, ConfigDb> configDbs) {
    _configDbs = ImmutableMap.copyOf(configDbs);
  }

  public static SonicConfigDbs readSonicConfigDbs(
      Map<String, String> sonicCofigDbData, ParseVendorConfigurationAnswerElement pvcae) {
    // deserialize all files in parallel
    List<ConfigDb> configDbs =
        sonicCofigDbData.entrySet().parallelStream()
            .map(
                e -> {
                  try {
                    ConfigDb configDb =
                        new ConfigDb(
                            e.getKey(),
                            BatfishObjectMapper.mapper().readValue(e.getValue(), Data.class));
                    if (!configDb.getHostname().isPresent()) {
                      pvcae.addRedFlagWarning(
                          e.getKey(), new Warning("Missing hostname", "SonicConfigDb"));
                    }
                    return configDb;
                  } catch (JsonProcessingException exception) {
                    pvcae.addRedFlagWarning(
                        e.getKey(),
                        new Warning(exception.getMessage(), RELPATH_SONIC_CONFIGDB_DIR));
                    return new ConfigDb(e.getKey(), new Data(ImmutableMap.of()));
                  }
                })
            .filter(configDb -> configDb.getHostname().isPresent())
            .collect(ImmutableList.toImmutableList());

    // Detect any duplicate hostnames
    Set<String> hostnames = new HashSet<>();
    Map<String, Set<String>> duplicateHostnames = new HashMap<>();
    for (ConfigDb configDb : configDbs) {
      String hostname = configDb.getHostname().get(); // must be non-empty
      if (!hostnames.add(hostname)) {
        duplicateHostnames
            .computeIfAbsent(hostname, h -> new HashSet<>())
            .add(configDb.getFilename());
      }
    }
    for (String hostname : duplicateHostnames.keySet()) {
      pvcae.addRedFlagWarning(
          RELPATH_SONIC_CONFIGDB_DIR,
          new Warning(
              String.format(
                  "Duplicate hostname %s in files %s", hostname, duplicateHostnames.get(hostname)),
              RELPATH_SONIC_CONFIGDB_DIR));
    }

    // return a map of all non-duplicate hostnames
    return new SonicConfigDbs(
        configDbs.stream()
            .filter(configDb -> !duplicateHostnames.containsKey(configDb.getHostname().get()))
            .collect(ImmutableMap.toImmutableMap(c -> c.getHostname().get(), Function.identity())));
  }

  private @Nonnull final Map<String, ConfigDb> _configDbs;
}
