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
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.AddressSpace;
import org.batfish.vendor.check_point_management.AddressSpaceToMatchExpr;
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
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Warnings warnings) {
    if (!checkValidHeaderSpaceInputs(src, dst, service, warnings)) {
      return Optional.empty();
    }
    // Guaranteed by checkValidHeaderSpaceInputs
    assert src instanceof AddressSpace;
    assert dst instanceof AddressSpace;
    ImmutableList.Builder<AclLineMatchExpr> exprs = ImmutableList.builder();
    exprs.add(addressSpaceToMatchExpr.convertSource((AddressSpace) src));
    exprs.add(addressSpaceToMatchExpr.convertDest((AddressSpace) dst));
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
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Warnings w) {
    ImmutableMap.Builder<String, IpAccessList> acls = ImmutableMap.builder();
    ImmutableList.Builder<AclLine> accessLayerLines = ImmutableList.builder();
    for (AccessRuleOrSection acl : access.getRulebase()) {
      if (acl instanceof AccessRule) {
        toAclLine((AccessRule) acl, objects, serviceToMatchExpr, addressSpaceToMatchExpr, w)
            .ifPresent(accessLayerLines::add);
        continue;
      }
      assert acl instanceof AccessSection;
      IpAccessList accessSection =
          toIpAccessList(
              (AccessSection) acl, objects, serviceToMatchExpr, addressSpaceToMatchExpr, w);
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
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Warnings w) {
    return IpAccessList.builder()
        .setName(aclName(section))
        .setSourceName(section.getName())
        .setLines(
            section.getRulebase().stream()
                .map(r -> toAclLine(r, objs, serviceToMatchExpr, addressSpaceToMatchExpr, w))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  /**
   * Convert specified {@link AccessRule} to an {@link AclLine}. Returns {@link Optional#empty()} if
   * the rule does not generate an {@link AclLine} (i.e. is not enabled).
   */
  @Nonnull
  static Optional<AclLine> toAclLine(
      AccessRule rule,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr,
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Warnings w) {
    if (!rule.getEnabled()) {
      return Optional.empty();
    }
    return Optional.of(
        ExprAclLine.builder()
            .setName(rule.getName())
            .setMatchCondition(
                toMatchExpr(rule, objs, serviceToMatchExpr, addressSpaceToMatchExpr, w))
            .setAction(toAction(objs.get(rule.getAction()), rule.getAction(), w))
            .build());
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
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Warnings w) {
    ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();

    // Source
    AclLineMatchExpr srcMatch =
        toAddressMatchExpr(rule.getSource(), objs, addressSpaceToMatchExpr, true, w);
    conjuncts.add(rule.getSourceNegate() ? AclLineMatchExprs.not(srcMatch) : srcMatch);

    // Dest
    AclLineMatchExpr dstMatch =
        toAddressMatchExpr(rule.getDestination(), objs, addressSpaceToMatchExpr, false, w);
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
   * Returns an {@link AclLineMatchExpr} matching the specified {@link Uid}s. {@code matchSource}
   * indicates if they are matched as sources or destinations. Relies on named {@link IpSpace}s
   * existing for each of the supplied objects.
   */
  @VisibleForTesting
  @Nonnull
  static AclLineMatchExpr toAddressMatchExpr(
      List<Uid> targets,
      Map<Uid, NamedManagementObject> objs,
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      boolean matchSource,
      Warnings w) {
    return AclLineMatchExprs.or(
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
                    IpSpace ref =
                        new IpSpaceReference(String.format("non-existent-%s", u.getValue()));
                    return matchSource
                        ? AclLineMatchExprs.matchSrc(ref)
                        : AclLineMatchExprs.matchDst(ref);
                  } else if (!(o instanceof AddressSpace)) {
                    String type = o.getClass().getSimpleName();
                    w.redFlag(
                        String.format(
                            "Cannot convert object '%s' (Uid '%s') of type '%s' to IpSpace,"
                                + " ignoring",
                            o.getName(), u.getValue(), type));
                    IpSpace ref =
                        new IpSpaceReference(
                            String.format("unsupported-%s-%s", type, u.getValue()));
                    return matchSource
                        ? AclLineMatchExprs.matchSrc(ref)
                        : AclLineMatchExprs.matchDst(ref);
                  }
                  AddressSpace addrSpace = (AddressSpace) o;
                  return matchSource
                      ? addressSpaceToMatchExpr.convertSource(addrSpace)
                      : addressSpaceToMatchExpr.convertDest(addrSpace);
                })
            .collect(ImmutableList.toImmutableList()));
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
