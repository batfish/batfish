package org.batfish.vendor.check_point_gateway.representation;

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
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.FalseExpr;
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
    // Guaranteed by checkValidHeaderSpaceInputs
    assert src instanceof AddressSpace;
    assert dst instanceof AddressSpace;
    ImmutableList.Builder<AclLineMatchExpr> exprs = ImmutableList.builder();
    exprs.add(AclLineMatchExprs.matchSrc(toIpSpace((AddressSpace) src)));
    exprs.add(AclLineMatchExprs.matchDst(toIpSpace((AddressSpace) dst)));
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
      ServiceToMatchExpr serviceToMatchExpr,
      Warnings w) {
    ImmutableMap.Builder<String, IpAccessList> acls = ImmutableMap.builder();
    ImmutableList.Builder<AclLine> accessLayerLines = ImmutableList.builder();
    for (AccessRuleOrSection acl : access.getRulebase()) {
      if (acl instanceof AccessRule) {
        accessLayerLines.add(toAclLine((AccessRule) acl, objects, serviceToMatchExpr, w));
        continue;
      }
      assert acl instanceof AccessSection;
      IpAccessList accessSection =
          toIpAccessList((AccessSection) acl, objects, serviceToMatchExpr, w);
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
      ServiceToMatchExpr serviceToMatchExpr,
      Warnings w) {
    return IpAccessList.builder()
        .setName(aclName(section))
        .setSourceName(section.getName())
        .setLines(
            section.getRulebase().stream()
                .map(r -> toAclLine(r, objs, serviceToMatchExpr, w))
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  /** Convert specified {@link AccessRule} to an {@link AclLine}. */
  @Nonnull
  static AclLine toAclLine(
      AccessRule rule,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr,
      Warnings w) {
    return ExprAclLine.builder()
        .setName(rule.getName())
        .setMatchCondition(toMatchExpr(rule, objs, serviceToMatchExpr, w))
        .setAction(toAction(objs.get(rule.getAction()), rule.getAction(), w))
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
      ServiceToMatchExpr serviceToMatchExpr,
      Warnings w) {
    ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();

    // Source
    AclLineMatchExpr srcMatch = AclLineMatchExprs.matchSrc(toIpSpace(rule.getSource(), objs, w));
    conjuncts.add(rule.getSourceNegate() ? AclLineMatchExprs.not(srcMatch) : srcMatch);

    // Dest
    AclLineMatchExpr dstMatch =
        AclLineMatchExprs.matchDst(toIpSpace(rule.getDestination(), objs, w));
    conjuncts.add(rule.getDestinationNegate() ? AclLineMatchExprs.not(dstMatch) : dstMatch);

    // Service
    AclLineMatchExpr svcMatch = servicesToMatchExpr(rule.getService(), objs, serviceToMatchExpr, w);
    conjuncts.add(rule.getServiceNegate() ? AclLineMatchExprs.not(svcMatch) : svcMatch);

    return AclLineMatchExprs.and(conjuncts.build());
  }

  /**
   * Returns an {@link AclLineMatchExpr} matching the specified {@link Service} {@link Uid}s.
   * Ignores {@link Uid}s for undefined or non-{@link Service} objects.
   */
  @VisibleForTesting
  @Nonnull
  static AclLineMatchExpr servicesToMatchExpr(
      List<Uid> services,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr,
      Warnings w) {
    return AclLineMatchExprs.or(
        services.stream()
            .map(objs::get)
            .map(
                o -> {
                  if (!(o instanceof Service)) {
                    w.redFlag(
                        String.format(
                            "Cannot convert %s (type %s) to a service match expression,"
                                + " making unmatchable.",
                            o.getName(), o.getClass().getSimpleName()));
                    return FalseExpr.INSTANCE;
                  }
                  return serviceToMatchExpr.visit((Service) o);
                })
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList()));
  }

  /** Convert specified {@link TypedManagementObject} to a {@link LineAction}. */
  @Nonnull
  static LineAction toAction(@Nullable NamedManagementObject obj, Uid uid, Warnings w) {
    if (obj == null) {
      w.redFlag(
          String.format(
              "Cannot convert non-existent object (Uid '%s') into an access-rule action, defaulting"
                  + " to deny action",
              uid.getValue()));
      return LineAction.DENY;
    } else if (!(obj instanceof RulebaseAction)) {
      w.redFlag(
          String.format(
              "Cannot convert object '%s' (Uid '%s') of type %s into an access-rule action,"
                  + " defaulting to deny action",
              obj.getName(), uid.getValue(), obj.getClass().getSimpleName()));
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
          w.redFlag(
              String.format(
                  "Cannot convert action '%s' (Uid '%s') into an access-rule action, defaulting to"
                      + " deny action",
                  ra.getName(), uid.getValue()));
          return LineAction.DENY;
      }
    }
  }

  /**
   * Returns an {@link IpSpace} containing the specified {@link Uid}s. Relies on named {@link
   * IpSpace}s existing for each of the supplied object's {@link Uid}s.
   */
  @VisibleForTesting
  @Nonnull
  static IpSpace toIpSpace(List<Uid> targets, Map<Uid, NamedManagementObject> objs, Warnings w) {
    return AclIpSpace.builder()
        .thenPermitting(
            targets.stream()
                .map(
                    u -> {
                      NamedManagementObject o = objs.get(u);
                      if (o == null) {
                        w.redFlag(
                            String.format(
                                "Cannot convert non-existent object (Uid '%s') to IpSpace,"
                                    + " ignoring",
                                u.getValue()));
                        return new IpSpaceReference(String.format("non-existent-%s", u.getValue()));
                      } else if (!(o instanceof AddressSpace)) {
                        String type = o.getClass().getSimpleName();
                        w.redFlag(
                            String.format(
                                "Cannot convert object '%s' (Uid '%s') of type '%s' to IpSpace,"
                                    + " ignoring",
                                o.getName(), u.getValue(), type));
                        return new IpSpaceReference(
                            String.format("unsupported-%s-%s", type, u.getValue()));
                      }
                      return toIpSpace((AddressSpace) o);
                    })
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  /**
   * Returns an {@link IpSpace} containing the specified {@link NamedManagementObject}. Relies on a
   * named {@link IpSpace} existing for the supplied object.
   */
  @Nonnull
  private static IpSpace toIpSpace(AddressSpace obj) {
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
    return String.format("%s (%s)", accessLayer.getUid().getValue(), accessLayer.getName());
  }

  /** Returns the name we use for IpAccessList of AccessSection */
  public static String aclName(AccessSection accessSection) {
    String uid = accessSection.getUid().getValue();
    // Using a generated name, so just use Uid as the name
    if (accessSection.getName().equals(AccessSection.generateName(accessSection.getUid()))) {
      return uid;
    }
    return String.format("%s (%s)", uid, accessSection.getName());
  }

  private CheckPointGatewayConversions() {}
}
