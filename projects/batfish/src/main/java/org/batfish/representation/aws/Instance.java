package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.immutableEntry;
import static org.batfish.representation.aws.AwsLocationInfoUtils.INSTANCE_INTERFACE_LINK_LOCATION_INFO;
import static org.batfish.representation.aws.AwsLocationInfoUtils.instanceInterfaceLocationInfo;
import static org.batfish.representation.aws.Utils.addNodeToSubnet;
import static org.batfish.representation.aws.Utils.checkNonNull;
import static org.batfish.representation.aws.Utils.createPublicIpsRefBook;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Ip;

/** Representation for an EC2 instance */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class Instance implements AwsVpcEntity, Serializable {

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

    private final @Nonnull String _id;

    @JsonCreator
    private static NetworkInterfaceId create(
        @JsonProperty(JSON_KEY_NETWORK_INTERFACE_ID) @Nullable String id) {
      checkArgument(id != null, "Security group id is null");
      return new NetworkInterfaceId(id);
    }

    private NetworkInterfaceId(String id) {
      _id = id;
    }

    public @Nonnull String getId() {
      return _id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class SecurityGroupId {

    private final @Nonnull String _id;

    @JsonCreator
    private static SecurityGroupId create(@JsonProperty(JSON_KEY_GROUP_ID) @Nullable String id) {
      checkArgument(id != null, "Security group id is null");
      return new SecurityGroupId(id);
    }

    private SecurityGroupId(String id) {
      _id = id;
    }

    public @Nonnull String getId() {
      return _id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class State {

    private final @Nonnull Status _name;

    @JsonCreator
    private static State create(@JsonProperty("Name") @Nullable String name) {
      checkArgument(name != null, "Name of State is null");
      return new State(Status.fromString(name));
    }

    private State(Status name) {
      _name = name;
    }

    public @Nonnull Status getName() {
      return _name;
    }
  }

  private final @Nonnull String _instanceId;

  private final @Nonnull List<String> _networkInterfaces;

  private final @Nullable Ip _primaryPrivateIpAddress;

  // Should be unused - this is redundant with {@link NetworkInterface#getGroups()}
  private final @Nonnull List<String> _securityGroups;

  private final @Nonnull Status _status;

  private final @Nullable String _subnetId;

  private final @Nonnull Map<String, String> _tags;

  private final @Nullable String _vpcId;

  @JsonCreator
  private static Instance create(
      @JsonProperty(JSON_KEY_INSTANCE_ID) @Nullable String instanceId,
      @JsonProperty(JSON_KEY_VPC_ID) @Nullable String vpcId,
      @JsonProperty(JSON_KEY_SUBNET_ID) @Nullable String subnetId,
      @JsonProperty(JSON_KEY_SECURITY_GROUPS) @Nullable List<SecurityGroupId> securityGroups,
      @JsonProperty(JSON_KEY_NETWORK_INTERFACES) @Nullable
          List<NetworkInterfaceId> networkInterfaces,
      @JsonProperty(JSON_KEY_PRIVATE_IP_ADDRESS) @Nullable Ip privateIpAddress,
      @JsonProperty(JSON_KEY_TAGS) @Nullable List<Tag> tags,
      @JsonProperty(JSON_KEY_STATE) @Nullable State state) {

    checkNonNull(instanceId, "InstanceId", "Instance");
    checkArgument(
        (vpcId == null && subnetId == null) || (vpcId != null && subnetId != null),
        "Only one of vpcId ('%s') and subnetId ('%s') is null",
        vpcId,
        subnetId);
    checkNonNull(securityGroups, "Security groups", "Instance");
    checkNonNull(networkInterfaces, "Network interfaces", "Instance");
    checkNonNull(state, "State", "Instance");

    // we don't know if Placement can be null. assuming for now that it can be

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
        privateIpAddress,
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)),
        state.getName());

    // check if the public and private ip addresses are associated with an
    // interface
  }

  public Instance(
      String instanceId,
      @Nullable String vpcId,
      @Nullable String subnetId,
      List<String> securityGroups,
      List<String> networkInterfaces,
      @Nullable Ip primaryPrivateIpAddress,
      Map<String, String> tags,
      Status status) {
    _instanceId = instanceId;
    _vpcId = vpcId;
    _subnetId = subnetId;
    _securityGroups = securityGroups;
    _networkInterfaces = networkInterfaces;
    _primaryPrivateIpAddress = primaryPrivateIpAddress;
    _tags = tags;
    _status = status;
  }

  static InstanceBuilder builder() {
    return new InstanceBuilder();
  }

  public @Nonnull String getHumanName() {
    String tag = _tags.get(TAG_NAME);
    if (tag == null) {
      return _instanceId;
    }
    return String.format("%s (%s)", _instanceId, tag);
  }

  @Override
  public String getId() {
    return _instanceId;
  }

  public @Nonnull List<String> getNetworkInterfaces() {
    return _networkInterfaces;
  }

  public @Nullable Ip getPrimaryPrivateIpAddress() {
    return _primaryPrivateIpAddress;
  }

  public @Nonnull List<String> getSecurityGroups() {
    return _securityGroups;
  }

  public @Nonnull Status getStatus() {
    return _status;
  }

  public @Nullable String getSubnetId() {
    return _subnetId;
  }

  public @Nonnull Map<String, String> getTags() {
    return _tags;
  }

  public @Nullable String getVpcId() {
    return _vpcId;
  }

  Configuration toConfigurationNode(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(
            instanceHostname(_instanceId), "aws", _tags, DeviceModel.AWS_EC2_INSTANCE);
    cfgNode.setDeviceType(DeviceType.HOST);
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setSubnetId(_subnetId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    for (String interfaceId : _networkInterfaces) {

      NetworkInterface netInterface = region.getNetworkInterfaces().get(interfaceId);
      if (netInterface == null) {
        warnings.redFlagf(
            "Network interface \"%s\" for instance \"%s\" not found", interfaceId, _instanceId);
        continue;
      }
      Subnet subnet = region.getSubnets().get(netInterface.getSubnetId());

      addNodeToSubnet(cfgNode, netInterface, subnet, awsConfiguration, warnings);
    }

    addPublicIpsRefBook(cfgNode, region);

    // create LocationInfo for each link location on the instance.
    cfgNode.setLocationInfo(
        cfgNode.getAllInterfaces().values().stream()
            .flatMap(
                iface ->
                    Stream.of(
                        immutableEntry(
                            interfaceLocation(iface), instanceInterfaceLocationInfo(iface)),
                        immutableEntry(
                            interfaceLinkLocation(iface), INSTANCE_INTERFACE_LINK_LOCATION_INFO)))
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue)));

    return cfgNode;
  }

  /** Adds a generated references book for public Ips if the instance has any such Ips */
  @VisibleForTesting
  void addPublicIpsRefBook(Configuration cfgNode, Region region) {
    createPublicIpsRefBook(
        _networkInterfaces.stream()
            .map(id -> region.getNetworkInterfaces().get(id))
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList()),
        cfgNode);
  }

  public static String instanceHostname(String instanceId) {
    return instanceId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Instance)) {
      return false;
    }
    Instance instance = (Instance) o;
    return Objects.equals(_instanceId, instance._instanceId)
        && Objects.equals(_networkInterfaces, instance._networkInterfaces)
        && Objects.equals(_primaryPrivateIpAddress, instance._primaryPrivateIpAddress)
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
        _primaryPrivateIpAddress,
        _securityGroups,
        _status.ordinal(),
        _subnetId,
        _tags,
        _vpcId);
  }

  static final class InstanceBuilder {
    private String _instanceId;
    private List<String> _networkInterfaces;
    private Ip _primaryPrivateIpAddress;
    private List<String> _securityGroups;
    private Status _status;
    private String _subnetId;
    private Map<String, String> _tags;
    private String _vpcId;

    private InstanceBuilder() {}

    public InstanceBuilder setInstanceId(String instanceId) {
      _instanceId = instanceId;
      return this;
    }

    public InstanceBuilder setNetworkInterfaces(List<String> networkInterfaces) {
      _networkInterfaces = networkInterfaces;
      return this;
    }

    public InstanceBuilder setPrimaryPrivateIpAddress(Ip primaryPrivateIpAddress) {
      _primaryPrivateIpAddress = primaryPrivateIpAddress;
      return this;
    }

    public InstanceBuilder setSecurityGroups(List<String> securityGroups) {
      _securityGroups = securityGroups;
      return this;
    }

    public InstanceBuilder setStatus(Status status) {
      _status = status;
      return this;
    }

    public InstanceBuilder setSubnetId(String subnetId) {
      _subnetId = subnetId;
      return this;
    }

    public InstanceBuilder setTags(Map<String, String> tags) {
      _tags = tags;
      return this;
    }

    public InstanceBuilder setVpcId(String vpcId) {
      _vpcId = vpcId;
      return this;
    }

    public Instance build() {
      checkArgument(_instanceId != null, "Instance id must be set");
      return new Instance(
          _instanceId,
          _vpcId,
          _subnetId,
          firstNonNull(_securityGroups, new LinkedList<>()),
          firstNonNull(_networkInterfaces, new LinkedList<>()),
          _primaryPrivateIpAddress,
          firstNonNull(_tags, new HashMap<>()),
          firstNonNull(_status, Status.RUNNING));
    }
  }
}
