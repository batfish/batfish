package org.batfish.vendor.check_point_gateway.representation;

import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConfiguration.SYNC_INTERFACE_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AccessSection;
import org.batfish.vendor.check_point_management.AddressSpace;
import org.batfish.vendor.check_point_management.AddressSpaceToMatchExpr;
import org.batfish.vendor.check_point_management.Cluster;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.ManagementDomain;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.PolicyTargets;
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
    exprs.add(serviceToMatchExpr.visit((Service) service, true));
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
      warnings.redFlagf(
          "source %s has unsupported type %s and will be ignored", src.getName(), src.getClass());
      valid = false;
    }
    if (!(dst instanceof AddressSpace)) {
      warnings.redFlagf(
          "destination %s has unsupported type %s and will be ignored",
          dst.getName(), dst.getClass());
      valid = false;
    }
    if (!(service instanceof Service)) {
      warnings.redFlagf(
          "service %s has unsupported type %s and will be ignored",
          service.getName(), service.getClass());
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
      Map.Entry<ManagementDomain, GatewayOrServer> domainAndGateway,
      Warnings w) {
    ImmutableMap.Builder<String, IpAccessList> acls = ImmutableMap.builder();
    ImmutableList.Builder<AclLine> accessLayerLines = ImmutableList.builder();
    Uid policyTargetsUid =
        access.getObjectsDictionary().values().stream()
            .filter(PolicyTargets.class::isInstance)
            .map(NamedManagementObject::getUid)
            .findAny()
            .orElse(null);
    for (AccessRuleOrSection acl : access.getRulebase()) {
      if (acl instanceof AccessRule) {
        toAclLine(
                (AccessRule) acl,
                objects,
                serviceToMatchExpr,
                addressSpaceToMatchExpr,
                domainAndGateway,
                policyTargetsUid,
                w)
            .ifPresent(accessLayerLines::add);
        continue;
      }
      assert acl instanceof AccessSection;
      IpAccessList accessSection =
          toIpAccessList(
              (AccessSection) acl,
              objects,
              serviceToMatchExpr,
              addressSpaceToMatchExpr,
              domainAndGateway,
              policyTargetsUid,
              w);
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
      Map.Entry<ManagementDomain, GatewayOrServer> domainAndGateway,
      @Nullable Uid policyTargetsUid,
      Warnings w) {
    return IpAccessList.builder()
        .setName(aclName(section))
        .setSourceName(section.getName())
        .setLines(
            section.getRulebase().stream()
                .map(
                    r ->
                        toAclLine(
                            r,
                            objs,
                            serviceToMatchExpr,
                            addressSpaceToMatchExpr,
                            domainAndGateway,
                            policyTargetsUid,
                            w))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  /**
   * Convert specified {@link AccessRule} to an {@link AclLine}. Returns {@link Optional#empty()} if
   * the rule does not generate an {@link AclLine} (i.e. is not enabled).
   */
  static @Nonnull Optional<AclLine> toAclLine(
      AccessRule rule,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr,
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Map.Entry<ManagementDomain, GatewayOrServer> domainAndGateway,
      @Nullable Uid policyTargetsUid,
      Warnings w) {
    if (!rule.getEnabled()
        || !appliesToGateway(rule.getInstallOn(), policyTargetsUid, domainAndGateway)) {
      return Optional.empty();
    }
    LineAction action = toAction(objs.get(rule.getAction()), rule.getAction(), w);
    return Optional.of(
        ExprAclLine.builder()
            .setName(rule.getName())
            .setMatchCondition(
                toMatchExpr(
                    rule,
                    objs,
                    serviceToMatchExpr,
                    addressSpaceToMatchExpr,
                    action == LineAction.PERMIT,
                    w))
            .setAction(action)
            .build());
  }

  /**
   * Convert specified {@link AccessRule} into an {@link AclLineMatchExpr} representing the match
   * conditions of the rule.
   */
  static @Nonnull AclLineMatchExpr toMatchExpr(
      AccessRule rule,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr,
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      boolean permitting,
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
    AclLineMatchExpr svcMatch =
        servicesToMatchExpr(
            rule.getService(), objs, serviceToMatchExpr, permitting ^ rule.getServiceNegate(), w);
    conjuncts.add(rule.getServiceNegate() ? AclLineMatchExprs.not(svcMatch) : svcMatch);

    return AclLineMatchExprs.and(conjuncts.build());
  }

  /**
   * Returns an {@link AclLineMatchExpr} matching the specified {@link Service} {@link Uid}s.
   * Ignores {@link Uid}s for undefined or non-{@link Service} objects.
   */
  @VisibleForTesting
  static @Nonnull AclLineMatchExpr servicesToMatchExpr(
      List<Uid> services,
      Map<Uid, NamedManagementObject> objs,
      ServiceToMatchExpr serviceToMatchExpr,
      boolean permitting,
      Warnings w) {
    return AclLineMatchExprs.or(
        services.stream()
            .map(objs::get)
            .map(
                o -> {
                  if (!(o instanceof Service)) {
                    w.redFlagf(
                        "Cannot convert %s (type %s) to a service match expression,"
                            + " making unmatchable.",
                        o.getName(), o.getClass().getSimpleName());
                    return FalseExpr.INSTANCE;
                  }
                  return serviceToMatchExpr.visit((Service) o, permitting);
                })
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList()));
  }

  /** Convert specified {@link TypedManagementObject} to a {@link LineAction}. */
  static @Nonnull LineAction toAction(@Nullable NamedManagementObject obj, Uid uid, Warnings w) {
    if (obj == null) {
      w.redFlagf(
          "Cannot convert non-existent object (Uid '%s') into an access-rule action, defaulting"
              + " to deny action",
          uid.getValue());
      return LineAction.DENY;
    } else if (!(obj instanceof RulebaseAction)) {
      w.redFlagf(
          "Cannot convert object '%s' (Uid '%s') of type %s into an access-rule action,"
              + " defaulting to deny action",
          obj.getName(), uid.getValue(), obj.getClass().getSimpleName());
      return LineAction.DENY;
    } else {
      RulebaseAction ra = (RulebaseAction) obj;
      return switch (ra.getAction()) {
        case DROP -> LineAction.DENY;
        case ACCEPT -> LineAction.PERMIT;
        case UNHANDLED -> {
          w.redFlagf(
              "Cannot convert action '%s' (Uid '%s') into an access-rule action, defaulting to deny"
                  + " action",
              ra.getName(), uid.getValue());
          yield LineAction.DENY;
        }
      };
    }
  }

  /**
   * Returns an {@link AclLineMatchExpr} matching the specified {@link Uid}s. {@code matchSource}
   * indicates if they are matched as sources or destinations. Relies on named {@link IpSpace}s
   * existing for each of the supplied objects.
   */
  @VisibleForTesting
  static @Nonnull AclLineMatchExpr toAddressMatchExpr(
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
                    w.redFlagf(
                        "Cannot convert non-existent object (Uid '%s') to IpSpace," + " ignoring",
                        u.getValue());
                    IpSpace ref =
                        new IpSpaceReference(String.format("non-existent-%s", u.getValue()));
                    return matchSource
                        ? AclLineMatchExprs.matchSrc(ref)
                        : AclLineMatchExprs.matchDst(ref);
                  } else if (!(o instanceof AddressSpace)) {
                    String type = o.getClass().getSimpleName();
                    w.redFlagf(
                        "Cannot convert object '%s' (Uid '%s') of type '%s' to IpSpace,"
                            + " ignoring",
                        o.getName(), u.getValue(), type);
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

  /**
   * Return {@code true} iff any of the install-on UIDs applies to the given gateway.
   *
   * @param policyTargetsUid The UID of the {@link PolicyTargets} object, if it exists.
   */
  static boolean appliesToGateway(
      List<Uid> installOn,
      @Nullable Uid policyTargetsUid,
      Map.Entry<ManagementDomain, GatewayOrServer> domainAndGateway) {
    if (policyTargetsUid != null && installOn.contains(policyTargetsUid)) {
      // Install-on list includes policy targets. All gateways are affected.
      return true;
    }
    Set<Uid> toCheck = new HashSet<>(installOn);
    Uid gatewayUid = domainAndGateway.getValue().getUid();
    if (toCheck.contains(gatewayUid)) {
      return true;
    }
    Map<Uid, GatewayOrServer> gateways = domainAndGateway.getKey().getGatewaysAndServers();
    Set<Uid> alreadyChecked = new HashSet<>();
    Map<String, Uid> gatewayNamesToUids =
        gateways.entrySet().stream()
            .collect(ImmutableMap.toImmutableMap(e -> e.getValue().getName(), Map.Entry::getKey));
    while (!toCheck.isEmpty()) {
      Uid uid = toCheck.iterator().next();
      if (uid.equals(gatewayUid)) {
        return true;
      }
      toCheck.remove(uid);
      alreadyChecked.add(uid);
      GatewayOrServer gatewayOrServer = gateways.get(uid);
      if (gatewayOrServer instanceof Cluster) {
        ((Cluster) gatewayOrServer)
            .getClusterMemberNames().stream()
                .map(gatewayNamesToUids::get)
                .filter(memberUid -> memberUid != null && !alreadyChecked.contains(memberUid))
                .forEach(toCheck::add);
      }
    }
    return false;
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

  /** Create a VRRP group for the virtual IP the cluster associates with this interface. */
  static void createClusterVrrpGroup(
      Interface syncInterface,
      org.batfish.datamodel.Interface.Builder newSyncIface,
      @Nullable Map<String, org.batfish.vendor.check_point_management.Interface> clusterInterfaces,
      int clusterMemberIndex,
      Warnings warnings) {
    assert syncInterface.getName().equals(SYNC_INTERFACE_NAME);
    if (clusterInterfaces == null) {
      return;
    }

    ConcreteInterfaceAddress sourceAddress = syncInterface.getAddress();
    if (sourceAddress == null) {
      warnings.redFlag(
          "Cannot assign virtual IPs since Sync interface has no source IP for"
              + " control traffic");
    }
    VrrpGroup.Builder vgBuilder =
        VrrpGroup.builder()
            .setSourceAddress(sourceAddress)
            // prefer member with lowest cluster member index
            .setPriority(VrrpGroup.MAX_PRIORITY - clusterMemberIndex)
            .setPreempt(true);
    clusterInterfaces.forEach(
        (receivingIfaceName, clusterInterface) ->
            vgBuilder.addVirtualAddress(receivingIfaceName, clusterInterface.getIpv4Address()));

    newSyncIface.setVrrpGroups(ImmutableSortedMap.of(0, vgBuilder.build()));
  }
}
