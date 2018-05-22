package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedList;
import java.util.List;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;

public class ServiceObjectGroup extends ObjectGroup {

  /** */
  private static final long serialVersionUID = 1L;

  private final List<ServiceObjectGroupLine> _lines;

  public ServiceObjectGroup(String name, int definitionLine) {
    super(name, definitionLine);
    _lines = new LinkedList<>();
  }

  public List<ServiceObjectGroupLine> getLines() {
    return _lines;
  }

  public AclLineMatchExpr toAclLineMatchExpr() {
    return new OrMatchExpr(
        _lines
            .stream()
            .map(ServiceObjectGroupLine::toAclLineMatchExpr)
            .collect(ImmutableSet.toImmutableSet()));
  }
}
