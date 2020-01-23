package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
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
import static org.batfish.representation.aws.Utils.getTraceTextForRule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Function;
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

  private SortedMap<IpSpace, IpRange> collectIpRanges() {
    return _ipRanges.stream()
        .collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Ordering.natural(),
                ipRange -> ipRange.getPrefix().toIpSpace(),
                Function.identity()));
  }

  private SortedMap<IpSpace, String> collectSecurityGroups(Region region) {
    ImmutableSortedMap.Builder<IpSpace, String> ipSpaceToSg = ImmutableSortedMap.naturalOrder();
    _securityGroups.stream()
        .map(sgID -> region.getSecurityGroups().get(sgID))
        .filter(Objects::nonNull)
        .forEach(
            securityGroup -> {
              securityGroup
                  .getUsersIpSpace()
                  .forEach(
                      prefix -> ipSpaceToSg.put(prefix.toIpSpace(), securityGroup.getGroupName()));
            });
    return ipSpaceToSg.build();
  }

  private SortedMap<IpSpace, String> collectPrefixLists(Region region) {
    ImmutableSortedMap.Builder<IpSpace, String> ipSpaceToPrefixLists =
        ImmutableSortedMap.naturalOrder();
    _prefixList.stream()
        .map(plId -> region.getPrefixLists().get(plId))
        .filter(Objects::nonNull)
        .forEach(
            prefixList -> {
              prefixList
                  .getCidrs()
                  .forEach(
                      prefix -> ipSpaceToPrefixLists.put(prefix.toIpSpace(), prefixList.getId()));
            });
    return ipSpaceToPrefixLists.build();
  }

  private AclLineMatchExpr ipRangeToMatchExpr(
      boolean ingress, IpRange ipRange, IpSpace ipSpace, String aclLineName, Warnings warnings) {
    ImmutableList.Builder<AclLineMatchExpr> matchesBuilder = ImmutableList.builder();
    matchesBuilder.add(exprForSrcOrDstIps(ipSpace, ipRange.getPrefix().toString(), ingress));
    matchesBuilder.addAll(getMatchExprsForProtocolAndPorts(aclLineName, warnings));
    return and(
        getTraceTextForRule(ipRange.getDescription()),
        matchesBuilder.build().toArray(new AclLineMatchExpr[0]));
  }

  private AclLineMatchExpr securityGroupToMatchExpr(
      boolean ingress, String sgName, IpSpace ipSpace, String aclLineName, Warnings warnings) {
    ImmutableList.Builder<AclLineMatchExpr> matchesBuilder = ImmutableList.builder();
    matchesBuilder.add(exprForSrcOrDstIps(ipSpace, sgName, ingress));
    matchesBuilder.addAll(getMatchExprsForProtocolAndPorts(aclLineName, warnings));
    return and(getTraceTextForRule(null), matchesBuilder.build().toArray(new AclLineMatchExpr[0]));
  }

  private AclLineMatchExpr prefixlistToMatchExpr(
      boolean ingress,
      String prefixListId,
      IpSpace ipSpace,
      String aclLineName,
      Warnings warnings) {
    ImmutableList.Builder<AclLineMatchExpr> matchesBuilder = ImmutableList.builder();
    matchesBuilder.add(exprForSrcOrDstIps(ipSpace, prefixListId, ingress));
    matchesBuilder.addAll(getMatchExprsForProtocolAndPorts(aclLineName, warnings));
    return and(getTraceTextForRule(null), matchesBuilder.build().toArray(new AclLineMatchExpr[0]));
  }

  private List<AclLineMatchExpr> getMatchExprsForProtocolAndPorts(
      String aclLineName, Warnings warnings) {
    ImmutableList.Builder<AclLineMatchExpr> matchesBuilder = ImmutableList.builder();
    Optional.ofNullable(Utils.toIpProtocol(_ipProtocol))
        .ifPresent(
            ipProtocol -> {
              matchesBuilder.add(
                  new MatchHeaderSpace(
                      HeaderSpace.builder().setIpProtocols(ipProtocol).build(),
                      traceElementForProtocol(ipProtocol)));
              if (ipProtocol == IpProtocol.TCP || ipProtocol == IpProtocol.UDP) {
                Optional.ofNullable(exprForDstPorts()).ifPresent(matchesBuilder::add);
              } else if (ipProtocol == IpProtocol.ICMP) {
                Optional.ofNullable(exprForIcmpTypeAndCode(aclLineName, warnings))
                    .ifPresent(matchesBuilder::add);
              } else if (_fromPort != null || _toPort != null) {
                // if protocols not from the above then fromPort and toPort should be null
                warnings.redFlag(
                    String.format(
                        "IpPermissions for term %s: unexpected to have IpProtocol=%s, FromPort=%s, and ToPort=%s",
                        aclLineName, _ipProtocol, _fromPort, _toPort));
              }
            });
    return matchesBuilder.build();
  }

  private MatchHeaderSpace exprForSrcOrDstIps(
      IpSpace ipSpace, String vsAddressStructure, boolean ingress) {
    if (ingress) {
      return new MatchHeaderSpace(
          HeaderSpace.builder().setSrcIps(ipSpace).build(),
          traceElementForAddress("source", vsAddressStructure));
    }
    return new MatchHeaderSpace(
        HeaderSpace.builder().setDstIps(ipSpace).build(),
        traceElementForAddress("destination", vsAddressStructure));
  }

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

  @Nullable
  private MatchHeaderSpace exprForIcmpTypeAndCode(String aclLineName, Warnings warnings) {
    int type = firstNonNull(_fromPort, -1);
    int code = firstNonNull(_toPort, -1);
    if (type != -1 && code != -1) {
      return new MatchHeaderSpace(
          HeaderSpace.builder().setIcmpTypes(type).setIcmpCodes(code).build(),
          traceElementForIcmp(type, code));
    }
    if (type == -1 && code != -1) {
      // Code should not be configured if type isn't.
      warnings.redFlag(
          String.format(
              "IpPermissions for term %s: unexpected for ICMP to have FromPort=%s and ToPort=%s",
              aclLineName, _fromPort, _toPort));
    }
    return null;
  }

  private static String traceElementForAddress(String direction, String vsAddressStructure) {
    return String.format("Matched %s address %s", direction, vsAddressStructure);
  }

  private static String traceElementForProtocol(IpProtocol protocol) {
    return String.format("Matched protocol %s", protocol);
  }

  private static String traceElementForDstPorts(int low, int high) {
    if (low == high) {
      return String.format("Matched destination port %s", low);
    }
    return String.format("Matched destination ports [%s-%s]", low, high);
  }

  private static String traceElementForIcmp(int type, int code) {
    return String.format("Matched ICMP type %s and ICMP code %s", type, code);
  }

  /**
   * Converts this {@link IpPermissions} to an {@link ExprAclLine}.
   *
   * <p>Returns {@link Optional#empty()} if the security group cannot be processed, e.g., uses an
   * unsupported definition of the affected IP addresses.
   */
  Optional<ExprAclLine> toIpAccessListLine(
      boolean ingress, Region region, String name, Warnings warnings, int ruleNum) {
    if (_ipProtocol.equals("icmpv6")) {
      // Not valid in IPv4 packets.
      return Optional.empty();
    }
    ImmutableList.Builder<AclLineMatchExpr> exprsForThisIpPerms = ImmutableList.builder();
    collectIpRanges().entrySet().stream()
        .map(entry -> ipRangeToMatchExpr(ingress, entry.getValue(), entry.getKey(), name, warnings))
        .forEach(exprsForThisIpPerms::add);
    collectSecurityGroups(region).entrySet().stream()
        .map(
            entry ->
                securityGroupToMatchExpr(ingress, entry.getValue(), entry.getKey(), name, warnings))
        .forEach(exprsForThisIpPerms::add);
    collectPrefixLists(region).entrySet().stream()
        .map(
            entry ->
                prefixlistToMatchExpr(ingress, entry.getValue(), entry.getKey(), name, warnings))
        .forEach(exprsForThisIpPerms::add);

    return Optional.of(
        ExprAclLine.accepting()
            .setMatchCondition(or(exprsForThisIpPerms.build()))
            .setName(name)
            .build());
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
