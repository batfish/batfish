package org.batfish.question.multipath;

import java.util.Set;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.specifier.IpSpaceAssignment;

/** Internal (resolved) parameters for multipath consistency question. */
public final class MultipathConsistencyParameters {
  private final AclLineMatchExpr _headerSpace;
  private final IpSpaceAssignment _srcIpSpaceAssignment;
  private final Set<String> _finalNodes;
  private final Set<String> _forbiddenTransitNodes;
  private final int _maxTraces;
  private final Set<String> _requiredTransitNodes;

  public MultipathConsistencyParameters(
      AclLineMatchExpr headerSpace,
      IpSpaceAssignment srcIpSpaceAssignment,
      Set<String> finalNodes,
      Set<String> forbiddenTransitNodes,
      int maxTraces,
      Set<String> requiredTransitNodes) {
    _headerSpace = headerSpace;
    _srcIpSpaceAssignment = srcIpSpaceAssignment;
    _finalNodes = finalNodes;
    _forbiddenTransitNodes = forbiddenTransitNodes;
    _maxTraces = maxTraces;
    _requiredTransitNodes = requiredTransitNodes;
  }

  public AclLineMatchExpr getHeaderSpace() {
    return _headerSpace;
  }

  public IpSpaceAssignment getSrcIpSpaceAssignment() {
    return _srcIpSpaceAssignment;
  }

  public Set<String> getFinalNodes() {
    return _finalNodes;
  }

  public Set<String> getForbiddenTransitNodes() {
    return _forbiddenTransitNodes;
  }

  public int getMaxTraces() {
    return _maxTraces;
  }

  public Set<String> getRequiredTransitNodes() {
    return _requiredTransitNodes;
  }
}
