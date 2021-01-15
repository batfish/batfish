package org.batfish.representation.cisco;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/** Representation of a Cisco IOS dynamic NAT. */
@ParametersAreNonnullByDefault
public final class CiscoIosDynamicNat extends CiscoIosNat {

  /* NAT must use either an ACL or a route-map to specify traffic to match. */
  private @Nullable String _aclName;
  private @Nullable String _routeMap;

  /* Interface whose address to use as pool (this and _natPool are mutually exclusive) */
  private @Nullable String _interface;
  private @Nullable String _natPool;
  // TODO: model overload. Overload is a relatively simple feature that adds PAT on top of NAT,
  // which kicks in when the existing ports already have sessions in the table. In Batfish terms,
  // this requires modeling a fairly complicated if-then-else structure after matching a NAT rule,
  // which is possible in the VI model but complicated to build.
  //
  // The bug modeling this could catch is if a session can traverse the nat rule but some downstream
  // filter blocks the port-translated flows, which only shows up under load.
  /* Overload, aka PAT. */
  private boolean _overload;

  @VisibleForTesting
  public static String computeDynamicDestinationNatAclName(@Nonnull String natAclName) {
    return String.format("~DYNAMIC_DESTINATION_NAT_INSIDE_ACL~%s~", natAclName);
  }

  /** ACL specifying matching traffic (mutually exclusive with {@link #getRouteMap() route-map}) */
  @Nullable
  public String getAclName() {
    return _aclName;
  }

  public void setAclName(@Nullable String aclName) {
    _aclName = aclName;
  }

  public boolean getOverload() {
    return _overload;
  }

  public void setOverload(boolean overload) {
    _overload = overload;
  }

  public @Nullable String getInterface() {
    return _interface;
  }

  public void setInterface(@Nullable String iface) {
    _interface = iface;
  }

  public @Nullable String getNatPool() {
    return _natPool;
  }

  public void setNatPool(@Nullable String natPool) {
    _natPool = natPool;
  }

  /** Route-map specifying matching traffic (mutually exclusive with {@link #getAclName() ACL}) */
  @Nullable
  public String getRouteMap() {
    return _routeMap;
  }

  public void setRouteMap(@Nullable String routeMap) {
    _routeMap = routeMap;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof CiscoIosDynamicNat)) {
      return false;
    }
    CiscoIosDynamicNat other = (CiscoIosDynamicNat) o;
    return (getAction() == other.getAction())
        && (getAddRoute() == other.getAddRoute())
        && Objects.equals(getVrf(), other.getVrf())
        && Objects.equals(_aclName, other._aclName)
        && Objects.equals(_interface, other._interface)
        && Objects.equals(_natPool, other._natPool)
        && Objects.equals(_overload, other._overload)
        && Objects.equals(_routeMap, other._routeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _aclName, getAction(), getAddRoute(), _interface, _natPool, _overload, _routeMap, getVrf());
  }

  @Override
  protected int natCompare(CiscoIosNat o) {
    checkArgument(
        o instanceof CiscoIosDynamicNat,
        "CiscoIosNat.natCompare should only be used for NATs of the same type.");
    CiscoIosDynamicNat other = (CiscoIosDynamicNat) o;
    /* Based on GNS3 testing:
     - Dynamic NAT rules configured with ACLs come before those configured with route-maps
     - ACLs with numeric names come first in numerical order, then others in lexicographical order
     - Route-maps are ordered lexicographically
     - It is not possible to configure two rules with the same structure
    */
    int aclsCompare =
        Comparator.comparing(
                CiscoIosDynamicNat::toIntOrNull, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(Comparator.nullsLast(String::compareTo))
            .compare(_aclName, other._aclName);
    if (aclsCompare != 0) {
      return aclsCompare;
    }
    // Neither NAT has an ACL defined. That should mean both have route-maps defined, but if either
    // has a null route-map, deprioritize that one as it can't be converted at all.
    return Comparator.nullsLast(String::compareTo).compare(_routeMap, other.getRouteMap());
  }

  /** Converts given ACL name to an int if possible, otherwise returns null. */
  @Nullable
  private static Integer toIntOrNull(@Nullable String aclName) {
    try {
      return Integer.parseInt(aclName); // throws same error for null and non-numeric names
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public Optional<Transformation.Builder> toIncomingTransformation(
      Map<String, IpAccessList> ipAccessLists,
      Map<String, NatPool> natPools,
      Map<String, Interface> interfaces) {
    // SOURCE_OUTSIDE matches and dynamically translates source addresses on ingress.
    if (getAction() != RuleAction.SOURCE_OUTSIDE) {
      // INSIDE rules require reverse translation on ingress, which is not
      // yet supported (we need to track NAT table state for dynamic NAT).
      return Optional.empty();
    }

    if (isMalformed(natPools, ipAccessLists, interfaces)) {
      return Optional.empty();
    }

    return Optional.of(makeTransformation(permittedByAcl(_aclName), false, natPools, interfaces));
  }

  @Override
  public Optional<Transformation.Builder> toOutgoingTransformation(
      Map<String, NatPool> natPools,
      Set<String> insideInterfaces,
      Map<String, Interface> interfaces,
      Configuration c) {
    /*
     * SOURCE_INSIDE matches and dynamically translates source addresses on egress
     * DESTINATION_INSIDE matches and dynamically translates destination addresses on egress
     */

    if (getAction() != RuleAction.SOURCE_INSIDE && getAction() != RuleAction.DESTINATION_INSIDE) {
      // OUTSIDE rules require reverse translation on egress, which is not
      // yet supported (we need to track NAT table state for dynamic NAT).
      return Optional.empty();
    }

    if (isMalformed(natPools, c.getIpAccessLists(), interfaces)) {
      return Optional.empty();
    }
    assert _aclName != null; // invariant of isMalformed being false, to help compiler.

    IpAccessList natAcl = c.getIpAccessLists().get(_aclName);

    if (getAction() == RuleAction.DESTINATION_INSIDE) {
      // Expect all lines to be header space matches for NAT ACL
      if (!natAcl.getLines().stream()
          .allMatch(
              l ->
                  l instanceof ExprAclLine
                      && ((ExprAclLine) l).getMatchCondition() instanceof MatchHeaderSpace)) {
        return Optional.empty();
      }

      // Create reverse acl to match destination address instead of source address
      String reverseAclName = computeDynamicDestinationNatAclName(_aclName);
      List<AclLine> lines =
          natAcl.getLines().stream()
              // Already checked that all lines are instances of ExprAclLine
              .map(ExprAclLine.class::cast)
              .map(
                  line -> {
                    HeaderSpace origHeader =
                        ((MatchHeaderSpace) line.getMatchCondition()).getHeaderspace();
                    HeaderSpace headerSpace =
                        origHeader.toBuilder()
                            .setDstIps(origHeader.getSrcIps())
                            .setSrcIps((IpSpace) null)
                            .build();
                    return ExprAclLine.builder()
                        .setAction(line.getAction())
                        .setMatchCondition(new MatchHeaderSpace(headerSpace))
                        .build();
                  })
              .collect(Collectors.toList());
      natAcl =
          IpAccessList.builder()
              .setLines(lines)
              .setName(reverseAclName)
              .setOwner(c)
              .setSourceName(_aclName)
              .setSourceType(natAcl.getSourceType())
              .build();
    }

    AclLineMatchExpr natAclExpr =
        and(permittedByAcl(natAcl.getName()), new MatchSrcInterface(insideInterfaces));
    return Optional.of(makeTransformation(natAclExpr, true, natPools, interfaces));
  }

  /**
   * Returns the (forward) transformation for this dynamic NAT expression using the given condition
   * on which to NAT, the direction of traffic, and the available NatPools and Interfaces. Assumes
   * {@link #isMalformed(Map, Map, Map)} has passed.
   */
  private Transformation.Builder makeTransformation(
      AclLineMatchExpr shouldNat,
      boolean outgoing,
      Map<String, NatPool> natPools,
      Map<String, Interface> interfaces) {
    if (_natPool != null) {
      NatPool pool = natPools.get(_natPool);
      return makeTransformation(shouldNat, pool.getFirst(), pool.getLast(), outgoing);
    } else {
      Ip ifaceAddress = interfaces.get(_interface).getAddress().getIp();
      return makeTransformation(shouldNat, ifaceAddress, ifaceAddress, outgoing);
    }
  }

  /**
   * Returns the (forward) transformation for this dynamic NAT expression using the given condition
   * on which to NAT, endpoint IPs for the pool, and the direction of traffic.
   */
  private Transformation.Builder makeTransformation(
      AclLineMatchExpr shouldNat, Ip first, Ip last, boolean outgoing) {
    TransformationStep step =
        getAction().whatChanges(outgoing) == IpField.SOURCE
            ? assignSourceIp(first, last)
            : assignDestinationIp(first, last);
    return when(shouldNat).apply(step);
  }

  /**
   * Returns {@code true} iff this dynamic NAT configuration is invalid based on the given existing
   * NAT pools and access lists.
   */
  private boolean isMalformed(
      Map<String, NatPool> natPools,
      Map<String, IpAccessList> ipAccessLists,
      Map<String, Interface> interfaces) {
    // Validate that ACL is configured, present, and is a standard ACL.
    if (_aclName == null) {
      // Not configured (should be rejected by parser, but confirm).
      return true;
    }
    if (!ipAccessLists.containsKey(_aclName)) {
      // Invalid reference
      return true;
    }
    if (!CiscoStructureType.IPV4_ACCESS_LIST_STANDARD
        .getDescription()
        .equals(ipAccessLists.get(_aclName).getSourceType())) {
      // Cisco IOS only supports standard ACLs for dynamic NAT.
      return true;
    }

    // Validate that NAT pool xor interface is configured and valid.
    // Parser allows unconfigured NAT pool for Arista NAT (nat overload), but it is not supported.
    if ((_natPool == null) == (_interface == null)) {
      // this shouldn't be possible from extraction, but check anyway
      return true;
    }
    if (_natPool == null) {
      // Interface can only be used for inside source NAT (at least on IOS).
      if (getAction() != RuleAction.SOURCE_INSIDE) {
        return true;
      }
      Interface iface = interfaces.get(_interface);
      if (iface == null || iface.getAddress() == null) {
        return true;
      }
    } else if (!natPools.containsKey(_natPool)) {
      // Configuration has an invalid reference
      return true;
    }

    return false;
  }

  @Override
  public Optional<StaticRoute> toRoute() {
    // TODO Create a route if this NAT is in default VRF and has add-route set
    // TODO Check if a route is still created if the NAT is invalid per isMalformed
    return Optional.empty();
  }
}
