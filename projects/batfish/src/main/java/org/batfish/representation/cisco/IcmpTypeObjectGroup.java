package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public class IcmpTypeObjectGroup extends ObjectGroup {

  private final List<IcmpTypeObjectGroupLine> _lines;

  public IcmpTypeObjectGroup(String name) {
    super(name);
    _lines = new LinkedList<>();
  }

  public List<IcmpTypeObjectGroupLine> getLines() {
    return _lines;
  }

  public AclLineMatchExpr toAclLineMatchExpr() {
    return or(
        _lines.stream()
            .map(IcmpTypeObjectGroupLine::toAclLineMatchExpr)
            .collect(ImmutableSet.toImmutableSet()));
  }
}
