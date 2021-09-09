package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.datamodel.IntegerSpace.PORTS;
import static org.batfish.datamodel.applications.PortsApplication.MAX_PORT_NUMBER;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.AddressSpace;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.Service;
import org.batfish.vendor.check_point_management.ServiceToMatchExpr;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;

/** Utility class for Checkpoint conversion methods */
public final class CheckPointGatewayConversions {

  /**
   * Convert src, dst, and service to an {@link AclLineMatchExpr} if they are of valid types. Else,
   * return {@link Optional#empty()}.
   */
  static @Nonnull Optional<AclLineMatchExpr> toMatchExpr(
      NamedManagementObject src,
      NamedManagementObject dst,
      NamedManagementObject service,
      ServiceToMatchExpr serviceToMatchExpr,
      Warnings warnings) {
    if (!checkValidHeaderSpaceInputs(src, dst, service, warnings)) {
      return Optional.empty();
    }
    ImmutableList.Builder<AclLineMatchExpr> exprs = ImmutableList.builder();
    exprs.add(new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(toIpSpace(src)).build()));
    exprs.add(new MatchHeaderSpace(HeaderSpace.builder().setDstIps(toIpSpace(dst)).build()));
    exprs.add(((Service) service).accept(serviceToMatchExpr));
    return Optional.of(AclLineMatchExprs.and(exprs.build()));
  }

  /**
   * Returns {@code true} iff the source, destination, and service for a NAT or access rule are
   * valid. In context of nat, refers to original fields. Warns for each invalid field.
   */
  @VisibleForTesting
  static boolean checkValidHeaderSpaceInputs(
      NamedManagementObject src,
      NamedManagementObject dst,
      NamedManagementObject service,
      Warnings warnings) {
    boolean valid = true;
    if (!(src instanceof AddressSpace)) {
      warnings.redFlag(
          String.format(
              "source %s has unsupported type %s and will be ignored",
              src.getName(), src.getClass()));
      valid = false;
    }
    if (!(dst instanceof AddressSpace)) {
      warnings.redFlag(
          String.format(
              "destination %s has unsupported type %s and will be ignored",
              dst.getName(), dst.getClass()));
      valid = false;
    }
    if (!(service instanceof Service)) {
      warnings.redFlag(
          String.format(
              "service %s has unsupported type %s and will be ignored",
              service.getName(), service.getClass()));
      valid = false;
    }
    return valid;
  }

  /**
   * Returns a {@code Map} of names to {@link IpAccessList}s corresponding to specified {@link
   * AccessLayer}.
   *
   * <p>{@link AccessSection}s will have their own {@link IpAccessList}s in the returned map, but
   * {@link AccessRule}s will be embedded directly in their parent's {@link IpAccessList}.
   */
  static Map<String, IpAccessList> toIpAccessLists(
      @Nonnull AccessLayer access,
      Map<Uid, NamedManagementObject> objects,
      ServiceToMatchExpr serviceToMatchExpr) {
    ImmutableMap.Builder<String, IpAccessList> acls = ImmutableMap.builder();
    ImmutableList.Builder<AclLine> accessLayerLines = ImmutableList.builder();
    for (AccessRuleOrSection acl : access.getRulebase()) {
      if (acl instanceof AccessRule) {
        accessLayerLines.add(toAclLine((AccessRule) acl, objects, serviceToMatchExpr));
        continue;
      }
      assert acl instanceof AccessSection;
      IpAccessList accessSection = toIpAccessList((AccessSection) acl, objects, serviceToMatchExpr);
      acls.put(accessSection.getName(), accessSection);
      accessLayerLines.add(new AclAclLine(accessSection.getName(), accessSection.getName()));
    }

    // AccessLayer
    IpAccessList accessLayer =
        IpAccessList.builder()
            .setName(aclName(access))
            .setSourceName(access.getName())
            .setLines(accessLayerLines.build())
            .build();
    acls.put(accessLayer.getName(), accessLayer);
    return acls.build();
  }

  /** Convert the specified {@link AccessSection} into an {@link IpAccessList}. */
  private static IpAccessList toIpAccessList(
      @Nonnull AccessSection section,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr) {
    return IpAccessList.builder()
        .setName(aclName(section))
        .setSourceName(section.getName())
        .setLines(
            section.getRulebase().stream()
                .map(r -> toAclLine(r, objs, serviceToMatchExpr))
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  /** Convert specified {@link AccessRule} to an {@link AclLine}. */
  @Nonnull
  static AclLine toAclLine(
      AccessRule rule,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr) {
    return ExprAclLine.builder()
        .setName(rule.getName())
        .setMatchCondition(toMatchExpr(rule, objs, serviceToMatchExpr))
        .setAction(toAction(objs.get(rule.getAction())))
        // TODO trace element and structure ID
        .build();
  }

  /**
   * Convert specified {@link AccessRule} into an {@link AclLineMatchExpr} representing the match
   * conditions of the rule.
   */
  @Nonnull
  static AclLineMatchExpr toMatchExpr(
      AccessRule rule,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr) {
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
    conjuncts.add(
        servicesToMatchExpr(rule.getService(), rule.getServiceNegate(), objs, serviceToMatchExpr));

    return new AndMatchExpr(conjuncts.build());
  }

  /**
   * Returns an {@link AclLineMatchExpr} matching the specified {@link Service} {@link Uid}s.
   * Ignores {@link Uid}s for undefined or non-{@link Service} objects.
   */
  @Nonnull
  private static AclLineMatchExpr servicesToMatchExpr(
      List<Uid> services,
      boolean negate,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr) {
    AclLineMatchExpr matchExpr =
        new OrMatchExpr(
            services.stream()
                .map(objs::get)
                .filter(Service.class::isInstance) // TODO warn about bad refs
                .map(Service.class::cast)
                .map(s -> s.accept(serviceToMatchExpr))
                .collect(ImmutableList.toImmutableList()));
    return negate ? new NotMatchExpr(matchExpr) : matchExpr;
  }

  /** Convert specified {@link TypedManagementObject} to a {@link LineAction}. */
  @Nonnull
  static LineAction toAction(@Nullable NamedManagementObject obj) {
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
  private static IpSpace toIpSpace(List<Uid> targets, Map<Uid, NamedManagementObject> objs) {
    return AclIpSpace.builder()
        .thenPermitting(
            targets.stream()
                .map(objs::get)
                .filter(Objects::nonNull) // TODO warn about missing refs?
                .map(CheckPointGatewayConversions::toIpSpace)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  /**
   * Returns an {@link IpSpace} containing the specified {@link NamedManagementObject}. Relies on a
   * named {@link IpSpace} existing for the supplied object.
   */
  @Nonnull
  private static IpSpace toIpSpace(NamedManagementObject obj) {
    if (obj instanceof CpmiAnyObject) {
      return UniverseIpSpace.INSTANCE;
    }
    return new IpSpaceReference(obj.getName());
  }

  /** Return {@code true} iff any of the install-on UIDs applies to the given gateway. */
  @SuppressWarnings("unused")
  static boolean appliesToGateway(List<Uid> installOn, GatewayOrServer gateway) {
    // TODO: implement
    return true;
  }

  /** Returns the name we use for IpAccessList of AccessLayer */
  public static String aclName(AccessLayer accessLayer) {
    return accessLayer.getUid().getValue();
  }

  /** Returns the name we use for IpAccessList of AccessSection */
  public static String aclName(AccessSection accessSection) {
    return accessSection.getUid().getValue();
  }

  /** Convert an entire CheckPoint port string to an {@link IntegerSpace}. */
  @VisibleForTesting
  static @Nonnull IntegerSpace portStringToIntegerSpace(String portStr) {
    String[] ranges = portStr.split(",", -1);
    IntegerSpace.Builder builder = IntegerSpace.builder();
    for (String range : ranges) {
      builder.including(portRangeStringToIntegerSpace(range.trim()));
    }
    return builder.build();
  }

  /** Convert a single element of a CheckPoint port string to an {@link IntegerSpace}. */
  @VisibleForTesting
  static @Nonnull IntegerSpace portRangeStringToIntegerSpace(String range) {
    if (range.isEmpty()) {
      // warn? all ports instead?
      return IntegerSpace.EMPTY;
    }
    IntegerSpace raw;
    char firstChar = range.charAt(0);
    if ('0' <= firstChar && firstChar <= '9') {
      // Examples:
      // 123
      // 50-90
      raw = IntegerSpace.parse(range);
    } else if (range.startsWith("<=")) {
      // Example: <=10
      raw = IntegerSpace.of(new SubRange(0, Integer.parseInt(range.substring(2))));
    } else if (range.startsWith("<")) {
      // Example: <10
      raw = IntegerSpace.of(new SubRange(0, Integer.parseInt(range.substring(1)) - 1));
    } else if (range.startsWith(">=")) {
      raw = IntegerSpace.of(new SubRange(Integer.parseInt(range.substring(2)), MAX_PORT_NUMBER));
    } else if (range.startsWith(">")) {
      raw =
          IntegerSpace.of(new SubRange(Integer.parseInt(range.substring(1)) + 1, MAX_PORT_NUMBER));
    } else {
      // unhandled
      // TODO: warn
      raw = IntegerSpace.EMPTY;
    }
    return raw.intersection(PORTS);
  }

  private CheckPointGatewayConversions() {}
}
