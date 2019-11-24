package org.batfish.representation.aws;

import static org.batfish.representation.aws.InternetGateway.BACKBONE_INTERFACE_NAME;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.vendor.VendorConfiguration;

/** The top-level class that represent AWS configuration */
@ParametersAreNonnullByDefault
public class AwsConfiguration extends VendorConfiguration {

  private static final long INITIAL_GENERATED_IP = Ip.FIRST_CLASS_E_EXPERIMENTAL_IP.asLong();

  @Nonnull private final Map<String, Configuration> _configurationNodes;

  @Nonnull private final AtomicLong _currentGeneratedIpAsLong;

  @Nonnull private final Map<String, Region> _regions;

  public AwsConfiguration() {
    this(new HashMap<>());
  }

  public AwsConfiguration(Map<String, Region> regions) {
    this(regions, new HashMap<>(), new AtomicLong(INITIAL_GENERATED_IP));
  }

  public AwsConfiguration(
      Map<String, Region> regions, Map<String, Configuration> configurationNodes) {
    this(regions, configurationNodes, new AtomicLong(INITIAL_GENERATED_IP));
  }

  public AwsConfiguration(
      Map<String, Region> regions,
      Map<String, Configuration> configurationNodes,
      AtomicLong currentGeneratedIpAsLong) {
    _regions = regions;
    _configurationNodes = configurationNodes;
    _currentGeneratedIpAsLong = currentGeneratedIpAsLong;
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

  /**
   * Convert this AWS config to a set of VI configurations
   *
   * <p>TODO: Populate all the structure names that appear in these configs
   */
  @Nonnull
  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    for (Region region : _regions.values()) {
      region.toConfigurationNodes(this, _configurationNodes, getWarnings());
    }

    return ImmutableList.copyOf(_configurationNodes.values());
  }

  @Override
  @Nonnull
  public List<BorderInterfaceInfo> getBorderInterfaces() {
    return _regions.values().stream()
        .flatMap(r -> r.getInternetGateways().values().stream())
        .map(igw -> NodeInterfacePair.of(igw.getId(), BACKBONE_INTERFACE_NAME))
        .map(BorderInterfaceInfo::new)
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  public String getFilename() {
    // not a real file name but a folder
    return BfConsts.RELPATH_AWS_CONFIGS_FILE;
  }

  @Override
  public String getHostname() {
    // This hostname does not appear in the vendor independent configs that are returned
    return BfConsts.RELPATH_AWS_CONFIGS_FILE;
  }

  @Override
  public void setHostname(String hostname) {
    throw new IllegalStateException("Setting the hostname is not allowed for AWS configs");
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    throw new IllegalStateException("Setting the format is not allowed for AWS configs");
  }
}
