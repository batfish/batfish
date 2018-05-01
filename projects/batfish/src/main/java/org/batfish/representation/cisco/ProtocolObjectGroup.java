package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.DefinedStructure;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;

public class ProtocolObjectGroup extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private final List<ProtocolObjectGroupLine> _lines;

  public ProtocolObjectGroup(String name, int definitionLine) {
    super(name, definitionLine);
    _lines = new LinkedList<>();
  }

  public List<ProtocolObjectGroupLine> getLines() {
    return _lines;
  }

  public AclLineMatchExpr toAclLineMatchExpr() {
    return new OrMatchExpr(
        _lines
            .stream()
            .map(ProtocolObjectGroupLine::toAclLineMatchExpr)
            .collect(ImmutableSet.toImmutableSet()));
  }
}
