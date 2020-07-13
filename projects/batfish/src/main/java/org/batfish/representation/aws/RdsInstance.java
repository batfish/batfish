package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

/** Represents as RDS instance */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class RdsInstance implements AwsVpcEntity, Serializable {

  private static final Set<String> _DOWN_STATES =
      ImmutableSet.of(
          "creating", "deleting", "stopped", "stopping", "failed", "rebooting", "moving-to-vpc");

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class DbSubnetGroup {

    @Nonnull private final String _vpcId;

    @Nonnull private final List<DbSubnet> _dbSubnets;

    @JsonCreator
    private static DbSubnetGroup create(
        @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
        @Nullable @JsonProperty(JSON_KEY_SUBNETS) List<DbSubnet> dbSubnets) {
      checkArgument(vpcId != null, "VPC Id cannot be null for DB subnet group");
      checkArgument(dbSubnets != null, "Subnets cannot be null for DB subnet group");

      return new DbSubnetGroup(vpcId, dbSubnets);
    }

    private DbSubnetGroup(String vpcId, List<DbSubnet> dbSubnets) {
      _vpcId = vpcId;
      _dbSubnets = dbSubnets;
    }

    @Nonnull
    String getVpcId() {
      return _vpcId;
    }

    @Nonnull
    List<DbSubnet> getDbSubnets() {
      return _dbSubnets;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class DbSubnet {

    @Nonnull private final String _availabilityZone;

    @Nonnull private final String _identifier;

    @Nonnull private final String _status;

    @JsonCreator
    private static DbSubnet create(
        @Nullable @JsonProperty(JSON_KEY_SUBNET_AVAILABILITY_ZONE) DbSubnetAz availabilityZone,
        @Nullable @JsonProperty(JSON_KEY_SUBNET_IDENTIFIER) String identifier,
        @Nullable @JsonProperty(JSON_KEY_SUBNET_STATUS) String status) {
      checkArgument(availabilityZone != null, "Availability zone cannot be null for DB subnet");
      checkArgument(identifier != null, "Identifier cannot be null for DB subnet");
      checkArgument(status != null, "Status cannot be null for DB subnet");

      return new DbSubnet(availabilityZone.getName(), identifier, status);
    }

    private DbSubnet(String availabilityZone, String identifier, String status) {
      _availabilityZone = availabilityZone;
      _identifier = identifier;
      _status = status;
    }

    @Nonnull
    String getAvailabilityZone() {
      return _availabilityZone;
    }

    @Nonnull
    String getIdentifier() {
      return _identifier;
    }

    @Nonnull
    String getStatus() {
      return _status;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class DbSubnetAz {

    @Nonnull private final String _name;

    @JsonCreator
    private static DbSubnetAz create(@Nullable @JsonProperty(JSON_KEY_NAME) String name) {
      checkArgument(name != null, "Name cannot be null for DB subnet group availability zone");
      return new DbSubnetAz(name);
    }

    private DbSubnetAz(String name) {
      _name = name;
    }

    @Nonnull
    String getName() {
      return _name;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class VpcSecurityGroup {

    @Nonnull private final String _status;
    @Nonnull private final String _id;

    @JsonCreator
    private static VpcSecurityGroup create(
        @Nullable @JsonProperty(JSON_KEY_STATUS) String status,
        @Nullable @JsonProperty(JSON_KEY_VPC_SECURITY_GROUP_ID) String id) {
      checkArgument(status != null, "Status cannot be null for VPC security group");
      checkArgument(id != null, "Security group id cannot be null for VPC security group");
      return new VpcSecurityGroup(status, id);
    }

    private VpcSecurityGroup(String status, String id) {
      _status = status;
      _id = id;
    }

    @Nonnull
    String getStatus() {
      return _status;
    }

    @Nonnull
    String getId() {
      return _id;
    }
  }

  @Nonnull private final String _dbInstanceIdentifier;

  @Nonnull private final String _dbInstanceStatus;

  @Nonnull private final ListMultimap<String, String> _azsSubnetIds;

  @Nonnull private final String _availabilityZone;

  @Nonnull private final String _vpcId;

  private final boolean _multiAz;

  @Nonnull private final List<String> _securityGroups;

  @JsonCreator
  private static RdsInstance create(
      @Nullable @JsonProperty(JSON_KEY_DB_INSTANCE_IDENTIFIER) String dbInstanceIdentifier,
      @Nullable @JsonProperty(JSON_KEY_AVAILABILITY_ZONE) String availabilityZone,
      @Nullable @JsonProperty(JSON_KEY_DB_SUBNET_GROUP) DbSubnetGroup dbSubnetGroup,
      @Nullable @JsonProperty(JSON_KEY_MULTI_AZ) Boolean multiAz,
      @Nullable @JsonProperty(JSON_KEY_DB_INSTANCE_STATUS) String dbInstanceStatus,
      @Nullable @JsonProperty(JSON_KEY_VPC_SECURITY_GROUPS)
          List<VpcSecurityGroup> vpcSecurityGroups) {

    checkArgument(
        dbInstanceIdentifier != null, "DB instance identifier cannot be null for RDS instance");
    checkArgument(availabilityZone != null, "Availability zone cannot be null for RDS instance");
    checkArgument(dbSubnetGroup != null, "DB subnet group cannot be null for RDS instance");
    checkArgument(multiAz != null, "Multi AZ key must be present for RDS instance");
    checkArgument(dbInstanceStatus != null, "DB instance status cannot be null for RDS instance");
    checkArgument(vpcSecurityGroups != null, "VPC security groups cannot be null for RDS instance");

    ListMultimap<String, String> azsSubnetIds = ArrayListMultimap.create();
    dbSubnetGroup
        .getDbSubnets()
        .forEach(
            s -> {
              if (s.getStatus().equalsIgnoreCase("active")) {
                azsSubnetIds.put(s.getAvailabilityZone(), s.getIdentifier());
              }
            });

    return new RdsInstance(
        dbInstanceIdentifier,
        availabilityZone,
        dbSubnetGroup.getVpcId(),
        multiAz,
        dbInstanceStatus,
        azsSubnetIds,
        vpcSecurityGroups.stream()
            .filter(g -> g.getStatus().equalsIgnoreCase("active"))
            .map(VpcSecurityGroup::getId)
            .collect(ImmutableList.toImmutableList()));
  }

  RdsInstance(
      String dbInstanceIdentifier,
      String availabilityZone,
      String vpcId,
      boolean multiAz,
      String dbInstanceStatus,
      ListMultimap<String, String> azSubnetIds,
      List<String> securityGroups) {
    _dbInstanceIdentifier = dbInstanceIdentifier;
    _availabilityZone = availabilityZone;
    _vpcId = vpcId;
    _multiAz = multiAz;
    _dbInstanceStatus = dbInstanceStatus;
    _azsSubnetIds = azSubnetIds;
    _securityGroups = securityGroups;
  }

  @Override
  public String getId() {
    return _dbInstanceIdentifier;
  }

  public Multimap<String, String> getAzSubnetIds() {
    return _azsSubnetIds;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  public boolean getMultiAz() {
    return _multiAz;
  }

  @Nonnull
  public String getAvailabilityZone() {
    return _availabilityZone;
  }

  @Nonnull
  public List<String> getSecurityGroups() {
    return _securityGroups;
  }

  @Nonnull
  public String getDbInstanceStatus() {
    return _dbInstanceStatus;
  }

  /** Return boolean indicating if the instance is up and operational. */
  @JsonIgnore
  public boolean isUp() {
    return !_DOWN_STATES.contains(_dbInstanceStatus);
  }

  Configuration toConfigurationNode(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(_dbInstanceIdentifier, "aws", DeviceModel.AWS_RDS_INSTANCE);

    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    // Deterministically pick a subnet in the right AZ that exists and has an available IP.
    // TODO: this should be based on the rds-instance ENIs in the NetworkInterfaces.json, also by
    // DNS address of the RDS interface.
    List<String> subnetsInAz = _azsSubnetIds.get(_availabilityZone);
    Optional<Subnet> matchingSubnet =
        subnetsInAz.stream()
            .sorted()
            .map(s -> region.getSubnets().get(s))
            .filter(Objects::nonNull) // exists
            .filter(Subnet::hasNextIp) // has available IP
            .findFirst();
    // If such a subnet exists, put the instance in its aggregate and hook it up.
    if (matchingSubnet.isPresent()) {
      Subnet subnet = matchingSubnet.get();
      String subnetId = subnet.getId();
      cfgNode.getVendorFamily().getAws().setSubnetId(subnetId);

      String instancesIfaceName = String.format("%s-%s", _dbInstanceIdentifier, subnetId);
      Ip instancesIfaceIp = subnet.getNextIp();
      ConcreteInterfaceAddress instancesIfaceAddress =
          ConcreteInterfaceAddress.create(
              instancesIfaceIp, subnet.getCidrBlock().getPrefixLength());
      Utils.newInterface(
          instancesIfaceName, cfgNode, instancesIfaceAddress, "To subnet " + subnetId);

      Utils.addLayer1Edge(
          awsConfiguration,
          cfgNode.getHostname(),
          instancesIfaceName,
          Subnet.nodeName(subnetId),
          Subnet.instancesInterfaceName(subnetId));

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

    return cfgNode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdsInstance)) {
      return false;
    }
    RdsInstance that = (RdsInstance) o;
    return _multiAz == that._multiAz
        && Objects.equals(_dbInstanceIdentifier, that._dbInstanceIdentifier)
        && _dbInstanceStatus.equals(that._dbInstanceStatus)
        && Objects.equals(_azsSubnetIds, that._azsSubnetIds)
        && Objects.equals(_availabilityZone, that._availabilityZone)
        && Objects.equals(_vpcId, that._vpcId)
        && Objects.equals(_securityGroups, that._securityGroups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _dbInstanceIdentifier,
        _dbInstanceStatus,
        _azsSubnetIds,
        _availabilityZone,
        _vpcId,
        _multiAz,
        _securityGroups);
  }
}
