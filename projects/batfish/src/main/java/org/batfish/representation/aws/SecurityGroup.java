package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;

/** Represents an AWS security group */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class SecurityGroup implements AwsVpcEntity, Serializable {
  static final String INGRESS = "INGRESS";
  static final String EGRESS = "EGRESS";

  @Nullable private final String _description;

  @Nonnull private final String _groupId;

  @Nonnull private final String _groupName;

  @Nonnull private final List<IpPermissions> _ipPermsEgress;

  @Nonnull private final List<IpPermissions> _ipPermsIngress;

  /** IPs and instance names of the instances which refer to this security group */
  @Nonnull private final Map<Ip, String> _referrerIps;

  @Nonnull private final Map<String, String> _tags;

  @Nonnull private final String _vpcId;

  @JsonCreator
  private static SecurityGroup create(
      @Nullable @JsonProperty(JSON_KEY_DESCRIPTION) String description,
      @Nullable @JsonProperty(JSON_KEY_GROUP_ID) String groupId,
      @Nullable @JsonProperty(JSON_KEY_GROUP_NAME) String groupName,
      @Nullable @JsonProperty(JSON_KEY_IP_PERMISSIONS_EGRESS) List<IpPermissions> ipPermsEgress,
      @Nullable @JsonProperty(JSON_KEY_IP_PERMISSIONS) List<IpPermissions> ipPermsIngress,
      @Nullable @JsonProperty(JSON_KEY_TAGS) List<Tag> tags,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId) {
    checkArgument(groupId != null, "Group id cannot be null for security groups");
    checkArgument(groupName != null, "Group name cannot be null for security groups");
    checkArgument(
        ipPermsEgress != null, "Egress IP permissions list cannot be null for security groups");
    checkArgument(
        ipPermsIngress != null, "Ingress IP permissions list cannot be null for security groups");
    checkArgument(vpcId != null, "VPC Id cannot be null for security groups");
    return new SecurityGroup(
        description,
        groupId,
        groupName,
        ipPermsEgress,
        ipPermsIngress,
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)),
        vpcId);
  }

  public SecurityGroup(
      String groupId,
      String groupName,
      List<IpPermissions> ipPermsEgress,
      List<IpPermissions> ipPermsIngress,
      String vpcId) {
    this(null, groupId, groupName, ipPermsEgress, ipPermsIngress, ImmutableMap.of(), vpcId);
  }

  public SecurityGroup(
      @Nullable String description,
      String groupId,
      String groupName,
      List<IpPermissions> ipPermsEgress,
      List<IpPermissions> ipPermsIngress,
      Map<String, String> tags,
      String vpcId) {
    _description = description;
    _groupId = groupId;
    _groupName = groupName;
    _ipPermsEgress = ipPermsEgress;
    _ipPermsIngress = ipPermsIngress;
    _referrerIps = new HashMap<>();
    _tags = tags;
    _vpcId = vpcId;
  }

  IpAccessList toAcl(Region region, boolean ingress, Warnings warnings) {
    List<AclLine> aclLines = toAclLines(region, ingress, warnings);
    return IpAccessList.builder().setName(getViName(ingress)).setLines(aclLines).build();
  }

  /**
   * Returns the name that will be assigned to the VI {@link IpAccessList} representing this
   * security group's ingress or egress permissions.
   */
  public String getViName(boolean ingress) {
    // See note about naming on SecurityGroup#getGroupName.
    return String.format(
        "~%s~SECURITY-GROUP~%s~%s~", ingress ? INGRESS : EGRESS, _groupName, _groupId);
  }

  /** Converts this security group's ingress or egress permission terms to List of AclLines */
  List<AclLine> toAclLines(Region region, boolean ingress, Warnings warnings) {
    List<IpPermissions> ipPerms = ingress ? _ipPermsIngress : _ipPermsEgress;
    return ipPerms.stream()
        .map(
            rule ->
                // NOTE: Keep VI ACL lines 1-to-1 with group's IpPermissions; do not filter
                rule.toIpAccessListLine(
                    ingress,
                    region,
                    String.format(
                        "%s - %s [%s]", _groupId, _groupName, ingress ? "ingress" : "egress"),
                    warnings))
        .collect(ImmutableList.toImmutableList());
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  /**
   * Note: it is tempting to follow the strategy used elsewhere in AWS, using the {@code Name} tag,
   * if present, instead of the {@code Group Name}. However, we specifically chose not to do this
   * because AWS displays security groups or auto-completes them in a search solely based on {@code
   * Group Name}, not on the Name Tag.
   */
  @Nonnull
  public String getGroupName() {
    return _groupName;
  }

  @Override
  public String getId() {
    return _groupId;
  }

  @Nonnull
  public List<IpPermissions> getIpPermsEgress() {
    return _ipPermsEgress;
  }

  @Nonnull
  public List<IpPermissions> getIpPermsIngress() {
    return _ipPermsIngress;
  }

  Map<Ip, String> getReferrerIps() {
    return _referrerIps;
  }

  @Nonnull
  public Map<String, String> getTags() {
    return _tags;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  void addReferrerIp(Ip ip, String referrer) {
    _referrerIps.put(ip, referrer);
  }

  void addReferrerIps(List<PrivateIpAddress> ips, String referrer) {
    ips.forEach(pip -> addReferrerIp(pip.getPrivateIp(), referrer));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SecurityGroup)) {
      return false;
    }
    SecurityGroup that = (SecurityGroup) o;
    return Objects.equals(_description, that._description)
        && Objects.equals(_groupId, that._groupId)
        && Objects.equals(_groupName, that._groupName)
        && Objects.equals(_ipPermsEgress, that._ipPermsEgress)
        && Objects.equals(_ipPermsIngress, that._ipPermsIngress)
        && Objects.equals(_referrerIps, that._referrerIps)
        && Objects.equals(_tags, that._tags)
        && Objects.equals(_vpcId, that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _description,
        _groupId,
        _groupName,
        _ipPermsEgress,
        _ipPermsIngress,
        _referrerIps,
        _tags,
        _vpcId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("description", _description)
        .add("groupId", _groupId)
        .add("groupName", _groupName)
        .add("ipPermsEgress", _ipPermsEgress)
        .add("ipPermsIngress", _ipPermsIngress)
        .add("referrerIps", _referrerIps)
        .add("tags", _tags)
        .add("vpcId", _vpcId)
        .toString();
  }
}
