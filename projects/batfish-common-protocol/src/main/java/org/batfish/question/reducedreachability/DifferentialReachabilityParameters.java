package org.batfish.question.reducedreachability;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.specifier.IpSpaceAssignment;

/** Parameters for differential reachability analysis */
public class DifferentialReachabilityParameters {
  private final Set<FlowDisposition> _flowDispositions;
  private final Set<String> _forbiddenTransitNodes;
  private final Set<String> _finalNodes;
  private final AclLineMatchExpr _headerSpace;
  private final IpSpaceAssignment _ipSpaceAssignment;
  private final Set<String> _requiredTransitNodes;

  public DifferentialReachabilityParameters(
      Set<FlowDisposition> flowDispositions,
      Set<String> forbiddenTransitNodes,
      Set<String> finalNodes,
      AclLineMatchExpr headerSpace,
      IpSpaceAssignment ipSpaceAssignment,
      Set<String> requiredTransitNodes) {
    _flowDispositions = ImmutableSet.copyOf(flowDispositions);
    _forbiddenTransitNodes = ImmutableSet.copyOf(forbiddenTransitNodes);
    _finalNodes = ImmutableSet.copyOf(finalNodes);
    _headerSpace = headerSpace;
    _ipSpaceAssignment = ipSpaceAssignment;
    _requiredTransitNodes = ImmutableSet.copyOf(requiredTransitNodes);
  }

  public Set<FlowDisposition> getFlowDispositions() {
    return _flowDispositions;
  }

  public Set<String> getForbiddenTransitNodes() {
    return _forbiddenTransitNodes;
  }

  public Set<String> getFinalNodes() {
    return _finalNodes;
  }

  public AclLineMatchExpr getHeaderSpace() {
    return _headerSpace;
  }

  public IpSpaceAssignment getIpSpaceAssignment() {
    return _ipSpaceAssignment;
  }

  public Set<String> getRequiredTransitNodes() {
    return _requiredTransitNodes;
  }
}
