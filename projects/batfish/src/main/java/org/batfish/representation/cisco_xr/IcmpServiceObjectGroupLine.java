package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class IcmpServiceObjectGroupLine implements ServiceObjectGroupLine {

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(
        HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.ICMP)).build());
  }
}
