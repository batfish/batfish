package org.batfish.question;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.specifier.IpSpaceAssignment;

/**
 * A set of parameters for reachability that are fully resolved, meaning everything is concrete.
 * There are no specifiers, etc.
 */
public final class ResolvedReachabilityParameters {

  public static class Builder {
    private SortedSet<FlowDisposition> _actions;
    private Set<String> _finalNodes;
    private Set<String> _forbiddenTransitNodes;
    private AclLineMatchExpr _headerSpace;
    private boolean _ignoreFilters;
    private IpSpaceAssignment _sourceIpSpaceAssignment;
    private SrcNattedConstraint _srcNatted;
    private Set<String> _requiredTransitNodes;

    public ResolvedReachabilityParameters build() {
      return new ResolvedReachabilityParameters(this);
    }

    public Builder setActions(SortedSet<FlowDisposition> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setFinalNodes(Set<String> finalNodes) {
      _finalNodes = ImmutableSet.copyOf(finalNodes);
      return this;
    }

    public Builder setForbiddenTransitNodes(Set<String> nodes) {
      _forbiddenTransitNodes = ImmutableSet.copyOf(nodes);
      return this;
    }

    public Builder setHeaderSpace(AclLineMatchExpr headerSpace) {
      _headerSpace = headerSpace;
      return this;
    }

    public Builder setIgnoreFilters(boolean ignoreFilters) {
      _ignoreFilters = ignoreFilters;
      return this;
    }

    public Builder setRequiredTransitNodes(Set<String> nodes) {
      _requiredTransitNodes = ImmutableSet.copyOf(nodes);
      return this;
    }

    public Builder setSourceIpSpaceAssignment(IpSpaceAssignment sourceIpSpaceAssignment) {
      _sourceIpSpaceAssignment = sourceIpSpaceAssignment;
      return this;
    }

    public Builder setSrcNatted(SrcNattedConstraint srcNatted) {
      _srcNatted = srcNatted;
      return this;
    }
  }

  private final SortedSet<FlowDisposition> _actions;
  private final Set<String> _finalNodes;
  private Set<String> _forbiddenTransitNodes;
  private final AclLineMatchExpr _headerSpace;
  private boolean _ignoreFilters;
  private final IpSpaceAssignment _sourceIpSpaceByLocations;
  private final SrcNattedConstraint _srcNatted;
  private final Set<String> _requiredTransitNodes;

  private ResolvedReachabilityParameters(Builder builder) {
    _actions = builder._actions;
    _finalNodes = builder._finalNodes;
    _forbiddenTransitNodes = builder._forbiddenTransitNodes;
    _headerSpace = builder._headerSpace;
    _ignoreFilters = builder._ignoreFilters;
    _sourceIpSpaceByLocations = builder._sourceIpSpaceAssignment;
    _srcNatted = builder._srcNatted;
    _requiredTransitNodes = builder._requiredTransitNodes;
  }

  public static Builder builder() {
    return new Builder();
  }

  public SortedSet<FlowDisposition> getActions() {
    return _actions;
  }

  public Set<String> getFinalNodes() {
    return _finalNodes;
  }

  public Set<String> getForbiddenTransitNodes() {
    return _forbiddenTransitNodes;
  }

  public AclLineMatchExpr getHeaderSpace() {
    return _headerSpace;
  }

  public boolean getIgnoreFilters() {
    return _ignoreFilters;
  }

  public IpSpaceAssignment getSourceIpAssignment() {
    return _sourceIpSpaceByLocations;
  }

  public SrcNattedConstraint getSrcNatted() {
    return _srcNatted;
  }

  public Set<String> getRequiredTransitNodes() {
    return _requiredTransitNodes;
  }
}
