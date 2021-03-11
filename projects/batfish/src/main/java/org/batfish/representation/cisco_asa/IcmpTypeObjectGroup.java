package org.batfish.representation.cisco_asa;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;

public class IcmpTypeObjectGroup extends ObjectGroup {

  private List<IcmpTypeObjectGroupLine> _lines;

  public IcmpTypeObjectGroup(String name) {
    super(name);
    _lines = new LinkedList<>();
  }

  public List<IcmpTypeObjectGroupLine> getLines() {
    return _lines;
  }

  public AclLineMatchExpr toAclLineMatchExpr() {
    return new OrMatchExpr(
        _lines.stream()
            .map(IcmpTypeObjectGroupLine::toAclLineMatchExpr)
            .collect(ImmutableSet.toImmutableSet()));
  }
}
