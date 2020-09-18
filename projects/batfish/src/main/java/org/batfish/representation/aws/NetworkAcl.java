package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/** Represents an AWS network ACL */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class NetworkAcl implements AwsVpcEntity, Serializable {

  private static final String SOURCE_TYPE_NAME = "Network ACL";

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  public static final class NetworkAclAssociation implements Serializable {

    @Nonnull private final String _subnetId;

    @JsonCreator
    private static NetworkAclAssociation create(
        @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId) {
      checkArgument(subnetId != null, "Subnet id cannot be null for network ACL association");
      return new NetworkAclAssociation(subnetId);
    }

    public NetworkAclAssociation(String subnetId) {
      _subnetId = subnetId;
    }

    @Nonnull
    String getSubnetId() {
      return _subnetId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof NetworkAclAssociation)) {
        return false;
      }
      NetworkAclAssociation that = (NetworkAclAssociation) o;
      return Objects.equals(_subnetId, that._subnetId);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_subnetId);
    }
  }

  @Nonnull private final List<NetworkAclEntry> _entries;

  @Nonnull private final List<NetworkAclAssociation> _networkAclAssociations;

  @Nonnull private final String _networkAclId;

  @Nonnull private final String _vpcId;

  private final boolean _isDefault;

  @JsonCreator
  private static NetworkAcl create(
      @Nullable @JsonProperty(JSON_KEY_NETWORK_ACL_ID) String networkAclId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_ASSOCIATIONS) List<NetworkAclAssociation> associations,
      @Nullable @JsonProperty(JSON_KEY_ENTRIES) List<NetworkAclEntry> entries,
      @Nullable @JsonProperty(JSON_KEY_IS_DEFAULT) Boolean isDefault) {
    checkArgument(networkAclId != null, "Network ACL id cannot be null");
    checkArgument(vpcId != null, "VPC id cannot be null for network ACL");
    checkArgument(associations != null, "Associations list cannot be null for network ACL");
    checkArgument(entries != null, "Entries list cannot be null for network ACL");
    checkArgument(isDefault != null, "IsDefault cannot be null for network ACL");

    return new NetworkAcl(networkAclId, vpcId, associations, entries, isDefault);
  }

  public NetworkAcl(
      String networkAclId,
      String vpcId,
      List<NetworkAclAssociation> associations,
      List<NetworkAclEntry> entries,
      boolean isDefault) {
    _networkAclId = networkAclId;
    _vpcId = vpcId;
    _networkAclAssociations = associations;
    _entries = entries;
    _isDefault = isDefault;
  }

  private IpAccessList getAcl(boolean isEgress) {
    List<AclLine> lines =
        _entries.stream()
            .filter(e -> e instanceof NetworkAclEntryV4) // ignore v6
            .filter(e -> (isEgress && e.getIsEgress()) || (!isEgress && !e.getIsEgress()))
            .map(e -> getAclLine((NetworkAclEntryV4) e))
            .collect(ImmutableList.toImmutableList());
    IpAccessList list =
        IpAccessList.builder()
            .setName(getAclName(_networkAclId, isEgress))
            .setLines(lines)
            .setSourceName(_networkAclId)
            .setSourceType(NetworkAcl.SOURCE_TYPE_NAME)
            .build();
    return list;
  }

  @VisibleForTesting
  static ExprAclLine getAclLine(NetworkAclEntryV4 entry) {
    int key = entry.getRuleNumber();
    LineAction action = entry.getIsAllow() ? LineAction.PERMIT : LineAction.DENY;
    Prefix prefix = entry.getCidrBlock();
    HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();
    if (!prefix.equals(Prefix.ZERO)) {
      if (entry.getIsEgress()) {
        headerSpaceBuilder.setDstIps(ImmutableSortedSet.of(IpWildcard.create(prefix)));
      } else {
        headerSpaceBuilder.setSrcIps(ImmutableSortedSet.of(IpWildcard.create(prefix)));
      }
    }
    IpProtocol protocol = Utils.toIpProtocol(entry.getProtocol());
    String protocolStr = protocol != null ? protocol.toString() : "ALL";
    if (protocol != null) {
      headerSpaceBuilder.setIpProtocols(ImmutableSortedSet.of(protocol));
    }
    int fromPort = entry.getPortRange() == null ? 0 : entry.getPortRange().getFrom();
    int toPort = entry.getPortRange() == null ? 65535 : entry.getPortRange().getTo();
    SubRange portRange = new SubRange(fromPort, toPort);
    if (fromPort != 0 || toPort != 65535) {
      headerSpaceBuilder.setDstPorts(ImmutableSortedSet.of(portRange));
    }
    if (protocol == IpProtocol.ICMP) {
      assert entry.getIcmpTypeCode() != null; // avoid null pointer warning
      if (entry.getIcmpTypeCode().getType() != -1) {
        headerSpaceBuilder.setIcmpTypes(
            ImmutableList.of(SubRange.singleton(entry.getIcmpTypeCode().getType())));
      }
      if (entry.getIcmpTypeCode().getCode() != -1) {
        headerSpaceBuilder.setIcmpCodes(
            ImmutableList.of(SubRange.singleton(entry.getIcmpTypeCode().getCode())));
      }
    }
    String portStr;
    if (protocol == IpProtocol.ICMP) {
      if (entry.getIcmpTypeCode().getType() == -1) {
        portStr = "ALL";
      } else {
        portStr =
            String.format(
                "[type=%s, code=%s]",
                entry.getIcmpTypeCode().getType(),
                entry.getIcmpTypeCode().getCode() == -1
                    ? "ALL"
                    : entry.getIcmpTypeCode().getCode());
      }
    } else if ((fromPort == 0 && toPort == 65535)) {
      portStr = "ALL";
    } else {
      portStr = portRange.toString();
    }
    return ExprAclLine.builder()
        .setAction(action)
        .setMatchCondition(new MatchHeaderSpace(headerSpaceBuilder.build()))
        .setName(getAclLineName(key, protocolStr, portStr, prefix, action))
        .build();
  }

  public static String getAclName(String networkAclId, boolean isEgress) {
    return networkAclId + (isEgress ? "_egress" : "_ingress");
  }

  static String getAclLineName(
      int lineNumber, String protocolStr, String portStr, Prefix prefix, LineAction action) {
    String actionStr = action == LineAction.PERMIT ? "ALLOW" : "DENY";
    String lineNumberStr = lineNumber == 32767 ? "*" : Integer.toString(lineNumber);
    // order mimics how things are ordered on the AWS console
    return String.format("%s %s %s %s %s", lineNumberStr, protocolStr, portStr, prefix, actionStr);
  }

  List<NetworkAclAssociation> getAssociations() {
    return _networkAclAssociations;
  }

  IpAccessList getEgressAcl() {
    return getAcl(true);
  }

  @Override
  public String getId() {
    return _networkAclId;
  }

  IpAccessList getIngressAcl() {
    return getAcl(false);
  }

  public boolean isDefault() {
    return _isDefault;
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
    if (!(o instanceof NetworkAcl)) {
      return false;
    }
    NetworkAcl that = (NetworkAcl) o;
    return Objects.equals(_entries, that._entries)
        && Objects.equals(_networkAclAssociations, that._networkAclAssociations)
        && Objects.equals(_networkAclId, that._networkAclId)
        && Objects.equals(_vpcId, that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_entries, _networkAclAssociations, _networkAclId, _vpcId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("networkAclId", _networkAclId)
        .add("vpcId", _vpcId)
        .add("associations", _networkAclAssociations)
        .add("entries", _entries)
        .toString();
  }
}
