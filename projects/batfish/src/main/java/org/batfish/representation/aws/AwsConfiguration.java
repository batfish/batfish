package org.batfish.representation.aws;

import static org.batfish.representation.aws.InternetGateway.BACKBONE_INTERFACE_NAME;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;

/** The top-level class that represent AWS configuration */
@ParametersAreNonnullByDefault
public class AwsConfiguration implements Serializable, GenericConfigObject {

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

  /** Convert this AWS config to a set of VI configurations */
  @Nonnull
  public Map<String, Configuration> toConfigurations(
      Settings settings, Map<String, Warnings> warningsByHost) {

    for (Region region : _regions.values()) {
      region.toConfigurationNodes(this, _configurationNodes, settings, warningsByHost);
    }

    return _configurationNodes;
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
  @Nonnull
  public Map<String, String> getAddressSpacePartitions() {
    ImmutableMap.Builder<String, String> partitions = ImmutableMap.builder();
    _regions.values().stream()
        .flatMap(r -> r.getSubnets().values().stream())
        .forEach(
            subnet -> {
              partitions.put(subnet.getId(), subnet.getVpcId());
            });
    _regions.values().stream()
        .flatMap(r -> r.getInstances().values().stream())
        .forEach(
            instance -> {
              if (instance.getVpcId() != null) {
                partitions.put(instance.getId(), instance.getVpcId());
              }
            });
    return partitions.build();
  }
}
