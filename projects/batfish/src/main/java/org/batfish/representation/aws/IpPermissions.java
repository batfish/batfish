package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
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
import static org.batfish.representation.aws.Utils.traceElementEniPrivateIp;
import static org.batfish.representation.aws.Utils.traceElementForAddress;
import static org.batfish.representation.aws.Utils.traceElementForDstPorts;
import static org.batfish.representation.aws.Utils.traceElementForIcmpCode;
import static org.batfish.representation.aws.Utils.traceElementForIcmpType;
import static org.batfish.representation.aws.Utils.traceElementForProtocol;
import static org.batfish.representation.aws.Utils.traceTextForAddress;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/** IP packet permissions within AWS security groups */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class IpPermissions implements Serializable {

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
  public static final class IpRange implements Serializable {

    private final @Nullable String _description;
    private final @Nonnull Prefix _prefix;

    @JsonCreator
    private static IpRange create(
        @JsonProperty(JSON_KEY_DESCRIPTION) @Nullable String description,
        @JsonProperty(JSON_KEY_CIDR_IP) @Nullable Prefix prefix) {
      checkArgument(prefix != null, "Prefix cannot be null in IpRange");
      return new IpRange(description, prefix);
    }

    public IpRange(@Nullable String description, Prefix prefix) {
      _description = description;
      _prefix = prefix;
    }

    public IpRange(Prefix prefix) {
      _description = null;
      _prefix = prefix;
    }

    public @Nonnull Prefix getPrefix() {
      return _prefix;
    }

    public @Nullable String getDescription() {
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

    private final @Nonnull String _id;

    @JsonCreator
    private static PrefixListId create(@JsonProperty(JSON_KEY_PREFIX_LIST_ID) @Nullable String id) {
      checkNonNull(id, JSON_KEY_PREFIX_LIST_ID, "PrefixListIds");
      return new PrefixListId(id);
    }

    PrefixListId(String id) {
      _id = id;
    }

    public @Nonnull String getId() {
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

  @VisibleForTesting
  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  public static final class UserIdGroupPair implements Serializable {

    private final @Nullable String _description;

    private final @Nonnull String _groupId;

    @JsonCreator
    private static UserIdGroupPair create(
        @JsonProperty(JSON_KEY_DESCRIPTION) @Nullable String desription,
        @JsonProperty(JSON_KEY_GROUP_ID) @Nullable String groupId) {
      checkArgument(groupId != null, "Group id cannot be null in user id group pair");
      return new UserIdGroupPair(groupId, desription);
    }

    public UserIdGroupPair(String groupId, @Nullable String description) {
      _groupId = groupId;
      _description = description;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public @Nonnull String getGroupId() {
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
      return Objects.equals(_groupId, that._groupId)
          && Objects.equals(_description, that._description);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_groupId, _description);
    }
  }

  private final @Nullable Integer _fromPort;

  private final @Nonnull String _ipProtocol;

  private final @Nonnull List<IpRange> _ipRanges;

  private final @Nonnull List<String> _prefixList;

  private final @Nonnull List<UserIdGroupPair> _userIdGroupPairs;

  private final @Nullable Integer _toPort;

  @JsonCreator
  private static IpPermissions create(
      @JsonProperty(JSON_KEY_IP_PROTOCOL) @Nullable String ipProtocol,
      @JsonProperty(JSON_KEY_FROM_PORT) @Nullable Integer fromPort,
      @JsonProperty(JSON_KEY_TO_PORT) @Nullable Integer toPort,
      @JsonProperty(JSON_KEY_IP_RANGES) @Nullable List<IpRange> ipRanges,
      @JsonProperty(JSON_KEY_PREFIX_LIST_IDS) @Nullable List<PrefixListId> prefixes,
      @JsonProperty(JSON_KEY_USER_GROUP_ID_PAIRS) @Nullable
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
        userIdGroupPairs);
  }

  public IpPermissions(
      String ipProtocol,
      @Nullable Integer fromPort,
      @Nullable Integer toPort,
      List<IpRange> ipRanges,
      List<String> prefixList,
      List<UserIdGroupPair> userIdGroupPairs) {
    _ipProtocol = ipProtocol;
    _fromPort = fromPort;
    _toPort = toPort;
    _ipRanges = ImmutableList.copyOf(ipRanges);
    _prefixList = ImmutableList.copyOf(prefixList);
    _userIdGroupPairs = ImmutableList.copyOf(userIdGroupPairs);
  }

  /**
   * Returns a Map containing all the Prefix Lists referred by this IPPermission instance and the
   * corresponding IpSpaces
   */
  @VisibleForTesting
  static Map<PrefixList, IpSpace> collectPrefixLists(Region region, List<String> prefixLists) {
    return prefixLists.stream()
        .distinct()
        .map(plId -> region.getPrefixLists().get(plId))
        .filter(Objects::nonNull)
        .collect(
            ImmutableMap.toImmutableMap(Function.identity(), IpPermissions::prefixListToIpSpace));
  }

  private static IpSpace prefixListToIpSpace(PrefixList pl) {
    return IpWildcardSetIpSpace.builder()
        .including(
            pl.getCidrs().stream().map(IpWildcard::create).collect(ImmutableSet.toImmutableSet()))
        .build();
  }

  /**
   * Generates a list of AclLineMatchExprs (MatchHeaderSpaces) to match the IpProtocol and dst ports
   * in this IpPermission instance (or ICMP type and code, if protocol is ICMP). Returns null if IP
   * Protocol and ports are not consistent
   */
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  private @Nullable List<AclLineMatchExpr> getMatchExprsForProtocolAndPorts(
      String aclLineName, Warnings warnings) {
    ImmutableList.Builder<AclLineMatchExpr> matchesBuilder = ImmutableList.builder();
    IpProtocol ipProtocol = Utils.toIpProtocol(_ipProtocol);
    Optional.ofNullable(ipProtocol)
        .map(protocol -> matchIpProtocol(protocol, traceElementForProtocol(protocol)))
        .ifPresent(matchesBuilder::add);
    if (ipProtocol == IpProtocol.TCP || ipProtocol == IpProtocol.UDP) {
      Optional.ofNullable(exprForDstPorts()).ifPresent(matchesBuilder::add);
    } else if (ipProtocol == IpProtocol.ICMP) {
      int type = firstNonNull(_fromPort, -1);
      int code = firstNonNull(_toPort, -1);
      if (type == -1 && code != -1) {
        // Code should not be configured if type isn't.
        warnings.redFlagf(
            "IpPermissions for term %s: unexpected for ICMP to have FromPort=%s and ToPort=%s",
            aclLineName, _fromPort, _toPort);
        return null;
      }
      exprForIcmpTypeAndCode(type, code).forEach(matchesBuilder::add);
    } else if (_fromPort != null || _toPort != null) {
      // if protocols not from the above then fromPort and toPort should be null
      warnings.redFlagf(
          "IpPermissions for term %s: unexpected to have IpProtocol=%s, FromPort=%s, and"
              + " ToPort=%s",
          aclLineName, _ipProtocol, _fromPort, _toPort);
      return null;
    }
    return matchesBuilder.build();
  }

  /** Returns a MatchHeaderSpace to match the provided IpSpace either in ingress or egress mode */
  private static AclLineMatchExpr exprForSrcOrDstIps(
      IpSpace ipSpace, String vsAddressStructure, boolean ingress, AddressType addressType) {
    if (ingress) {
      return matchSrc(ipSpace, traceElementForAddress("source", vsAddressStructure, addressType));
    }
    return matchDst(
        ipSpace, traceElementForAddress("destination", vsAddressStructure, addressType));
  }

  /** Returns a AclLineMatchExpr to match the destination ports in this IpPermission instance */
  private @Nullable AclLineMatchExpr exprForDstPorts() {
    // if the range isn't all ports, set it in ACL
    int low = (_fromPort == null || _fromPort == -1) ? 0 : _fromPort;
    int hi = (_toPort == null || _toPort == -1) ? 65535 : _toPort;
    if (low != 0 || hi != 65535) {
      return matchDstPort(IntegerSpace.of(new SubRange(low, hi)), traceElementForDstPorts(low, hi));
    }
    return null;
  }

  /**
   * Returns a MatchHeaderSpace to match the ICMP type and code. This method should be called only
   * after the protocol is determined to be ICMP
   */
  private static @Nonnull Stream<AclLineMatchExpr> exprForIcmpTypeAndCode(int type, int code) {
    if (type == -1) {
      return Stream.of();
    }

    MatchHeaderSpace matchType =
        new MatchHeaderSpace(
            HeaderSpace.builder().setIcmpTypes(type).build(), traceElementForIcmpType(type));
    if (code == -1) {
      return Stream.of(matchType);
    }

    MatchHeaderSpace matchCode =
        new MatchHeaderSpace(
            HeaderSpace.builder().setIcmpCodes(code).build(), traceElementForIcmpCode(code));
    return Stream.of(matchType, matchCode);
  }

  /**
   * Converts this {@link IpPermissions} to an {@link ExprAclLine}.
   *
   * <p>Returns unmatchable line if the security group cannot be processed, e.g., uses an
   * unsupported definition of the affected IP addresses.
   */
  @Nonnull
  ExprAclLine toIpAccessListLine(boolean ingress, Region region, String name, Warnings warnings) {
    if (_ipProtocol.equals("icmpv6")) {
      // Not valid in IPv4 packets.
      return ExprAclLine.accepting().setMatchCondition(FALSE).setName(name).build();
    }
    List<AclLineMatchExpr> protocolAndPortExprs = getMatchExprsForProtocolAndPorts(name, warnings);
    if (protocolAndPortExprs == null) {
      return ExprAclLine.accepting().setMatchCondition(FALSE).setName(name).build();
    }
    ImmutableList.Builder<AclLineMatchExpr> aclLineExprs = ImmutableList.builder();
    _ipRanges.stream()
        .map(
            ipRange ->
                and(
                    ImmutableList.<AclLineMatchExpr>builder()
                        .addAll(protocolAndPortExprs)
                        .add(
                            exprForSrcOrDstIps(
                                ipRange.getPrefix().toIpSpace(),
                                ipRange.getPrefix().toString(),
                                ingress,
                                AddressType.CIDR_IP))
                        .build(),
                    getTraceElementForRule(ipRange.getDescription())))
        .forEach(aclLineExprs::add);

    aclLineExprs.addAll(userIdGroupsToAclLineExprs(region, protocolAndPortExprs, ingress));
    aclLineExprs.addAll(
        collectPrefixListsIntoAclLineExprs(
            collectPrefixLists(region, _prefixList), protocolAndPortExprs, ingress));
    return ExprAclLine.accepting()
        .setMatchCondition(or(aclLineExprs.build()))
        // TODO Should we set this trace element? If so, to what?
        .setTraceElement(null)
        .setName(name)
        .build();
  }

  private List<AclLineMatchExpr> userIdGroupsToAclLineExprs(
      Region region, List<AclLineMatchExpr> protocolAndPortExprs, boolean ingress) {
    return _userIdGroupPairs.stream()
        .map(
            uIdGr ->
                Optional.ofNullable(region.getSecurityGroups().get(uIdGr.getGroupId()))
                    .map(
                        sg ->
                            createAclLineExprForSg(
                                protocolAndPortExprs,
                                toMatchExpr(sg, ingress),
                                uIdGr.getDescription())))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(ImmutableList.toImmutableList());
  }

  private static AclLineMatchExpr toMatchExpr(SecurityGroup sg, boolean ingress) {
    ImmutableList.Builder<AclLineMatchExpr> matchExprBuilder = ImmutableList.builder();
    for (Entry<Ip, String> ipAndInstance : sg.getReferrerIps().entrySet()) {
      TraceElement traceElement = traceElementEniPrivateIp(ipAndInstance.getValue());
      IpIpSpace ipSpace = ipAndInstance.getKey().toIpSpace();
      if (ingress) {
        matchExprBuilder.add(matchSrc(ipSpace, traceElement));
      } else {
        matchExprBuilder.add(matchDst(ipSpace, traceElement));
      }
    }
    // See note about naming on SecurityGroup#getGroupName.
    return or(
        matchExprBuilder.build(),
        TraceElement.of(
            traceTextForAddress(
                ingress ? "source" : "destination",
                sg.getGroupName(),
                AddressType.SECURITY_GROUP)));
  }

  private AclLineMatchExpr createAclLineExprForSg(
      List<AclLineMatchExpr> protocolAndPortExprs,
      AclLineMatchExpr matchAddressForSg,
      @Nullable String ruleDescription) {
    return and(
        ImmutableList.<AclLineMatchExpr>builder()
            .addAll(protocolAndPortExprs)
            .add(matchAddressForSg)
            .build(),
        getTraceElementForRule(ruleDescription));
  }

  private static List<AclLineMatchExpr> collectPrefixListsIntoAclLineExprs(
      Map<PrefixList, IpSpace> prefixLists,
      List<AclLineMatchExpr> protocolAndPortExprs,
      boolean ingress) {
    return prefixLists.entrySet().stream()
        .map(
            entry ->
                and(
                    ImmutableList.<AclLineMatchExpr>builder()
                        .addAll(protocolAndPortExprs)
                        .add(
                            exprForSrcOrDstIps(
                                entry.getValue(),
                                entry.getKey().getId(),
                                ingress,
                                AddressType.PREFIX_LIST))
                        .build(),
                    getTraceElementForRule(null)))
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
    return Objects.equals(_fromPort, that._fromPort)
        && Objects.equals(_toPort, that._toPort)
        && Objects.equals(_ipProtocol, that._ipProtocol)
        && Objects.equals(_ipRanges, that._ipRanges)
        && Objects.equals(_prefixList, that._prefixList)
        && Objects.equals(_userIdGroupPairs, that._userIdGroupPairs);
  }

  @Override
  public int hashCode() {
    return com.google.common.base.Objects.hashCode(
        _fromPort, _ipProtocol, _ipRanges, _prefixList, _userIdGroupPairs, _toPort);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_fromPort", _fromPort)
        .add("_ipProtocol", _ipProtocol)
        .add("_ipRanges", _ipRanges)
        .add("_prefixList", _prefixList)
        .add("_userIdGroupPairs", _userIdGroupPairs)
        .add("_toPort", _toPort)
        .toString();
  }

  public @Nullable Integer getFromPort() {
    return _fromPort;
  }

  public @Nonnull String getIpProtocol() {
    return _ipProtocol;
  }

  public @Nonnull List<IpRange> getIpRanges() {
    return _ipRanges;
  }

  public @Nonnull List<String> getPrefixList() {
    return _prefixList;
  }

  public @Nonnull List<UserIdGroupPair> getUserIdGroupPairs() {
    return _userIdGroupPairs;
  }

  public @Nullable Integer getToPort() {
    return _toPort;
  }
}
