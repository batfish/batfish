package org.batfish.representation.aws;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GenericConfigObject;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;

/** The top-level class that represent AWS configuration */
@ParametersAreNonnullByDefault
public class AwsConfiguration implements Serializable, GenericConfigObject {

  private static final long INITIAL_GENERATED_IP = Ip.FIRST_CLASS_E_EXPERIMENTAL_IP.asLong();

  @Nonnull private final Map<String, Configuration> _configurationNodes;

  @Nonnull private final AtomicLong _currentGeneratedIpAsLong;

  @Nonnull private final Map<String, Region> _regions;

  public AwsConfiguration() {
    _configurationNodes = new HashMap<>();
    _currentGeneratedIpAsLong = new AtomicLong(INITIAL_GENERATED_IP);
    _regions = new HashMap<>();
  }

  /** Adds a config subtree */
  public void addConfigElement(
      String region,
      JsonNode json,
      String sourceFileName,
      ParseVendorConfigurationAnswerElement pvcae)
      throws IOException {
    _regions
        .computeIfAbsent(region, r -> new Region(region))
        .addConfigElement(json, sourceFileName, pvcae);
  }

  @Nonnull
  Map<String, Configuration> getConfigurationNodes() {
    return _configurationNodes;
  }

  @Nonnull
  Prefix getNextGeneratedLinkSubnet() {
    long base = _currentGeneratedIpAsLong.getAndAdd(2L);
    assert base % 2 == 0;
    return Prefix.create(Ip.create(base), Prefix.MAX_PREFIX_LENGTH - 1);
  }

  /** Convert this AWS config to a set of VI configurations */
  @Nonnull
  public Map<String, Configuration> toConfigurations(
      Settings settings, Map<String, Warnings> warningsByHost) {

    for (Region region : _regions.values()) {
      region.toConfigurationNodes(this, _configurationNodes, settings, warningsByHost);
    }

    return _configurationNodes;
  }
}
