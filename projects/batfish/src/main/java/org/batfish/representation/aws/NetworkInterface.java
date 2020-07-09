package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a network interface in an AWS VPC.
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-network-interfaces.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class NetworkInterface implements AwsVpcEntity, Serializable {

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
    String getInstanceId() {
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
    String getId() {
      return _id;
    }
  }

  @Nullable private final String _attachmentInstanceId;

  @Nonnull private final String _description;

  @Nonnull private final List<String> _groups;

  @Nonnull private final String _networkInterfaceId;

  @Nonnull private final List<PrivateIpAddress> _privateIpAddresses;

  @Nonnull private final String _subnetId;

  @Nonnull private final String _vpcId;

  @Nonnull private final Map<String, String> _tags;

  @JsonCreator
  private static NetworkInterface create(
      @Nullable @JsonProperty(JSON_KEY_NETWORK_INTERFACE_ID) String networkInterfaceId,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_GROUPS) List<Group> groups,
      @Nullable @JsonProperty(JSON_KEY_PRIVATE_IP_ADDRESSES)
          List<PrivateIpAddress> privateIpAddresses,
      @Nullable @JsonProperty(JSON_KEY_DESCRIPTION) String description,
      @Nullable @JsonProperty(JSON_KEY_ATTACHMENT) Attachment attachment,
      @Nullable @JsonProperty(JSON_KEY_TAGSET) List<Tag> tags) {
    /*
     We do not parse the top-level privateIpAddress field -- that address shows up in privateIpAddresses list.
     We do not parse the top-level association field -- that object shows up as a sub-field of privateIpAddresses.
    */
    // all top-level keys other than attachment are mandatory
    checkNonNull(networkInterfaceId, JSON_KEY_NETWORK_INTERFACE_ID, "NetworkInterface");
    checkNonNull(subnetId, JSON_KEY_SUBNET_ID, "NetworkInterface");
    checkNonNull(vpcId, JSON_KEY_VPC_ID, "NetworkInterface");
    checkNonNull(groups, JSON_KEY_GROUPS, "NetworkInterface");
    checkNonNull(privateIpAddresses, JSON_KEY_PRIVATE_IP_ADDRESSES, "NetworkInterface");
    checkNonNull(description, JSON_KEY_DESCRIPTION, "NetworkInterface");

    return new NetworkInterface(
        networkInterfaceId,
        subnetId,
        vpcId,
        groups.stream().map(Group::getId).collect(ImmutableList.toImmutableList()),
        privateIpAddresses,
        description,
        attachment == null ? null : attachment.getInstanceId(),
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)));
  }

  public @Nonnull String getHumanName() {
    // TODO: it looks like _description is typically not useful. Are there other human names?
    return _networkInterfaceId;
  }

  public NetworkInterface(
      String networkInterfaceId,
      String subnetId,
      String vpcId,
      List<String> groups,
      List<PrivateIpAddress> privateIpAddresses,
      String description,
      @Nullable String attachmentInstanceId,
      Map<String, String> tags) {
    _networkInterfaceId = networkInterfaceId;
    _subnetId = subnetId;
    _vpcId = vpcId;
    _groups = groups;
    _privateIpAddresses = privateIpAddresses;
    _description = description;
    _attachmentInstanceId = attachmentInstanceId;
    _tags = tags;

    // sanity check that we have at least one primary ip
    getPrimaryPrivateIp();
  }

  @Nonnull
  public PrivateIpAddress getPrimaryPrivateIp() {
    return _privateIpAddresses.stream()
        .filter(PrivateIpAddress::isPrimary)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No primary private Ip address found"));
  }

  @Nullable
  public String getAttachmentInstanceId() {
    return _attachmentInstanceId;
  }

  @Nonnull
  public String getDescription() {
    return _description;
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
  public List<PrivateIpAddress> getPrivateIpAddresses() {
    return _privateIpAddresses;
  }

  @Nonnull
  public String getSubnetId() {
    return _subnetId;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  @Nonnull
  public Map<String, String> getTags() {
    return _tags;
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
    return Objects.equals(_attachmentInstanceId, that._attachmentInstanceId)
        && Objects.equals(_description, that._description)
        && Objects.equals(_groups, that._groups)
        && Objects.equals(_privateIpAddresses, that._privateIpAddresses)
        && Objects.equals(_networkInterfaceId, that._networkInterfaceId)
        && Objects.equals(_subnetId, that._subnetId)
        && Objects.equals(_vpcId, that._vpcId)
        && Objects.equals(_tags, that._tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _attachmentInstanceId,
        _description,
        _groups,
        _privateIpAddresses,
        _networkInterfaceId,
        _subnetId,
        _vpcId,
        _tags);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_attachmentInstanceId", _attachmentInstanceId)
        .add("_description", _description)
        .add("_groups", _groups)
        .add("_ipAddressAssociations", _privateIpAddresses)
        .add("_networkInterfaceId", _networkInterfaceId)
        .add("_subnetId", _subnetId)
        .add("_vpcId", _vpcId)
        .add("_tags", _tags)
        .toString();
  }
}
