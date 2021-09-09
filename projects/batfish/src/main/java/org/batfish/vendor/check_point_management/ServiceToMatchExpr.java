package org.batfish.vendor.check_point_management;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.TrueExpr;

/** Generates an {@link AclLineMatchExpr} for the specified {@link Service}. */
public class ServiceToMatchExpr implements ServiceVisitor<AclLineMatchExpr> {

  public ServiceToMatchExpr(Map<Uid, NamedManagementObject> objs) {
    _objs = objs;
  }

  @Override
  public AclLineMatchExpr visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
    // Does not constrain headerspace
    return TrueExpr.INSTANCE;
  }

  @Override
  public AclLineMatchExpr visitServiceGroup(ServiceGroup serviceGroup) {
    Set<Uid> allMembers = getDescendantObjects(serviceGroup, new HashSet<>());
    List<AclLineMatchExpr> matchExprs =
        allMembers.stream()
            .map(_objs::get)
            .filter(Service.class::isInstance)
            .map(Service.class::cast)
            .map(s -> s.accept(this))
            .collect(ImmutableList.toImmutableList());
    return new OrMatchExpr(matchExprs);
  }

  @Override
  public AclLineMatchExpr visitServiceIcmp(ServiceIcmp serviceIcmp) {
    HeaderSpace.Builder hsb = HeaderSpace.builder();
    hsb.setIpProtocols(IpProtocol.ICMP);
    hsb.setIcmpTypes(serviceIcmp.getIcmpType());
    Optional.ofNullable(serviceIcmp.getIcmpCode()).ifPresent(hsb::setIcmpCodes);
    return new MatchHeaderSpace(hsb.build());
  }

  @Override
  public AclLineMatchExpr visitServiceTcp(ServiceTcp serviceTcp) {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.TCP)
            .setDstPorts(IntegerSpace.parse(serviceTcp.getPort()).getSubRanges())
            .build());
  }

  @Override
  public AclLineMatchExpr visitServiceUdp(ServiceUdp serviceUdp) {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.UDP)
            .setDstPorts(IntegerSpace.parse(serviceUdp.getPort()).getSubRanges())
            .build());
  }

  /**
   * Returns descendant objects for the specified {@link ServiceGroup}. Keeps track of visited
   * descendants to prevent loops, though these should not occur in real configs.
   */
  private Set<Uid> getDescendantObjects(ServiceGroup group, Set<Uid> alreadyTraversedMembers) {
    Uid groupUid = group.getUid();
    if (alreadyTraversedMembers.contains(groupUid)) {
      return ImmutableSet.of();
    }
    alreadyTraversedMembers.add(groupUid);

    Set<Uid> descendantObjects = new HashSet<>();
    for (Uid memberUid : group.getMembers()) {
      NamedManagementObject member = _objs.get(memberUid);
      if (member instanceof ServiceGroup) {
        descendantObjects.addAll(
            getDescendantObjects((ServiceGroup) member, alreadyTraversedMembers));
      } else if (member instanceof Service) {
        descendantObjects.add(memberUid);
      }
    }
    return descendantObjects;
  }

  private final @Nonnull Map<Uid, NamedManagementObject> _objs;
}
