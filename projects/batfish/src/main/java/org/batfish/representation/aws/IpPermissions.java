package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_CIDR_IP;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DESCRIPTION;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_FROM_PORT;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_GROUP_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_IP_PROTOCOL;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_IP_RANGES;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PREFIX_LIST_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PREFIX_LIST_IDS;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_TO_PORT;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_USER_GROUP_ID_PAIRS;
import static org.batfish.representation.aws.Utils.checkNonNull;
import static org.batfish.representation.aws.Utils.getTraceElementForRule;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceElementForDstPorts;
import static org.batfish.representation.aws.Utils.traceElementForIcmp;
import static org.batfish.representation.aws.Utils.traceElementForProtocol;

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
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/** IP packet permissions within AWS security groups */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class IpPermissions implements Serializable {

  /** Type of source/destination address */
  public enum AddressType {
    SECURITY_GROUP("Security Group"),
    PREFIX_LIST("Prefix List"),
    CIDR_IP("CIDR IP");

    private final String _name;

    AddressType(String name) {
      _name = name;
    }

    @Override
    public String toString() {
      return _name;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class IpRange implements Serializable {

    @Nullable private final String _description;
    @Nonnull private final Prefix _prefix;

    @JsonCreator
    private static IpRange create(
        @Nullable @JsonProperty(JSON_KEY_DESCRIPTION) String description,
        @Nullable @JsonProperty(JSON_KEY_CIDR_IP) Prefix prefix) {
      checkArgument(prefix != null, "Prefix cannot be null in IpRange");
      return new IpRange(description, prefix);
    }

    public IpRange(String description, Prefix prefix) {
      _description = description;
      _prefix = prefix;
    }

    public IpRange(Prefix prefix) {
      _description = null;
      _prefix = prefix;
    }

    @Nonnull
    public Prefix getPrefix() {
      return _prefix;
    }

    @Nullable
    public String getDescription() {
      return _description;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof IpRange)) {
        return false;
      }
      IpRange that = (IpRange) o;
      return Objects.equals(_prefix, that._prefix)
          && Objects.equals(_description, that._description);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_prefix, _description);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class PrefixListId implements Serializable {

    @Nonnull private final String _id;

    @JsonCreator
    private static PrefixListId create(@Nullable @JsonProperty(JSON_KEY_PREFIX_LIST_ID) String id) {
      checkNonNull(id, JSON_KEY_PREFIX_LIST_ID, "PrefixListIds");
      return new PrefixListId(id);
    }

    PrefixListId(String id) {
      _id = id;
    }

    @Nonnull
    public String getId() {
      return _id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof PrefixListId)) {
        return false;
      }
      PrefixListId that = (PrefixListId) o;
      return _id.equals(that._id);
    }

    @Override
    public int hashCode() {
      return _id.hashCode();
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class UserIdGroupPair implements Serializable {

    @Nonnull private final String _groupId;

    @JsonCreator
    private static UserIdGroupPair create(
        @Nullable @JsonProperty(JSON_KEY_GROUP_ID) String groupId) {
      checkArgument(groupId != null, "Group id cannot be null in user id group pair");
      return new UserIdGroupPair(groupId);
    }

    UserIdGroupPair(String groupId) {
      _groupId = groupId;
    }

    @Nonnull
    String getGroupId() {
      return _groupId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof UserIdGroupPair)) {
        return false;
      }
      UserIdGroupPair that = (UserIdGroupPair) o;
      return Objects.equals(_groupId, that._groupId);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_groupId);
    }
  }

  @Nullable private final Integer _fromPort;

  @Nonnull private final String _ipProtocol;

  @Nonnull private final List<IpRange> _ipRanges;

  @Nonnull private final List<String> _prefixList;

  @Nonnull private final List<String> _securityGroups;

  @Nullable private final Integer _toPort;

  @JsonCreator
  private static IpPermissions create(
      @Nullable @JsonProperty(JSON_KEY_IP_PROTOCOL) String ipProtocol,
      @Nullable @JsonProperty(JSON_KEY_FROM_PORT) Integer fromPort,
      @Nullable @JsonProperty(JSON_KEY_TO_PORT) Integer toPort,
      @Nullable @JsonProperty(JSON_KEY_IP_RANGES) List<IpRange> ipRanges,
      @Nullable @JsonProperty(JSON_KEY_PREFIX_LIST_IDS) List<PrefixListId> prefixes,
      @Nullable @JsonProperty(JSON_KEY_USER_GROUP_ID_PAIRS)
          List<UserIdGroupPair> userIdGroupPairs) {
    checkArgument(ipProtocol != null, "IP protocol cannot be null for IP permissions");
    checkArgument(ipRanges != null, "IP ranges cannot be null for IP permissions");
    checkArgument(
        userIdGroupPairs != null, "User Id groups pairs cannot be null for IP permissions");

    return new IpPermissions(
        ipProtocol,
        fromPort,
        toPort,
        ipRanges,
        firstNonNull(prefixes, ImmutableList.<PrefixListId>of()).stream()
            .map(PrefixListId::getId)
            .collect(ImmutableList.toImmutableList()),
        userIdGroupPairs.stream()
            .map(UserIdGroupPair::getGroupId)
            .collect(ImmutableList.toImmutableList()));
  }

  IpPermissions(
      String ipProtocol,
      @Nullable Integer fromPort,
      @Nullable Integer toPort,
      List<IpRange> ipRanges,
      List<String> prefixList,
      List<String> securityGroups) {
    _ipProtocol = ipProtocol;
    _fromPort = fromPort;
    _toPort = toPort;
    _ipRanges = ipRanges;
    _prefixList = prefixList;
    _securityGroups = securityGroups;
  }

  /**
   * Returns a Map containing all the Security Groups referred by this IPPermission instance and the
   * corresponding IpSpaces
   */
  private Map<SecurityGroup, IpSpace> collectSecurityGroups(Region region) {
    ImmutableMap.Builder<SecurityGroup, IpSpace> sgToIpSpace = ImmutableMap.builder();
    _securityGroups.stream()
        .map(sgID -> region.getSecurityGroups().get(sgID))
        .filter(Objects::nonNull)
        .distinct()
        .forEach(
            securityGroup ->
                securityGroup
                    .getUsersIpSpace()
                    .forEach(prefix -> sgToIpSpace.put(securityGroup, prefix.toIpSpace())));
    return sgToIpSpace.build();
  }

  /**
   * Returns a Map containing all the Prefix Lists referred by this IPPermission instance and the
   * corresponding IpSpaces
   */
  private Map<PrefixList, IpSpace> collectPrefixLists(Region region) {
    ImmutableMap.Builder<PrefixList, IpSpace> plToIpSpace = ImmutableMap.builder();
    _prefixList.stream()
        .map(plId -> region.getPrefixLists().get(plId))
        .filter(Objects::nonNull)
        .distinct()
        .forEach(
            prefixList -> {
              prefixList
                  .getCidrs()
                  .forEach(prefix -> plToIpSpace.put(prefixList, prefix.toIpSpace()));
            });
    return plToIpSpace.build();
  }

  /**
   * Generates a list of AclLineMatchExprs (MatchHeaderSpaces) to match the IpProtocol and dst ports
   * in this IpPermission instance (or ICMP type and code, if protocol is ICMP). Returns null if IP
   * Protocol and ports are not consistent
   */
  @Nullable
  private List<AclLineMatchExpr> getMatchExprsForProtocolAndPorts(
      String aclLineName, Warnings warnings) {
    ImmutableList.Builder<AclLineMatchExpr> matchesBuilder = ImmutableList.builder();
    IpProtocol ipProtocol = Utils.toIpProtocol(_ipProtocol);
    Optional.ofNullable(ipProtocol)
        .map(
            protocol ->
                new MatchHeaderSpace(
                    HeaderSpace.builder().setIpProtocols(protocol).build(),
                    traceElementForProtocol(protocol)))
        .ifPresent(matchesBuilder::add);
    if (ipProtocol == IpProtocol.TCP || ipProtocol == IpProtocol.UDP) {
      Optional.ofNullable(exprForDstPorts()).ifPresent(matchesBuilder::add);
    } else if (ipProtocol == IpProtocol.ICMP) {
      int type = firstNonNull(_fromPort, -1);
      int code = firstNonNull(_toPort, -1);
      if (type == -1 && code != -1) {
        // Code should not be configured if type isn't.
        warnings.redFlag(
            String.format(
                "IpPermissions for term %s: unexpected for ICMP to have FromPort=%s and ToPort=%s",
                aclLineName, _fromPort, _toPort));
        return null;
      }
      Optional.ofNullable(exprForIcmpTypeAndCode(type, code)).ifPresent(matchesBuilder::add);
    } else if (_fromPort != null || _toPort != null) {
      // if protocols not from the above then fromPort and toPort should be null
      warnings.redFlag(
          String.format(
              "IpPermissions for term %s: unexpected to have IpProtocol=%s, FromPort=%s, and ToPort=%s",
              aclLineName, _ipProtocol, _fromPort, _toPort));
      return null;
    }
    return matchesBuilder.build();
  }

  /** Returns a MatchHeaderSpace to match the provided IpSpace either in ingress or egress mode */
  private static MatchHeaderSpace exprForSrcOrDstIps(
      IpSpace ipSpace, String vsAddressStructure, boolean ingress, AddressType addressType) {
    if (ingress) {
      return new MatchHeaderSpace(
          HeaderSpace.builder().setSrcIps(ipSpace).build(),
          traceElementForAddress("source", vsAddressStructure, addressType));
    }
    return new MatchHeaderSpace(
        HeaderSpace.builder().setDstIps(ipSpace).build(),
        traceElementForAddress("destination", vsAddressStructure, addressType));
  }

  /** Returns a MatchHeaderSpace to match the destination ports in this IpPermission instance */
  @Nullable
  private MatchHeaderSpace exprForDstPorts() {
    // if the range isn't all ports, set it in ACL
    int low = (_fromPort == null || _fromPort == -1) ? 0 : _fromPort;
    int hi = (_toPort == null || _toPort == -1) ? 65535 : _toPort;
    if (low != 0 || hi != 65535) {
      return new MatchHeaderSpace(
          HeaderSpace.builder().setDstPorts(new SubRange(low, hi)).build(),
          traceElementForDstPorts(low, hi));
    }
    return null;
  }

  /**
   * Returns a MatchHeaderSpace to match the ICMP type and code. This method should be called only
   * after the protocol is determined to be ICMP
   */
  @Nullable
  private static MatchHeaderSpace exprForIcmpTypeAndCode(int type, int code) {
    HeaderSpace.Builder hsBuilder = HeaderSpace.builder();
    if (type != -1) {
      hsBuilder.setIcmpTypes(type);
      if (code != -1) {
        hsBuilder.setIcmpCodes(code);
      }
      return new MatchHeaderSpace(hsBuilder.build(), traceElementForIcmp(type, code));
    }
    // type == -1 and code == -1
    return null;
  }

  /**
   * Converts this {@link IpPermissions} to a {@link List} of {@link ExprAclLine}s. Each element
   * present in {@link #_ipRanges}, {@link #_securityGroups} or {@link #_prefixList} will generate
   * one {@link ExprAclLine}
   *
   * <p>Returns empty {@link List} if the security group cannot be processed, e.g., uses an
   * unsupported definition of the affected IP addresses.
   */
  List<ExprAclLine> toIpAccessListLines(
      boolean ingress, Region region, String name, Warnings warnings) {
    if (_ipProtocol.equals("icmpv6")) {
      // Not valid in IPv4 packets.
      return ImmutableList.of();
    }
    List<AclLineMatchExpr> protocolAndPortExprs = getMatchExprsForProtocolAndPorts(name, warnings);
    if (protocolAndPortExprs == null) {
      return ImmutableList.of();
    }
    ImmutableList.Builder<ExprAclLine> aclLines = ImmutableList.builder();
    _ipRanges.stream()
        .map(
            ipRange ->
                ExprAclLine.accepting()
                    .setMatchCondition(
                        and(
                            ImmutableList.<AclLineMatchExpr>builder()
                                .addAll(protocolAndPortExprs)
                                .add(
                                    exprForSrcOrDstIps(
                                        ipRange.getPrefix().toIpSpace(),
                                        ipRange.getPrefix().toString(),
                                        ingress,
                                        AddressType.CIDR_IP))
                                .build()))
                    .setTraceElement(getTraceElementForRule(ipRange.getDescription()))
                    .setName(name)
                    .build())
        .forEach(aclLines::add);

    aclLines.addAll(
        collectSecurityGroupIntoAclLines(
            collectSecurityGroups(region), protocolAndPortExprs, ingress, name));
    aclLines.addAll(
        collectPrefixListsIntoAclLines(
            collectPrefixLists(region), protocolAndPortExprs, ingress, name));
    return aclLines.build();
  }

  private static List<ExprAclLine> collectSecurityGroupIntoAclLines(
      Map<SecurityGroup, IpSpace> sgs,
      List<AclLineMatchExpr> protocolAndPortExprs,
      boolean ingress,
      String aclLineName) {
    return sgs.entrySet().stream()
        .map(
            entry ->
                ExprAclLine.accepting()
                    .setMatchCondition(
                        and(
                            ImmutableList.<AclLineMatchExpr>builder()
                                .addAll(protocolAndPortExprs)
                                .add(
                                    exprForSrcOrDstIps(
                                        entry.getValue(),
                                        entry.getKey().getGroupName(),
                                        ingress,
                                        AddressType.SECURITY_GROUP))
                                .build()))
                    .setTraceElement(getTraceElementForRule(null))
                    .setName(aclLineName)
                    .build())
        .collect(ImmutableList.toImmutableList());
  }

  private static List<ExprAclLine> collectPrefixListsIntoAclLines(
      Map<PrefixList, IpSpace> prefixLists,
      List<AclLineMatchExpr> protocolAndPortExprs,
      boolean ingress,
      String aclLineName) {
    return prefixLists.entrySet().stream()
        .map(
            entry ->
                ExprAclLine.accepting()
                    .setMatchCondition(
                        and(
                            ImmutableList.<AclLineMatchExpr>builder()
                                .addAll(protocolAndPortExprs)
                                .add(
                                    exprForSrcOrDstIps(
                                        entry.getValue(),
                                        entry.getKey().getId(),
                                        ingress,
                                        AddressType.PREFIX_LIST))
                                .build()))
                    .setTraceElement(getTraceElementForRule(null))
                    .setName(aclLineName)
                    .build())
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpPermissions)) {
      return false;
    }
    IpPermissions that = (IpPermissions) o;
    return _fromPort == that._fromPort
        && _toPort == that._toPort
        && Objects.equals(_ipProtocol, that._ipProtocol)
        && Objects.equals(_ipRanges, that._ipRanges)
        && Objects.equals(_ipRanges, that._ipRanges)
        && Objects.equals(_prefixList, that._prefixList)
        && Objects.equals(_securityGroups, that._securityGroups);
  }

  @Override
  public int hashCode() {
    return com.google.common.base.Objects.hashCode(
        _fromPort, _ipProtocol, _ipRanges, _prefixList, _securityGroups, _toPort);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_fromPort", _fromPort)
        .add("_ipProtocol", _ipProtocol)
        .add("_ipRanges", _ipRanges)
        .add("_prefixList", _prefixList)
        .add("_securityGroups", _securityGroups)
        .add("_toPort", _toPort)
        .toString();
  }
}
