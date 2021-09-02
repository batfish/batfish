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
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;

/** Utility class for Checkpoint conversion methods */
public final class CheckPointGatewayConversions {
  /**
   * Returns a {@code Map} of names to {@link IpAccessList}s corresponding to specified {@link
   * AccessLayer}.
   *
   * <p>{@link AccessSection}s will have their own {@link IpAccessList}s in the returned map, but
   * {@link AccessRule}s will be embedded directly in their parent's {@link IpAccessList}.
   */
  static Map<String, IpAccessList> toIpAccessLists(@Nonnull AccessLayer access) {
    Map<Uid, TypedManagementObject> objs = access.getObjectsDictionary();
    ImmutableMap.Builder<String, IpAccessList> acls = ImmutableMap.builder();
    ImmutableList.Builder<AclLine> accessLayerLines = ImmutableList.builder();
    for (AccessRuleOrSection acl : access.getRulebase()) {
      if (acl instanceof AccessRule) {
        accessLayerLines.add(toAclLine((AccessRule) acl, objs));
        continue;
      }
      assert acl instanceof AccessSection;
      IpAccessList accessSection = toIpAccessList((AccessSection) acl, objs);
      acls.put(accessSection.getName(), accessSection);
      accessLayerLines.add(new AclAclLine(accessSection.getName(), accessSection.getName()));
    }

    // AccessLayer
    acls.put(
        access.getName(),
        IpAccessList.builder()
            .setName(access.getName())
            .setLines(accessLayerLines.build())
            .build());
    return acls.build();
  }

  /** Convert the specified {@link AccessSection} into an {@link IpAccessList}. */
  private static IpAccessList toIpAccessList(
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
    return ExprAclLine.builder()
        .setName(rule.getName())
        .setMatchCondition(toMatchExpr(rule, objs))
        .setAction(toAction(objs.get(rule.getAction())))
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
  static LineAction toAction(@Nullable TypedManagementObject obj) {
    if (obj == null) {
      // TODO warn
      return LineAction.DENY;
    } else if (!(obj instanceof RulebaseAction)) {
      // TODO warn
      return LineAction.DENY;
    } else {
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
                .map(i -> objs.get(i).getName())
                .map(IpSpaceReference::new)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  private CheckPointGatewayConversions() {}
}
