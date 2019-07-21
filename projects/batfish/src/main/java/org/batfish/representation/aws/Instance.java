package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;

/** Representation for an EC2 instance */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class Instance implements AwsVpcEntity, Serializable {

  /** Represents the status of the instance */
  public enum Status {
    PENDING("pending"),
    RUNNING("running"),
    SHUTTING_DOWN("shutting-down"),
    TERMINATED("terminated"),
    STOPPING("stopping"),
    STOPPED("stopped");

    private static final Map<String, Status> MAP = initMap();

    @JsonCreator
    public static Status fromString(String name) {
      Status value = MAP.get(name.toLowerCase());
      if (value == null) {
        throw new BatfishException(
            "No " + Status.class.getSimpleName() + " with name: '" + name + "'");
      }
      return value;
    }

    private static Map<String, Status> initMap() {
      ImmutableMap.Builder<String, Status> map = ImmutableMap.builder();
      for (Status value : Status.values()) {
        String name = value._name.toLowerCase();
        map.put(name, value);
      }
      return map.build();
    }

    private final String _name;

    Status(String name) {
      _name = name;
    }

    @JsonValue
    public String getName() {
      return _name;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class NetworkInterfaceId {

    @Nonnull private final String _id;

    @JsonCreator
    private static NetworkInterfaceId create(
        @Nullable @JsonProperty(JSON_KEY_NETWORK_INTERFACE_ID) String id) {
      checkArgument(id != null, "Security group id is null");
      return new NetworkInterfaceId(id);
    }

    private NetworkInterfaceId(String id) {
      _id = id;
    }

    @Nonnull
    public String getId() {
      return _id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class SecurityGroupId {

    @Nonnull private final String _id;

    @JsonCreator
    private static SecurityGroupId create(@Nullable @JsonProperty(JSON_KEY_GROUP_ID) String id) {
      checkArgument(id != null, "Security group id is null");
      return new SecurityGroupId(id);
    }

    private SecurityGroupId(String id) {
      _id = id;
    }

    @Nonnull
    public String getId() {
      return _id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class State {

    @Nonnull private final Status _name;

    @JsonCreator
    private static State create(@Nullable @JsonProperty("Name") String name) {
      checkArgument(name != null, "Name of State is null");
      return new State(Status.fromString(name));
    }

    private State(Status name) {
      _name = name;
    }

    @Nonnull
    public Status getName() {
      return _name;
    }
  }

  @ParametersAreNonnullByDefault
  private static class Tag {

    @Nonnull private final String _key;

    @Nonnull private final String _value;

    @JsonCreator
    private static Tag create(
        @Nullable @JsonProperty("Key") String key, @Nullable @JsonProperty("Value") String value) {
      checkArgument(key != null, "Tag key is null");
      checkArgument(value != null, "Tag value is null");
      return new Tag(key, value);
    }

    private Tag(String key, String value) {
      _key = key;
      _value = value;
    }

    @Nonnull
    public String getKey() {
      return _key;
    }

    @Nonnull
    public String getValue() {
      return _value;
    }
  }

  @Nonnull private final String _instanceId;

  @Nonnull private final List<String> _networkInterfaces;

  @Nonnull private final List<String> _securityGroups;

  @Nonnull private final Status _status;

  @Nullable private final String _subnetId;

  @Nonnull private final Map<String, String> _tags;

  @Nullable private final String _vpcId;

  @JsonCreator
  private static Instance create(
      @Nullable @JsonProperty(JSON_KEY_INSTANCE_ID) String instanceId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
      @Nullable @JsonProperty(JSON_KEY_SECURITY_GROUPS) List<SecurityGroupId> securityGroups,
      @Nullable @JsonProperty(JSON_KEY_NETWORK_INTERFACES)
          List<NetworkInterfaceId> networkInterfaces,
      @Nullable @JsonProperty(JSON_KEY_TAGS) List<Tag> tags,
      @Nullable @JsonProperty(JSON_KEY_STATE) State state) {

    checkArgument(instanceId != null, "InstanceId cannot be null in Instance json");
    checkArgument(
        (vpcId == null && subnetId == null) || (vpcId != null && subnetId != null),
        "Only one of vpcId ('%s') and subnetId ('%s') is null",
        vpcId,
        subnetId);
    checkArgument(securityGroups != null, "Security groups cannot be null in Instance json");
    checkArgument(networkInterfaces != null, "Network interfaces cannot be null in Instance json");
    checkArgument(tags != null, "Tags cannot be null in Instance json");
    checkArgument(state != null, "State cannot be null in Instance json");

    return new Instance(
        instanceId,
        vpcId,
        subnetId,
        securityGroups.stream()
            .map(SecurityGroupId::getId)
            .collect(ImmutableList.toImmutableList()),
        networkInterfaces.stream()
            .map(NetworkInterfaceId::getId)
            .collect(ImmutableList.toImmutableList()),
        tags.stream().collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)),
        state.getName());

    // check if the public and private ip addresses are associated with an
    // interface
  }

  Instance(
      String instanceId,
      @Nullable String vpcId,
      @Nullable String subnetId,
      List<String> securityGroups,
      List<String> networkInterfaces,
      Map<String, String> tags,
      Status status) {
    _instanceId = instanceId;
    _vpcId = vpcId;
    _subnetId = subnetId;
    _securityGroups = securityGroups;
    _networkInterfaces = networkInterfaces;
    _tags = tags;
    _status = status;
  }

  @Override
  public String getId() {
    return _instanceId;
  }

  @Nonnull
  public String getInstanceId() {
    return _instanceId;
  }

  @Nonnull
  public List<String> getNetworkInterfaces() {
    return _networkInterfaces;
  }

  @Nonnull
  public List<String> getSecurityGroups() {
    return _securityGroups;
  }

  @Nonnull
  public Status getStatus() {
    return _status;
  }

  @Nullable
  public String getSubnetId() {
    return _subnetId;
  }

  @Nullable
  public String getVpcId() {
    return _vpcId;
  }

  Configuration toConfigurationNode(Region region, Warnings warnings) {
    String name = _tags.getOrDefault("Name", _instanceId);
    Configuration cfgNode = Utils.newAwsConfiguration(name, "aws");

    for (String interfaceId : _networkInterfaces) {

      NetworkInterface netInterface = region.getNetworkInterfaces().get(interfaceId);
      if (netInterface == null) {
        warnings.redFlag(
            String.format(
                "Network interface \"%s\" for instance \"%s\" not found",
                interfaceId, _instanceId));
        continue;
      }

      ImmutableSortedSet.Builder<ConcreteInterfaceAddress> ifaceAddressesBuilder =
          new ImmutableSortedSet.Builder<>(Comparator.naturalOrder());

      Subnet subnet = region.getSubnets().get(netInterface.getSubnetId());
      Prefix ifaceSubnet = subnet.getCidrBlock();
      Ip defaultGatewayAddress = subnet.computeInstancesIfaceIp();
      StaticRoute defaultRoute =
          StaticRoute.builder()
              .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
              .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
              .setNextHopIp(defaultGatewayAddress)
              .setNetwork(Prefix.ZERO)
              .build();
      cfgNode.getDefaultVrf().getStaticRoutes().add(defaultRoute);

      for (Ip ip : netInterface.getIpAddressAssociations().keySet()) {
        if (!ifaceSubnet.containsIp(ip)) {
          warnings.pedantic(
              String.format(
                  "Instance subnet \"%s\" does not contain private ip: \"%s\"", ifaceSubnet, ip));
          continue;
        }

        if (ip.equals(ifaceSubnet.getEndIp())) {
          warnings.pedantic(
              String.format("Expected end address \"%s\" to be used by generated subnet node", ip));
          continue;
        }

        ConcreteInterfaceAddress address =
            ConcreteInterfaceAddress.create(ip, ifaceSubnet.getPrefixLength());
        ifaceAddressesBuilder.add(address);
      }
      SortedSet<ConcreteInterfaceAddress> ifaceAddresses = ifaceAddressesBuilder.build();
      Interface iface = Utils.newInterface(interfaceId, cfgNode, ifaceAddresses.first());
      iface.setAllAddresses(ifaceAddresses);

      cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
      cfgNode.getVendorFamily().getAws().setSubnetId(_subnetId);
      cfgNode.getVendorFamily().getAws().setRegion(region.getName());
    }

    Utils.processSecurityGroups(region, cfgNode, _securityGroups, warnings);

    return cfgNode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Instance instance = (Instance) o;
    return Objects.equals(_instanceId, instance._instanceId)
        && Objects.equals(_networkInterfaces, instance._networkInterfaces)
        && Objects.equals(_securityGroups, instance._securityGroups)
        && _status == instance._status
        && Objects.equals(_subnetId, instance._subnetId)
        && Objects.equals(_tags, instance._tags)
        && Objects.equals(_vpcId, instance._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _instanceId,
        _networkInterfaces,
        _securityGroups,
        _status.ordinal(),
        _subnetId,
        _tags,
        _vpcId);
  }
}
