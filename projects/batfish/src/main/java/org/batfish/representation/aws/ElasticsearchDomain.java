package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;

/** Represents elastic search domain in AWS */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class ElasticsearchDomain implements AwsVpcEntity, Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class VpcOptions {

    @Nonnull private final String _vpcId;

    @Nonnull private final List<String> _securityGroupIds;

    @Nonnull private final List<String> _subnetIds;

    @JsonCreator
    private static VpcOptions create(
        @Nullable @JsonProperty(JSON_KEY_ES_VPC_ID) String vpcId,
        @Nullable @JsonProperty(JSON_KEY_SECURITY_GROUP_IDS) List<String> securityGroupIds,
        @Nullable @JsonProperty(JSON_KEY_SUBNET_IDS) List<String> subnetIds) {
      checkArgument(vpcId != null, "VPC Id cannot be null for VPC options");
      checkArgument(securityGroupIds != null, "Security group Ids cannot be null for VPC options");
      checkArgument(subnetIds != null, "Subnet Ids cannot be null for VPC options");

      return new VpcOptions(vpcId, securityGroupIds, subnetIds);
    }

    private VpcOptions(String vpcId, List<String> securityGroupIds, List<String> subnetIds) {
      _vpcId = vpcId;
      _securityGroupIds = securityGroupIds;
      _subnetIds = subnetIds;
    }

    @Nonnull
    public String getVpcId() {
      return _vpcId;
    }

    @Nonnull
    public List<String> getSecurityGroupIds() {
      return _securityGroupIds;
    }

    @Nonnull
    public List<String> getSubnetIds() {
      return _subnetIds;
    }
  }

  @Nonnull private final List<String> _securityGroups;

  @Nonnull private final String _domainName;

  @Nullable private final String _vpcId;

  @Nonnull private final List<String> _subnets;

  private final boolean _available;

  public boolean getAvailable() {
    return _available;
  }

  @Override
  public String getId() {
    return _domainName;
  }

  @Nonnull
  public List<String> getSecurityGroups() {
    return _securityGroups;
  }

  @Nonnull
  public List<String> getSubnets() {
    return _subnets;
  }

  @Nullable
  public String getVpcId() {
    return _vpcId;
  }

  @JsonCreator
  private static ElasticsearchDomain create(
      @Nullable @JsonProperty(JSON_KEY_DOMAIN_NAME) String domainName,
      @Nullable @JsonProperty(JSON_KEY_VPC_OPTIONS) VpcOptions vpcOptions,
      @Nullable @JsonProperty(JSON_KEY_CREATED) Boolean created,
      @Nullable @JsonProperty(JSON_KEY_DELETED) Boolean deleted) {
    checkArgument(domainName != null, "Domain name cannot be null for elastic search domain");
    checkArgument(created != null, "Created key must exist in elastic search domain");
    checkArgument(deleted != null, "Deleted key must exist in elastic search domain");

    return new ElasticsearchDomain(
        domainName,
        vpcOptions == null ? null : vpcOptions.getVpcId(),
        vpcOptions == null ? ImmutableList.of() : vpcOptions.getSecurityGroupIds(),
        vpcOptions == null ? ImmutableList.of() : vpcOptions.getSubnetIds(),
        created && !deleted);
  }

  public ElasticsearchDomain(
      String domainName,
      @Nullable String vpcId,
      List<String> securityGroups,
      List<String> subnets,
      boolean available) {
    _domainName = domainName;
    _vpcId = vpcId;
    _securityGroups = securityGroups;
    _subnets = subnets;
    _available = available;
  }

  public Configuration toConfigurationNode(
      AwsConfiguration awsVpcConfig, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_domainName, "aws");

    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    // create an interface per subnet
    for (String subnetId : _subnets) {
      Subnet subnet = region.getSubnets().get(subnetId);
      if (subnet == null) {
        warnings.redFlag(
            String.format(
                "Subnet \"%s\" for Elasticsearch domain \"%s\" not found", subnetId, _domainName));
        continue;
      }
      String instancesIfaceName = String.format("%s-%s", _domainName, subnetId);
      Ip instancesIfaceIp = subnet.getNextIp();
      ConcreteInterfaceAddress instancesIfaceAddress =
          ConcreteInterfaceAddress.create(
              instancesIfaceIp, subnet.getCidrBlock().getPrefixLength());
      Utils.newInterface(instancesIfaceName, cfgNode, instancesIfaceAddress);

      Ip defaultGatewayAddress = subnet.computeInstancesIfaceIp();
      StaticRoute defaultRoute =
          StaticRoute.builder()
              .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
              .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
              .setNextHopIp(defaultGatewayAddress)
              .setNetwork(Prefix.ZERO)
              .build();
      cfgNode.getDefaultVrf().getStaticRoutes().add(defaultRoute);
    }

    Utils.processSecurityGroups(region, cfgNode, _securityGroups, warnings);

    return cfgNode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ElasticsearchDomain)) {
      return false;
    }
    ElasticsearchDomain that = (ElasticsearchDomain) o;
    return _available == that._available
        && Objects.equals(_securityGroups, that._securityGroups)
        && Objects.equals(_domainName, that._domainName)
        && Objects.equals(_vpcId, that._vpcId)
        && Objects.equals(_subnets, that._subnets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_securityGroups, _domainName, _vpcId, _subnets, _available);
  }
}
