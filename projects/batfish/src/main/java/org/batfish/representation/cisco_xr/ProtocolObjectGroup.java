package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;

public class ProtocolObjectGroup extends ObjectGroup {

  private final List<ProtocolObjectGroupLine> _lines;

  public ProtocolObjectGroup(String name) {
    super(name);
    _lines = new LinkedList<>();
  }

  public List<ProtocolObjectGroupLine> getLines() {
    return _lines;
  }

  public AclLineMatchExpr toAclLineMatchExpr() {
    return new OrMatchExpr(
        _lines.stream()
            .map(ProtocolObjectGroupLine::toAclLineMatchExpr)
            .collect(ImmutableSet.toImmutableSet()));
  }
}
