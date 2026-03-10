package org.batfish.representation.cisco_asa;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class IcmpTypeGroupTypeLine implements IcmpTypeObjectGroupLine {

  private final int _type;

  public IcmpTypeGroupTypeLine(int type) {
    _type = type;
  }

  public int getType() {
    return _type;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(
      Map<String, IcmpTypeObjectGroup> icmpTypeObjectGroups) {
    return new MatchHeaderSpace(
        HeaderSpace.builder()
            .setIpProtocols(IpProtocol.ICMP)
            .setIcmpTypes(ImmutableList.of(new SubRange(_type)))
            .build());
  }
}
