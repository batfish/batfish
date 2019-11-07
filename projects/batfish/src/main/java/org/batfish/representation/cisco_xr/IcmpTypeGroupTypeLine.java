package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
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
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(
        HeaderSpace.builder().setIcmpTypes(ImmutableList.of(new SubRange(_type))).build());
  }
}
