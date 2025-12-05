package org.batfish.representation.cisco_asa;

import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public class ProtocolObjectGroup extends ObjectGroup {

  private final List<ProtocolObjectGroupLine> _lines;

  public ProtocolObjectGroup(String name) {
    super(name);
    _lines = new LinkedList<>();
  }

  public List<ProtocolObjectGroupLine> getLines() {
    return _lines;
  }

  public AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ProtocolObjectGroup> protocolObjectGroups) {
    return or(
        _lines.stream()
            .map(line -> line.toAclLineMatchExpr(protocolObjectGroups))
            .collect(ImmutableSet.toImmutableSet()));
  }
}
