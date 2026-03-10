package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.connectGatewayToVpc;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.specifier.Location.interfaceLinkLocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.specifier.LocationInfo;

/** Represents an AWS VPC endpoint of type gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class VpcEndpointGateway extends VpcEndpoint {

  /** Name of the filter that permits destination IPs that belong to the service and drops rest */
  static final String SERVICE_PREFIX_FILTER = "~SERVICE~PREFIX~FILTER";

  static final TraceElement PERMIT_SERVICE_IPS = TraceElement.of("Allowed IPs in service prefixes");

  static final TraceElement DENY_NON_SERVICE_IPS =
      TraceElement.of("Denied IPs not in service prefixes");

  public VpcEndpointGateway(String id, String serviceName, String vpcId, Map<String, String> tags) {
    super(id, serviceName, vpcId, tags);
  }

  @Override
  List<Configuration> toConfigurationNodes(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    return ImmutableList.of(toConfigurationNode(awsConfiguration, region, warnings));
  }

  @VisibleForTesting
  Configuration toConfigurationNode(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(_id, "aws", DeviceModel.AWS_VPC_ENDPOINT_GATEWAY);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.setHumanName(humanName(_tags, _serviceName));

    Interface serviceInterface =
        Utils.newInterface(
            serviceInterfaceName(_serviceName),
            cfgNode,
            LinkLocalAddress.of(LINK_LOCAL_IP),
            "To Service");

    List<Prefix> servicePrefixes = getServicePrefixes(_serviceName, region, warnings);
    IpSpace servicePrefixSpace =
        IpWildcardSetIpSpace.builder()
            .including(
                servicePrefixes.stream().map(IpWildcard::create).collect(Collectors.toList()))
            .build();

    servicePrefixes.forEach(
        prefix -> addStaticRoute(cfgNode, toStaticRoute(prefix, serviceInterface.getName())));

    IpAccessList servicePrefixFilter = computeServicePrefixFilter(servicePrefixSpace);
    cfgNode.getIpAccessLists().put(servicePrefixFilter.getName(), servicePrefixFilter);

    Interface vpcInterface =
        connectGatewayToVpc(_id, cfgNode, _vpcId, awsConfiguration, region, warnings);
    if (vpcInterface != null) {
      vpcInterface.setIncomingFilter(servicePrefixFilter);
    }

    cfgNode.setLocationInfo(
        ImmutableMap.of(
            interfaceLinkLocation(serviceInterface),
            new LocationInfo(
                true,
                servicePrefixSpace,
                // using LINK_LOCAL_IP gets us EXITS_NETWORK as disposition for service prefixes
                LINK_LOCAL_IP.toIpSpace())));

    return cfgNode;
  }

  @VisibleForTesting
  static IpAccessList computeServicePrefixFilter(IpSpace servicePrefixSpace) {
    return IpAccessList.builder()
        .setName(SERVICE_PREFIX_FILTER)
        .setLines(
            ExprAclLine.builder()
                .setTraceElement(PERMIT_SERVICE_IPS)
                .setMatchCondition(AclLineMatchExprs.matchDst(servicePrefixSpace))
                .setAction(LineAction.PERMIT)
                .build(),
            ExprAclLine.builder()
                .setTraceElement(DENY_NON_SERVICE_IPS)
                .setMatchCondition(TrueExpr.INSTANCE)
                .setAction(LineAction.DENY)
                .build())
        .build();
  }

  private static List<Prefix> getServicePrefixes(
      String serviceName, Region region, Warnings warnings) {
    Optional<PrefixList> prefixList =
        region.getPrefixLists().values().stream()
            .filter(plist -> plist.getPrefixListName().equals(serviceName))
            .findAny();

    if (!prefixList.isPresent()) {
      warnings.redFlagf("Prefix list not found for VPC endpoint %s", serviceName);
      return ImmutableList.of();
    }

    return prefixList.get().getCidrs();
  }

  @VisibleForTesting
  static String serviceInterfaceName(String serviceName) {
    return serviceName;
  }

  @VisibleForTesting
  static @Nonnull String humanName(Map<String, String> tags, String serviceName) {
    return tags.getOrDefault(TAG_NAME, serviceName);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VpcEndpointGateway)) {
      return false;
    }
    VpcEndpointGateway that = (VpcEndpointGateway) o;
    return _id.equals(that._id)
        && _serviceName.equals(that._serviceName)
        && _vpcId.equals(that._vpcId)
        && _tags.equals(that._tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _serviceName, _vpcId, _tags);
  }
}
