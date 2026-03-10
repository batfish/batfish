package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;

/** Represents elastic search domain in AWS */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class ElasticsearchDomain implements AwsVpcEntity, Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class ElasticSearchClusterConfig {

    private final int _instanceCount;

    @JsonCreator
    private static ElasticSearchClusterConfig create(
        @JsonProperty(JSON_KEY_INSTANCE_COUNT) @Nullable Integer instanceCount) {
      checkNonNull(instanceCount, JSON_KEY_INSTANCE_COUNT, "ElasticSearchClusterConfig");

      return new ElasticSearchClusterConfig(instanceCount);
    }

    private ElasticSearchClusterConfig(int instanceCount) {
      _instanceCount = instanceCount;
    }

    public int getInstanceCount() {
      return _instanceCount;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class VpcOptions {

    private final @Nonnull String _vpcId;

    private final @Nonnull List<String> _securityGroupIds;

    private final @Nonnull List<String> _subnetIds;

    @JsonCreator
    private static VpcOptions create(
        @JsonProperty(JSON_KEY_ES_VPC_ID) @Nullable String vpcId,
        @JsonProperty(JSON_KEY_SECURITY_GROUP_IDS) @Nullable List<String> securityGroupIds,
        @JsonProperty(JSON_KEY_SUBNET_IDS) @Nullable List<String> subnetIds) {
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

    public @Nonnull String getVpcId() {
      return _vpcId;
    }

    public @Nonnull List<String> getSecurityGroupIds() {
      return _securityGroupIds;
    }

    public @Nonnull List<String> getSubnetIds() {
      return _subnetIds;
    }
  }

  private final @Nonnull String _arn;

  private final @Nonnull List<String> _securityGroups;

  private final @Nonnull String _domainName;

  private final @Nullable String _vpcEndpoint;

  private final @Nullable String _vpcId;

  private final int _instanceCount;

  private final @Nonnull List<String> _subnets;

  private final boolean _available;

  public boolean getAvailable() {
    return _available;
  }

  public @Nonnull String getDomainName() {
    return _domainName;
  }

  @Override
  public String getId() {
    return _arn;
  }

  public int getInstanceCount() {
    return _instanceCount;
  }

  public @Nonnull List<String> getSecurityGroups() {
    return _securityGroups;
  }

  public @Nonnull List<String> getSubnets() {
    return _subnets;
  }

  public @Nullable String getVpcEndpoint() {
    return _vpcEndpoint;
  }

  public @Nullable String getVpcId() {
    return _vpcId;
  }

  @JsonCreator
  private static ElasticsearchDomain create(
      @JsonProperty(JSON_KEY_ARN) @Nullable String arn,
      @JsonProperty(JSON_KEY_DOMAIN_NAME) @Nullable String domainName,
      @JsonProperty(JSON_KEY_VPC_OPTIONS) @Nullable VpcOptions vpcOptions,
      @JsonProperty(JSON_KEY_ELASTIC_SEARCH_CLUSTER_CONFIG) @Nullable
          ElasticSearchClusterConfig clusterConfig,
      @JsonProperty(JSON_KEY_CREATED) @Nullable Boolean created,
      @JsonProperty(JSON_KEY_DELETED) @Nullable Boolean deleted,
      @JsonProperty(JSON_KEY_ENDPOINTS) @Nullable Map<String, String> endpoints) {
    checkNonNull(arn, JSON_KEY_ARN, "ELastic search domain");
    checkNonNull(domainName, JSON_KEY_DOMAIN_NAME, "Elastic search domain");
    checkNonNull(clusterConfig, JSON_KEY_ELASTIC_SEARCH_CLUSTER_CONFIG, "Elastic search domain");
    checkNonNull(created, JSON_KEY_CREATED, "Elastic search domain");
    checkNonNull(deleted, JSON_KEY_DELETED, "Elastic search domain");

    return new ElasticsearchDomain(
        arn,
        domainName,
        vpcOptions == null ? null : vpcOptions.getVpcId(),
        endpoints == null ? null : endpoints.getOrDefault("vpc", null),
        clusterConfig._instanceCount,
        vpcOptions == null ? ImmutableList.of() : vpcOptions.getSecurityGroupIds(),
        vpcOptions == null ? ImmutableList.of() : vpcOptions.getSubnetIds(),
        created && !deleted);
  }

  public ElasticsearchDomain(
      String arn,
      String domainName,
      @Nullable String vpcId,
      @Nullable String vpcEndpoint,
      int instanceCount,
      List<String> securityGroups,
      List<String> subnets,
      boolean available) {
    _arn = arn;
    _domainName = domainName;
    _vpcId = vpcId;
    _vpcEndpoint = vpcEndpoint;
    _instanceCount = instanceCount;
    _securityGroups = securityGroups;
    _subnets = subnets;
    _available = available;
  }

  public List<Configuration> toConfigurationNodes(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    return IntStream.range(0, _instanceCount)
        .mapToObj(
            instanceNum ->
                toConfigurationNode(
                    instanceNum,
                    _subnets.get(instanceNum % _subnets.size()),
                    awsConfiguration,
                    region,
                    warnings))
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  @Nullable
  Configuration toConfigurationNode(
      int instanceNumber,
      String subnetId,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    Subnet subnet = region.getSubnets().get(subnetId);
    if (subnet == null) {
      warnings.redFlagf(
          "Subnet \"%s\" for Elasticsearch domain \"%s\" not found", subnetId, _domainName);
      return null;
    }
    Configuration cfgNode =
        Utils.newAwsConfiguration(
            getNodeName(instanceNumber, _arn, _vpcEndpoint),
            "aws",
            DeviceModel.AWS_ELASTICSEARCH_DOMAIN);

    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());
    cfgNode.getVendorFamily().getAws().setSubnetId(subnetId);
    cfgNode.setHumanName(_domainName);

    // TODO: a better way to get IPs in the subnet, use the network interface data or DNS queries
    String instancesIfaceName = subnetId;
    Ip instancesIfaceIp = subnet.getNextIp();
    ConcreteInterfaceAddress instancesIfaceAddress =
        ConcreteInterfaceAddress.create(instancesIfaceIp, subnet.getCidrBlock().getPrefixLength());
    Utils.newInterface(instancesIfaceName, cfgNode, instancesIfaceAddress, "To subnet " + subnetId);

    Utils.addLayer1Edge(
        awsConfiguration,
        cfgNode.getHostname(),
        instancesIfaceName,
        Subnet.nodeName(subnet.getId()),
        Subnet.instancesInterfaceName(subnet.getId()));

    Ip defaultGatewayAddress = subnet.computeInstancesIfaceIp();
    StaticRoute defaultRoute =
        StaticRoute.builder()
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
            .setNextHopIp(defaultGatewayAddress)
            .setNetwork(Prefix.ZERO)
            .build();
    cfgNode.getDefaultVrf().getStaticRoutes().add(defaultRoute);

    return cfgNode;
  }

  static @Nonnull String getNodeName(int instanceNumber, String arn, @Nullable String vpcEndpoint) {
    return String.format("%d-%s", instanceNumber, vpcEndpoint == null ? arn : vpcEndpoint);
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
    return _arn.equals(that._arn)
        && _available == that._available
        && _instanceCount == that._instanceCount
        && Objects.equals(_securityGroups, that._securityGroups)
        && Objects.equals(_domainName, that._domainName)
        && Objects.equals(_vpcId, that._vpcId)
        && Objects.equals(_vpcEndpoint, that._vpcEndpoint)
        && Objects.equals(_subnets, that._subnets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _arn,
        _instanceCount,
        _vpcEndpoint,
        _securityGroups,
        _domainName,
        _vpcId,
        _subnets,
        _available);
  }
}
