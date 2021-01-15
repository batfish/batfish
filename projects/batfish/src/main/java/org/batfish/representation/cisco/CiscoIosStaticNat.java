package org.batfish.representation.cisco;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.cisco.CiscoConfiguration.DEFAULT_STATIC_ROUTE_DISTANCE;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

@ParametersAreNonnullByDefault
public class CiscoIosStaticNat extends CiscoIosNat {

  private Prefix _localNetwork;
  private Prefix _globalNetwork;

  @Override
  protected int natCompare(CiscoIosNat o) {
    checkArgument(
        o instanceof CiscoIosStaticNat,
        "CiscoIosNat.natCompare should only be used for NATs of the same type.");
    CiscoIosStaticNat other = (CiscoIosStaticNat) o;
    // Rules with longer prefixes should come first
    return Comparator.comparing(Prefix::getPrefixLength)
        .reversed()
        .compare(_localNetwork, other._localNetwork);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof CiscoIosStaticNat)) {
      return false;
    }
    CiscoIosStaticNat other = (CiscoIosStaticNat) o;
    return (getAction() == other.getAction())
        && (getAddRoute() == other.getAddRoute())
        && Objects.equals(getVrf(), other.getVrf())
        && Objects.equals(_localNetwork, other._localNetwork)
        && Objects.equals(_globalNetwork, other._globalNetwork);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAction(), getAddRoute(), _localNetwork, _globalNetwork, getVrf());
  }

  public Prefix getLocalNetwork() {
    return _localNetwork;
  }

  public void setLocalNetwork(Prefix localNetwork) {
    _localNetwork = localNetwork;
  }

  public Prefix getGlobalNetwork() {
    return _globalNetwork;
  }

  public void setGlobalNetwork(Prefix globalNetwork) {
    _globalNetwork = globalNetwork;
  }

  @Override
  public Optional<Transformation.Builder> toOutgoingTransformation(
      Map<String, RouteMap> routeMaps,
      Map<String, NatPool> natPools,
      Set<String> insideInterfaces,
      Map<String, Interface> interfaces,
      Configuration c) {

    /*
     * No named ACL in rule, but need to match src/dest to global/local according
     * to direction and rule type
     */
    AclLineMatchExpr matchExpr;
    TransformationStep step;
    switch (getAction()) {
      case SOURCE_INSIDE:
        matchExpr = AclLineMatchExprs.matchSrc(_localNetwork);
        step = TransformationStep.shiftSourceIp(_globalNetwork);
        break;
      case SOURCE_OUTSIDE:
        matchExpr = AclLineMatchExprs.matchDst(_localNetwork);
        step = TransformationStep.shiftDestinationIp(_globalNetwork);
        break;
      default:
        return Optional.empty();
    }

    matchExpr = AclLineMatchExprs.and(matchExpr, new MatchSrcInterface(insideInterfaces));
    return Optional.of(Transformation.when(matchExpr).apply(step));
  }

  @Override
  public Optional<Transformation.Builder> toIncomingTransformation(
      Map<String, IpAccessList> ipAccessLists,
      Map<String, RouteMap> routeMaps,
      Map<String, NatPool> natPools,
      Map<String, Interface> interfaces) {
    /*
     * No named ACL in rule, but need to match src/dest to global/local according
     * to direction and rule type
     */
    AclLineMatchExpr matchExpr;
    TransformationStep step;
    switch (getAction()) {
      case SOURCE_INSIDE:
        matchExpr = AclLineMatchExprs.matchDst(_globalNetwork);
        step = TransformationStep.shiftDestinationIp(_localNetwork);
        break;
      case SOURCE_OUTSIDE:
        matchExpr = AclLineMatchExprs.matchSrc(_globalNetwork);
        step = TransformationStep.shiftSourceIp(_localNetwork);
        break;
      default:
        return Optional.empty();
    }

    return Optional.of(Transformation.when(matchExpr).apply(step));
  }

  @Override
  public Optional<StaticRoute> toRoute() {
    if (!getAddRoute() || getVrf() != null) {
      return Optional.empty();
    }
    assert getAction() == RuleAction.SOURCE_OUTSIDE; // only valid option for add-route
    return Optional.of(
        new StaticRoute(
            _localNetwork,
            _globalNetwork.getStartIp(),
            null,
            DEFAULT_STATIC_ROUTE_DISTANCE,
            null,
            null,
            false));
  }
}
