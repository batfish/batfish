package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsLocationInfoUtils.INSTANCE_INTERFACE_LINK_LOCATION_INFO;
import static org.batfish.representation.aws.AwsLocationInfoUtils.instanceInterfaceLocationInfo;
import static org.batfish.representation.aws.Utils.addNodeToSubnet;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;

/** Represents an AWS VPC endpoint of type gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpcEndpointInterface extends VpcEndpoint {

  private final @Nonnull List<String> _networkInterfaceIds;

  private final @Nonnull List<String> _subnetIds;

  VpcEndpointInterface(
      String id,
      String serviceName,
      String vpcId,
      List<String> networkInterfaceIds,
      List<String> subnetIds,
      Map<String, String> tags) {
    super(id, serviceName, vpcId, tags);
    _networkInterfaceIds = networkInterfaceIds;
    _subnetIds = subnetIds;
  }

  @Override
  List<Configuration> toConfigurationNodes(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    return _subnetIds.stream()
        .map(subnetId -> toConfigurationNode(subnetId, awsConfiguration, region, warnings))
        .collect(ImmutableList.toImmutableList());
  }

  /** Creates configuration node corresponding to one availability zone */
  @VisibleForTesting
  Configuration toConfigurationNode(
      String subnetId, ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(
            getNodeId(subnetId, _id), "aws", DeviceModel.AWS_VPC_ENDPOINT_INTERFACE);
    cfgNode.setHumanName(_tags.get(TAG_NAME));
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setSubnetId(subnetId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    Subnet subnet = region.getSubnets().get(subnetId);
    if (subnet == null) {
      warnings.redFlagf(
          "Subnet with id %s, associated with VPC endpoint %s, not found", subnetId, _id);
      return cfgNode;
    }

    // find the network interface that is in this subnet and is one of our interfaces
    Optional<NetworkInterface> networkInterface =
        region.getNetworkInterfaces().values().stream()
            .filter(
                iface ->
                    _networkInterfaceIds.contains(iface.getId())
                        && iface.getSubnetId().equals(subnetId))
            .findFirst();
    if (!networkInterface.isPresent()) {
      warnings.redFlagf(
          "Network interface not found for VPC endpoint %s in subnet %s", _id, subnetId);
      return cfgNode;
    }

    Interface viIface =
        addNodeToSubnet(cfgNode, networkInterface.get(), subnet, awsConfiguration, warnings);

    // Create LocationInfo the interface
    cfgNode.setLocationInfo(
        ImmutableMap.of(
            interfaceLocation(viIface),
            instanceInterfaceLocationInfo(viIface),
            interfaceLinkLocation(viIface),
            INSTANCE_INTERFACE_LINK_LOCATION_INFO));

    return cfgNode;
  }

  static String getNodeId(String subnetId, String vpcEndpointId) {
    return String.format("%s-%s", vpcEndpointId, subnetId);
  }

  public @Nonnull List<String> getNetworkInterfaceIds() {
    return _networkInterfaceIds;
  }

  public @Nonnull List<String> getSubnetIds() {
    return _subnetIds;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VpcEndpointInterface)) {
      return false;
    }
    VpcEndpointInterface that = (VpcEndpointInterface) o;
    return _networkInterfaceIds.equals(that._networkInterfaceIds)
        && _subnetIds.equals(that._subnetIds)
        && _id.equals(that._id)
        && _serviceName.equals(that._serviceName)
        && _vpcId.equals(that._vpcId)
        && _tags.equals(that._tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_networkInterfaceIds, _subnetIds, _id, _serviceName, _vpcId, _tags);
  }
}
