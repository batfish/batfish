package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Represents a network interface in an AWS VPC */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class NetworkInterface implements AwsVpcEntity, Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class Association {

    @Nonnull private final Ip _publicIp;

    @JsonCreator
    private static Association create(@Nullable @JsonProperty(JSON_KEY_PUBLIC_IP) Ip publicIp) {
      checkArgument(publicIp != null, "Public IP cannot be null in network interface association");
      return new Association(publicIp);
    }

    private Association(Ip publicIp) {
      _publicIp = publicIp;
    }

    @Nonnull
    public Ip getPublicIp() {
      return _publicIp;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class Attachment {

    @Nullable private final String _instanceId;

    @JsonCreator
    private static Attachment create(
        @Nullable @JsonProperty(JSON_KEY_STATUS) String status,
        @Nullable @JsonProperty(JSON_KEY_INSTANCE_ID) String instanceId) {
      checkArgument(status != null, "Attachment status cannot be null for network interface");

      // pay attention to instance id only we are attached
      return new Attachment(status.equals("attached") ? instanceId : null);
    }

    private Attachment(@Nullable String instanceId) {
      _instanceId = instanceId;
    }

    @Nullable
    public String getInstanceId() {
      return _instanceId;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class Group {

    @Nonnull private final String _id;

    @JsonCreator
    private static Group create(@Nullable @JsonProperty(JSON_KEY_GROUP_ID) String id) {
      checkArgument(id != null, "Id cannot be null in network interface group");
      return new Group(id);
    }

    private Group(String id) {
      _id = id;
    }

    @Nonnull
    public String getId() {
      return _id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class PrivateIpAddress {

    @Nonnull private final Ip _privateIp;

    @Nullable private final Ip _publicIp;

    @JsonCreator
    private static PrivateIpAddress create(
        @Nullable @JsonProperty(JSON_KEY_PRIVATE_IP_ADDRESS) Ip privateIp,
        @Nullable @JsonProperty(JSON_KEY_ASSOCIATION) Association association) {
      checkArgument(privateIp != null, "Private IP cannot be null for network interface");

      return new PrivateIpAddress(
          privateIp, association == null ? null : association.getPublicIp());
    }

    private PrivateIpAddress(Ip privateIp, @Nullable Ip publicIp) {
      _privateIp = privateIp;
      _publicIp = publicIp;
    }

    @Nonnull
    public Ip getPrivateIp() {
      return _privateIp;
    }

    @Nullable
    public Ip getPublicIp() {
      return _publicIp;
    }
  }

  @Nullable private final Ip _associationPublicIp;

  @Nullable private final String _attachmentInstanceId;

  @Nonnull private final List<String> _groups;

  @Nonnull private final Map<Ip, Ip> _ipAddressAssociations;

  @Nonnull private final String _networkInterfaceId;

  @Nonnull private final String _subnetId;

  @Nonnull private final String _vpcId;

  @JsonCreator
  private static NetworkInterface create(
      @Nullable @JsonProperty(JSON_KEY_NETWORK_INTERFACE_ID) String networkInterfaceId,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_GROUPS) List<Group> groups,
      @Nullable @JsonProperty(JSON_KEY_PRIVATE_IP_ADDRESSES)
          List<PrivateIpAddress> privateIpAddresses,
      @Nullable @JsonProperty(JSON_KEY_ASSOCIATION) Association association,
      @Nullable @JsonProperty(JSON_KEY_ATTACHMENT) Attachment attachment) {
    // all top-level keys other than association and attachment are mandatory
    checkArgument(networkInterfaceId != null, "Network interface id cannot be null");
    checkArgument(subnetId != null, "Subnet id cannot be null for network interface");
    checkArgument(vpcId != null, "VPC id cannot be null for network interface");
    checkArgument(groups != null, "Group list cannot be null for network interface");
    checkArgument(
        privateIpAddresses != null, "Private IP address list cannot be null for network interface");

    HashMap<Ip, Ip> addressMap = new HashMap<>();
    privateIpAddresses.forEach(p -> addressMap.put(p.getPrivateIp(), p.getPublicIp()));

    return new NetworkInterface(
        networkInterfaceId,
        subnetId,
        vpcId,
        groups.stream().map(Group::getId).collect(ImmutableList.toImmutableList()),
        addressMap,
        association == null ? null : association.getPublicIp(),
        attachment == null ? null : attachment.getInstanceId());
  }

  public NetworkInterface(
      String networkInterfaceId,
      String subnetId,
      String vpcId,
      List<String> groups,
      Map<Ip, Ip> ipAddressAssociations,
      @Nullable Ip associationPublicIp,
      @Nullable String attachmentInstanceId) {
    _networkInterfaceId = networkInterfaceId;
    _subnetId = subnetId;
    _vpcId = vpcId;
    _groups = groups;
    _ipAddressAssociations = ipAddressAssociations;
    _associationPublicIp = associationPublicIp;
    _attachmentInstanceId = attachmentInstanceId;
  }

  @Nullable
  public Ip getAssociationPublicIp() {
    return _associationPublicIp;
  }

  @Nullable
  public String getAttachmentInstanceId() {
    return _attachmentInstanceId;
  }

  @Nonnull
  public List<String> getGroups() {
    return _groups;
  }

  @Override
  public String getId() {
    return _networkInterfaceId;
  }

  @Nonnull
  public Map<Ip, Ip> getIpAddressAssociations() {
    return _ipAddressAssociations;
  }

  @Nonnull
  public String getNetworkInterfaceId() {
    return _networkInterfaceId;
  }

  @Nonnull
  public String getSubnetId() {
    return _subnetId;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkInterface that = (NetworkInterface) o;
    return Objects.equals(_associationPublicIp, that._associationPublicIp)
        && Objects.equals(_attachmentInstanceId, that._attachmentInstanceId)
        && Objects.equals(_groups, that._groups)
        && Objects.equals(_ipAddressAssociations, that._ipAddressAssociations)
        && Objects.equals(_networkInterfaceId, that._networkInterfaceId)
        && Objects.equals(_subnetId, that._subnetId)
        && Objects.equals(_vpcId, that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _associationPublicIp,
        _attachmentInstanceId,
        _groups,
        _ipAddressAssociations,
        _networkInterfaceId,
        _subnetId,
        _vpcId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_associationPublicIp", _associationPublicIp)
        .add("_attachmentInstanceId", _attachmentInstanceId)
        .add("_groups", _groups)
        .add("_ipAddressAssociations", _ipAddressAssociations)
        .add("_networkInterfaceId", _networkInterfaceId)
        .add("_subnetId", _subnetId)
        .add("_vpcId", _vpcId)
        .toString();
  }
}
