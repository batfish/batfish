package org.batfish.vendor.check_point_management;

import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.addressCpmiAnyTraceElement;
import static org.batfish.vendor.check_point_management.CheckPointManagementTraceElementCreators.addressGroupTraceElement;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.FalseExpr;

/**
 * Create an {@link AclLineMatchExpr} representing the visited {@link AddressSpace}. Matches the
 * {@link AddressSpace} as a source or destination based on the last value supplied to {@link
 * AddressSpaceToMatchExpr#setMatchSource(boolean)}.
 *
 * <p><i>Relies on named {@link IpSpace}s existing for the {@code AddressSpace} and all its
 * children.</i>
 */
public class AddressSpaceToMatchExpr implements AddressSpaceVisitor<AclLineMatchExpr> {

  /**
   * Set boolean indicating if subsequent visits to the {@link AclLineMatchExpr} should match on
   * source or destination address.
   */
  public void setMatchSource(boolean matchSource) {
    _matchSource = matchSource;
  }

  @Override
  public AclLineMatchExpr visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
    return matchExpr(UniverseIpSpace.INSTANCE, addressCpmiAnyTraceElement(_matchSource));
  }

  @Override
  public AclLineMatchExpr visitAddressRange(AddressRange addressRange) {
    return matchExpr(new IpSpaceReference(addressRange.getName()));
  }

  @Override
  public AclLineMatchExpr visitGatewayOrServer(GatewayOrServer gatewayOrServer) {
    return matchExpr(new IpSpaceReference(gatewayOrServer.getName()));
  }

  @Override
  public AclLineMatchExpr visitGroup(Group group) {
    return getDescendantMatchExpr(group, new HashSet<>());
  }

  /**
   * Returns an {@link AclLineMatchExpr} representing descendant objects. Keeps track of visited
   * descendants to prevent loops, though these should not occur in real configs.
   */
  private AclLineMatchExpr getDescendantMatchExpr(Group group, Set<Uid> alreadyTraversedMembers) {
    Uid groupUid = group.getUid();
    alreadyTraversedMembers.add(groupUid);

    List<AclLineMatchExpr> descendantObjExprs = new ArrayList<>();
    for (Uid memberUid : group.getMembers()) {
      NamedManagementObject member = _objs.get(memberUid);
      if (member instanceof Group) {
        if (!alreadyTraversedMembers.contains(memberUid)) {
          descendantObjExprs.add(getDescendantMatchExpr((Group) member, alreadyTraversedMembers));
        }
      } else if (member instanceof AddressSpace) {
        descendantObjExprs.add(((AddressSpace) member).accept(this));
      } else {
        // Don't match non-address-space objects
        descendantObjExprs.add(FalseExpr.INSTANCE);
      }
    }
    return AclLineMatchExprs.or(addressGroupTraceElement(group, _matchSource), descendantObjExprs);
  }

  @Override
  public AclLineMatchExpr visitHost(Host host) {
    return matchExpr(new IpSpaceReference(host.getName()));
  }

  @Override
  public AclLineMatchExpr visitNetwork(Network network) {
    return matchExpr(new IpSpaceReference(network.getName()));
  }

  private AclLineMatchExpr matchExpr(IpSpace ipSpace) {
    return matchExpr(ipSpace, null);
  }

  @VisibleForTesting
  AclLineMatchExpr matchExpr(IpSpace ipSpace, @Nullable TraceElement traceElement) {
    return _matchSource
        ? AclLineMatchExprs.matchSrc(ipSpace, traceElement)
        : AclLineMatchExprs.matchDst(ipSpace, traceElement);
  }

  public AddressSpaceToMatchExpr(Map<Uid, NamedManagementObject> objs) {
    _objs = objs;
  }

  private boolean _matchSource;
  private final Map<Uid, NamedManagementObject> _objs;
}
