package org.batfish.question;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.questions.InvalidReachabilityParametersException;
import org.batfish.specifier.IpSpaceAssignment;

/**
 * A set of parameters for reachability that are fully resolved, meaning everything is concrete.
 * There are no specifiers, etc.
 */
public final class ResolvedReachabilityParameters {

  public static class Builder {
    private SortedSet<FlowDisposition> _actions;

    private Map<String, Configuration> _configurations;

    private DataPlane _dataPlane;

    private Set<String> _finalNodes;

    private Set<String> _forbiddenTransitNodes;

    private AclLineMatchExpr _headerSpace;

    private boolean _ignoreFilters;

    private int _maxChunkSize;

    private IpSpaceAssignment _sourceIpSpaceAssignment;

    private SrcNattedConstraint _srcNatted;

    private boolean _specialize;

    private Set<String> _requiredTransitNodes;

    public ResolvedReachabilityParameters build() throws InvalidReachabilityParametersException {
      return new ResolvedReachabilityParameters(this);
    }

    public Builder setActions(SortedSet<FlowDisposition> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setConfigurations(Map<String, Configuration> configurations) {
      _configurations = ImmutableMap.copyOf(configurations);
      return this;
    }

    public Builder setDataPlane(DataPlane dataPlane) {
      _dataPlane = dataPlane;
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

    public Builder setMaxChunkSize(int maxChunkSize) {
      _maxChunkSize = maxChunkSize;
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

    public Builder setSpecialize(boolean specialize) {
      _specialize = specialize;
      return this;
    }
  }

  private final SortedSet<FlowDisposition> _actions;

  private final Map<String, Configuration> _configurations;

  private final DataPlane _dataPlane;

  private final Set<String> _finalNodes;

  private Set<String> _forbiddenTransitNodes;

  private final AclLineMatchExpr _headerSpace;

  private boolean _ignoreFilters;

  private final int _maxChunkSize;

  private final IpSpaceAssignment _sourceIpSpaceByLocations;

  private final SrcNattedConstraint _srcNatted;

  private final boolean _specialize;

  private final Set<String> _requiredTransitNodes;

  private ResolvedReachabilityParameters(Builder builder) {
    _actions = builder._actions;
    _configurations = builder._configurations;
    _dataPlane = builder._dataPlane;
    _finalNodes = builder._finalNodes;
    _forbiddenTransitNodes = builder._forbiddenTransitNodes;
    _headerSpace = builder._headerSpace;
    _ignoreFilters = builder._ignoreFilters;
    _maxChunkSize = builder._maxChunkSize;
    _sourceIpSpaceByLocations = builder._sourceIpSpaceAssignment;
    _srcNatted = builder._srcNatted;
    _specialize = builder._specialize;
    _requiredTransitNodes = builder._requiredTransitNodes;
  }

  public static Builder builder() {
    return new Builder();
  }

  public SortedSet<FlowDisposition> getActions() {
    return _actions;
  }

  public Map<String, Configuration> getConfigurations() {
    return _configurations;
  }

  public DataPlane getDataPlane() {
    return _dataPlane;
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

  public int getMaxChunkSize() {
    return _maxChunkSize;
  }

  public IpSpaceAssignment getSourceIpAssignment() {
    return _sourceIpSpaceByLocations;
  }

  public SrcNattedConstraint getSrcNatted() {
    return _srcNatted;
  }

  public boolean getSpecialize() {
    return _specialize;
  }

  public Set<String> getRequiredTransitNodes() {
    return _requiredTransitNodes;
  }
}
