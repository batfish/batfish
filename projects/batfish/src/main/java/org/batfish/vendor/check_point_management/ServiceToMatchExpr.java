package org.batfish.vendor.check_point_management;

import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
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
    // TODO implement
    return TrueExpr.INSTANCE;
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

  @SuppressWarnings("unused")
  private final @Nonnull Map<Uid, NamedManagementObject> _objs;
}
