package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
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
  static final class NetworkAclAssociation implements Serializable {

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
    public String getSubnetId() {
      return _subnetId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
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

  @JsonCreator
  private static NetworkAcl create(
      @Nullable @JsonProperty(JSON_KEY_NETWORK_ACL_ID) String networkAclId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_ASSOCIATIONS) List<NetworkAclAssociation> associations,
      @Nullable @JsonProperty(JSON_KEY_ENTRIES) List<NetworkAclEntry> entries) {
    checkArgument(networkAclId != null, "Network ACL id cannot be null");
    checkArgument(vpcId != null, "VPC id cannot be null for network ACL");
    checkArgument(associations != null, "Associations list cannot be null for network ACL");
    checkArgument(entries != null, "Entries list cannot be null for network ACL");

    return new NetworkAcl(networkAclId, vpcId, associations, entries);
  }

  public NetworkAcl(
      String networkAclId,
      String vpcId,
      List<NetworkAclAssociation> associations,
      List<NetworkAclEntry> entries) {
    _networkAclId = networkAclId;
    _vpcId = vpcId;
    _networkAclAssociations = associations;
    _entries = entries;
  }

  private IpAccessList getAcl(boolean isEgress) {
    String listName = _networkAclId + (isEgress ? "_egress" : "_ingress");
    Map<Integer, IpAccessListLine> lineMap = new TreeMap<>();
    for (NetworkAclEntry entry : _entries) {
      if ((isEgress && entry.getIsEgress()) || (!isEgress && !entry.getIsEgress())) {
        int key = entry.getRuleNumber();
        LineAction action = entry.getIsAllow() ? LineAction.PERMIT : LineAction.DENY;
        Prefix prefix = entry.getCidrBlock();
        HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();
        if (!prefix.equals(Prefix.ZERO)) {
          if (isEgress) {
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
        String portStr;
        if (protocol == IpProtocol.ICMP) {
          // TODO: flesh these out
          portStr = "some ICMP type(s)/code(s)";
        } else if ((fromPort == 0 && toPort == 65535)) {
          portStr = "ALL";
        } else {
          portStr = portRange.toString();
        }
        String actionStr = action == LineAction.PERMIT ? "ALLOW" : "DENY";
        String lineNumber = key == 32767 ? "*" : Integer.toString(key);
        lineMap.put(
            key,
            IpAccessListLine.builder()
                .setAction(action)
                .setMatchCondition(new MatchHeaderSpace(headerSpaceBuilder.build()))
                .setName(
                    String.format(
                        "%s %s %s %s %s", lineNumber, protocolStr, portStr, prefix, actionStr))
                .build());
      }
    }
    List<IpAccessListLine> lines = ImmutableList.copyOf(lineMap.values());
    IpAccessList list =
        IpAccessList.builder()
            .setName(listName)
            .setLines(lines)
            .setSourceName(_networkAclId)
            .setSourceType(NetworkAcl.SOURCE_TYPE_NAME)
            .build();
    return list;
  }

  public List<NetworkAclAssociation> getAssociations() {
    return _networkAclAssociations;
  }

  public IpAccessList getEgressAcl() {
    return getAcl(true);
  }

  @Override
  public String getId() {
    return _networkAclId;
  }

  public IpAccessList getIngressAcl() {
    return getAcl(false);
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
