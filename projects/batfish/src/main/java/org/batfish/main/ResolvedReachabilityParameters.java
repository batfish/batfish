package org.batfish.main;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.specifier.Location;

public class ResolvedReachabilityParameters {

  public static class Builder {
    private SortedSet<ForwardingAction> _actions;

    private Map<String, Configuration> _configurations;

    private DataPlane _dataPlane;

    private Set<String> _finalNodes;

    private Set<String> _forbiddenTransitNodes;

    private HeaderSpace _headerSpace;

    private int _maxChunkSize;

    private Multimap<IpSpace, Location> _sourceLocationsByIpSpace;

    private SrcNattedConstraint _srcNatted;

    private boolean _specialize;

    private Set<String> _requiredTransitNodes;

    private boolean _useCompression;

    public ResolvedReachabilityParameters build() {
      return new ResolvedReachabilityParameters(this);
    }

    public Builder setActions(SortedSet<ForwardingAction> actions) {
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

    public Builder setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
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

    public Builder setSourceLocationsByIpSpace(
        Multimap<IpSpace, Location> sourceLocationsByIpSpace) {
      _sourceLocationsByIpSpace = ImmutableMultimap.copyOf(sourceLocationsByIpSpace);
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

    public Builder setUseCompression(boolean useCompression) {
      _useCompression = useCompression;
      return this;
    }
  }

  private final SortedSet<ForwardingAction> _actions;

  private final Map<String, Configuration> _configurations;

  private final DataPlane _dataPlane;

  private final Set<String> _finalNodes;

  private Set<String> _forbiddenTransitNodes;

  private final HeaderSpace _headerSpace;

  private final int _maxChunkSize;

  private final Multimap<IpSpace, Location> _sourceIpSpaceByLocations;

  private final SrcNattedConstraint _srcNatted;

  private final boolean _specialize;

  private final Set<String> _transitNodes;

  private final boolean _useCompression;

  private ResolvedReachabilityParameters(Builder builder) {
    _actions = builder._actions;
    _configurations = builder._configurations;
    _dataPlane = builder._dataPlane;
    _finalNodes = builder._finalNodes;
    _forbiddenTransitNodes = builder._forbiddenTransitNodes;
    _headerSpace = builder._headerSpace;
    _maxChunkSize = builder._maxChunkSize;
    _sourceIpSpaceByLocations = builder._sourceLocationsByIpSpace;
    _srcNatted = builder._srcNatted;
    _specialize = builder._specialize;
    _transitNodes = builder._requiredTransitNodes;
    _useCompression = builder._useCompression;
  }

  public static Builder builder() {
    return new Builder();
  }

  public SortedSet<ForwardingAction> getActions() {
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

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public int getMaxChunkSize() {
    return _maxChunkSize;
  }

  public Multimap<IpSpace, Location> getSourceLocationsByIpSpace() {
    return _sourceIpSpaceByLocations;
  }

  public SrcNattedConstraint getSrcNatted() {
    return _srcNatted;
  }

  public boolean getSpecialize() {
    return _specialize;
  }

  public Set<String> getRequiredTransitNodes() {
    return _transitNodes;
  }

  public boolean getUseCompression() {
    return _useCompression;
  }
}
