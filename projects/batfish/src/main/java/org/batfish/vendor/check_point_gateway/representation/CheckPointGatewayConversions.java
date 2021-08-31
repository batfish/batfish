package org.batfish.vendor.check_point_gateway.representation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclAclLine;
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

  /**
   * Returns a {@code Map} of all {@link IpAccessList}s corresponding to specified {@link
   * AccessLayer}.
   *
   * <p>{@link AccessSection}s will have their own {@link IpAccessList}s created, but {@link
   * AccessRule}s will be embedded directly in their parent's {@link IpAccessList}.
   */
  static Map<String, IpAccessList> toIpAccessLists(@Nonnull AccessLayer access) {
    Map<Uid, TypedManagementObject> objs = access.getObjectsDictionary();
    ImmutableMap.Builder<String, IpAccessList> acls = ImmutableMap.builder();
    ImmutableList.Builder<AclLine> accessLayerLines = ImmutableList.builder();
    for (AccessRuleOrSection acl : access.getRulebase()) {
      // Directly embed AccessRules into the AccessLayer's lines
      if (acl instanceof AccessRule) {
        accessLayerLines.add(toAclLine((AccessRule) acl, objs));
        continue;
      }
      // Generate IpAccessList for AccessSections and reference new IpAccessList from AccessLayer
      assert acl instanceof AccessSection;
      IpAccessList convertedSection = toIpAccessList((AccessSection) acl, objs);
      acls.put(convertedSection.getName(), convertedSection);
      accessLayerLines.add(new AclAclLine(convertedSection.getName(), convertedSection.getName()));
    }

    acls.put(
        access.getName(),
        IpAccessList.builder()
            .setName(access.getName())
            .setLines(accessLayerLines.build())
            .build());
    return acls.build();
  }

  static IpAccessList toIpAccessList(
      @Nonnull AccessSection section, Map<Uid, TypedManagementObject> objs) {
    return IpAccessList.builder()
        .setName(section.getName())
        .setLines(
            section.getRulebase().stream()
                .map(r -> toAclLine(r, objs))
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  /** Convert specified {@link AccessRule} to an {@link AclLine}. */
  @Nonnull
  static AclLine toAclLine(AccessRule rule, Map<Uid, TypedManagementObject> objs) {
    TypedManagementObject actionObj = objs.get(rule.getAction());
    LineAction action = LineAction.DENY;
    if (actionObj == null) {
      // TODO warn
    } else if (!(actionObj instanceof RulebaseAction)) {
      // TODO warn
    } else {
      action = toAction((RulebaseAction) actionObj);
    }

    return ExprAclLine.builder()
        .setName(rule.getName())
        .setMatchCondition(toMatchExpr(rule, objs))
        .setAction(action)
        // TODO trace element and structure ID
        .build();
  }

  /**
   * Convert specified {@link AccessRule} into an {@link AclLineMatchExpr} representing the match
   * conditions of the rule.
   */
  @Nonnull
  static AclLineMatchExpr toMatchExpr(AccessRule rule, Map<Uid, TypedManagementObject> objs) {
    ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();

    // Source
    IpSpace srcRefs = toIpSpace(rule.getSource(), objs);
    AclLineMatchExpr srcMatch =
        rule.getSourceNegate()
            ? new MatchHeaderSpace(HeaderSpace.builder().setNotSrcIps(srcRefs).build())
            : new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(srcRefs).build());
    conjuncts.add(srcMatch);

    // Dest
    IpSpace dstRefs = toIpSpace(rule.getDestination(), objs);
    AclLineMatchExpr dstMatch =
        rule.getDestinationNegate()
            ? new MatchHeaderSpace(HeaderSpace.builder().setNotDstIps(dstRefs).build())
            : new MatchHeaderSpace(HeaderSpace.builder().setDstIps(dstRefs).build());
    conjuncts.add(dstMatch);

    // Service
    // TODO encode service match condition

    return new AndMatchExpr(conjuncts.build());
  }

  /** Convert specified {@link TypedManagementObject} to a {@link LineAction}. */
  @Nonnull
  private static LineAction toAction(@Nonnull RulebaseAction obj) {
    switch (obj.getAction()) {
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
   * IpSpace}s existing for each of the supplied object's {@link Uid}s.
   */
  @Nonnull
  private static IpSpace toIpSpace(List<Uid> targets, Map<Uid, TypedManagementObject> objs) {
    return AclIpSpace.builder()
        .thenPermitting(
            targets.stream()
                .map(i -> objs.get(i).getUid().getValue())
                .map(IpSpaceReference::new)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  private CheckPointGatewayConversions() {}
}
