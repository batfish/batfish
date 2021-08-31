package org.batfish.vendor.check_point_gateway.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;

/** Utility class for Checkpoint conversion methods */
public final class CheckPointGatewayConversions {
  /**
   * Converts the given {@link AddressRange} into an {@link IpSpace}, or returns {@code null} if the
   * address range does not represent an IPv4 space.
   */
  static @Nullable IpSpace toIpSpace(AddressRange addressRange) {
    // TODO Convert IPv6 address ranges
    if (addressRange.getIpv4AddressFirst() == null || addressRange.getIpv4AddressLast() == null) {
      return null;
    }
    return IpRange.range(addressRange.getIpv4AddressFirst(), addressRange.getIpv4AddressLast());
  }

  /** Converts the given {@link Network} into an {@link IpSpace}. */
  static @Nonnull IpSpace toIpSpace(Network network) {
    // TODO Network objects also have a "mask-length4" that we don't currently extract.
    //  If network objects always represent valid Prefixes, it may be simpler to extract
    //  that instead of subnet-mask and convert the network to a PrefixIpSpace.
    // In Network, the mask has bits that matter set, but IpWildcard interprets set mask bits as
    // "don't care". Flip mask to convert to IpWildcard.
    long flippedMask = network.getSubnetMask().asLong() ^ Ip.MAX.asLong();
    return IpWildcard.ipWithWildcardMask(network.getSubnet4(), flippedMask).toIpSpace();
  }

  /** Convert specified {@link AccessRuleOrSection} to an {@link IpAccessList}. */
  static IpAccessList toIpAccessList(@Nonnull AccessLayer access) {
    return IpAccessList.builder()
        .setLines(
            access.getRulebase().stream()
                .flatMap(a -> toAclLines(a, access.getObjectsDictionary()).stream())
                .collect(ImmutableList.toImmutableList()))
        .setName(access.getName())
        .build();
  }

  /**
   * Convert specified {@link AccessRuleOrSection} to a corresponding {@code List} of {@link
   * AclLine}s.
   */
  @Nonnull
  static List<AclLine> toAclLines(
      AccessRuleOrSection access, Map<Uid, TypedManagementObject> objs) {
    if (access instanceof AccessRule) {
      AccessRule rule = (AccessRule) access;
      return ImmutableList.of(toAclLine(rule, objs));
    }
    assert access instanceof AccessSection;
    AccessSection section = (AccessSection) access;
    return section.getRulebase().stream()
        .map(r -> toAclLine(r, objs))
        .collect(ImmutableList.toImmutableList());
  }

  @Nonnull
  static AclLine toAclLine(AccessRule rule, Map<Uid, TypedManagementObject> objs) {
    // TODO more population of fields
    return ExprAclLine.builder()
        .setName(rule.getName())
        .setMatchCondition(toMatchExpr(rule, objs))
        .setAction(toAction(objs.get(rule.getAction())))
        .setTraceElement(null)
        .setVendorStructureId(null)
        .build();
  }

  /** Convert specified {@link TypedManagementObject} to a {@link LineAction}. */
  @Nonnull
  static LineAction toAction(@Nullable TypedManagementObject obj) {
    if (obj == null) {
      // TODO warn
      return LineAction.DENY;
    }
    if (!(obj instanceof RulebaseAction)) {
      // TODO warn
      return LineAction.DENY;
    }

    RulebaseAction ra = (RulebaseAction) obj;
    switch (ra.getAction()) {
      case DROP:
        return LineAction.DENY;
      case ACCEPT:
        return LineAction.PERMIT;
      case UNHANDLED:
      default:
        // TODO warn
        return LineAction.DENY;
    }
  }

  /**
   * Returns an {@link IpSpace} containing the specified {@link Uid}s. Relies on named {@link
   * IpSpace}s existing for each of the supplied object {@link Uid}s.
   */
  @Nonnull
  static IpSpace toIpSpaceReferences(List<Uid> targets, Map<Uid, TypedManagementObject> objs) {
    return AclIpSpace.builder()
        .thenPermitting(
            targets.stream()
                .map(i -> objs.get(i).getUid().getValue())
                .map(IpSpaceReference::new)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  @Nonnull
  static AclLineMatchExpr toMatchExpr(AccessRule rule, Map<Uid, TypedManagementObject> objs) {
    ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();

    // TODO
    // Source
    IpSpace srcRefs = toIpSpaceReferences(rule.getSource(), objs);
    AclLineMatchExpr srcMatch =
        rule.getSourceNegate()
            ? new MatchHeaderSpace(HeaderSpace.builder().setNotSrcIps(srcRefs).build())
            : new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(srcRefs).build());
    conjuncts.add(srcMatch);

    // Dest
    IpSpace dstRefs = toIpSpaceReferences(rule.getDestination(), objs);
    AclLineMatchExpr dstMatch =
        rule.getDestinationNegate()
            ? new MatchHeaderSpace(HeaderSpace.builder().setNotDstIps(dstRefs).build())
            : new MatchHeaderSpace(HeaderSpace.builder().setDstIps(dstRefs).build());
    conjuncts.add(dstMatch);

    // Service
    // TODO encode service match condition
    //    conjuncts.add(
    //        new OrMatchExpr(
    //            rule.getService().stream()
    //                .map(s -> serviceToMatchExpr(s, rule.getServiceNegate(), objs))
    //                .collect(ImmutableList.toImmutableList())));

    return new AndMatchExpr(conjuncts.build());
  }

  private CheckPointGatewayConversions() {}
}
