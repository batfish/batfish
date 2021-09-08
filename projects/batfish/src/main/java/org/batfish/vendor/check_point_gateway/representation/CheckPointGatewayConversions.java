package org.batfish.vendor.check_point_gateway.representation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
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
import org.batfish.datamodel.IpProtocol;
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
import org.batfish.vendor.check_point_management.AddressSpace;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.Service;
import org.batfish.vendor.check_point_management.ServiceGroup;
import org.batfish.vendor.check_point_management.ServiceTcp;
import org.batfish.vendor.check_point_management.ServiceVisitor;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;

/** Utility class for Checkpoint conversion methods */
public final class CheckPointGatewayConversions {

  /**
   * Convert src, dst, and service to a {@link HeaderSpace} if they are of valid types. Else, return
   * {@link Optional#empty()}.
   */
  static @Nonnull Optional<HeaderSpace> toHeaderSpace(
      TypedManagementObject src,
      TypedManagementObject dst,
      TypedManagementObject service,
      Warnings warnings) {
    if (!checkValidHeaderSpaceInputs(src, dst, service, warnings)) {
      return Optional.empty();
    }
    HeaderSpace.Builder hsb = HeaderSpace.builder();
    if (!(src instanceof CpmiAnyObject)) {
      hsb.setSrcIps(new IpSpaceReference(src.getName()));
    }
    if (!(dst instanceof CpmiAnyObject)) {
      hsb.setDstIps(new IpSpaceReference(dst.getName()));
    }
    applyServiceConstraint((Service) service, hsb);
    return Optional.of(hsb.build());
  }

  /**
   * Returns {@code true} iff the source, destination, and service for a NAT or access rule are
   * valid. In context of nat, refers to original fields. Warns for each invalid field.
   */
  @VisibleForTesting
  static boolean checkValidHeaderSpaceInputs(
      TypedManagementObject src,
      TypedManagementObject dst,
      TypedManagementObject service,
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
      @Nonnull AccessSection section, Map<Uid, TypedManagementObject> objs) {
    return IpAccessList.builder()
        .setName(aclName(section))
        .setSourceName(section.getName())
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

  private static final ServiceToHeaderSpaceConstraints SERVICE_TO_HEADER_SPACE_CONSTRAINTS =
      new ServiceToHeaderSpaceConstraints();

  /**
   * Restricts the given {@link HeaderSpace.Builder} to protocols/ports matching the given {@link
   * Service}.
   */
  public static void applyServiceConstraint(Service service, HeaderSpace.Builder hsb) {
    SERVICE_TO_HEADER_SPACE_CONSTRAINTS.setHeaderSpace(hsb);
    service.accept(SERVICE_TO_HEADER_SPACE_CONSTRAINTS);
    SERVICE_TO_HEADER_SPACE_CONSTRAINTS.setHeaderSpace(null);
  }

  /**
   * Applies a {@link Service} to its current {@link HeaderSpace.Builder}. Does not modify the
   * headerspace if the given service object is unconstrained.
   */
  private static class ServiceToHeaderSpaceConstraints implements ServiceVisitor<Void> {
    private @Nullable HeaderSpace.Builder _hsb;

    private ServiceToHeaderSpaceConstraints() {}

    private void setHeaderSpace(@Nullable HeaderSpace.Builder hsb) {
      _hsb = hsb;
    }

    @Override
    public Void visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
      // Does not constrain headerspace
      return null;
    }

    @Override
    public Void visitServiceGroup(ServiceGroup serviceGroup) {
      // TODO Implement
      return null;
    }

    @Override
    public Void visitServiceTcp(ServiceTcp serviceTcp) {
      // TODO Is this correct/sufficient? Does it need to modify src port?
      //      Also, need to verify that port is an integer and decide what to do if not
      assert _hsb != null;
      _hsb.setIpProtocols(IpProtocol.TCP);
      _hsb.setDstPorts(IntegerSpace.parse(serviceTcp.getPort()).getSubRanges());
      return null;
    }
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

  private CheckPointGatewayConversions() {}
}
