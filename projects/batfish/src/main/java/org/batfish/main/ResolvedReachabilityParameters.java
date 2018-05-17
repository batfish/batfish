package org.batfish.main;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.specifier.Location;

public class ResolvedReachabilityParameters {

  public static class Builder {
    private SortedSet<ForwardingAction> _actions;

    private Set<String> _finalNodes;

    private HeaderSpace _headerSpace;

    private int _maxChunkSize;

    private Map<Set<Location>, IpSpace> _sourceIpSpaceByLocations;

    private Boolean _sourceNatted;

    private boolean _specialize;

    private Set<String> _transitNodes;

    private boolean _useCompression;

    public ResolvedReachabilityParameters build() {
      return new ResolvedReachabilityParameters(this);
    }

    public Builder setActions(SortedSet<ForwardingAction> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setSourceIpSpaceByLocations(
        Map<Set<Location>, IpSpace> sourceIpSpaceByLocations) {
      _sourceIpSpaceByLocations = ImmutableMap.copyOf(sourceIpSpaceByLocations);
      return this;
    }

    public Builder setFinalNodes(Set<String> finalNodes) {
      _finalNodes = ImmutableSet.copyOf(finalNodes);
      return this;
    }

    public Builder setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
      return null;
    }

    public Builder setMaxChunkSize(int maxChunkSize) {
      _maxChunkSize = maxChunkSize;
      return this;
    }

    public Builder setSourceNatted(Boolean sourceNatted) {
      _sourceNatted = sourceNatted;
      return this;
    }

    public Builder setTransitNodes(Set<String> transitNodes) {
      _transitNodes = ImmutableSet.copyOf(transitNodes);
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

  private final Set<String> _finalNodes;

  private final HeaderSpace _headerSpace;

  private final int _maxChunkSize;

  private final Map<Set<Location>, IpSpace> _sourceIpSpaceByLocations;

  private final Boolean _sourceNatted;

  private final boolean _specialize;

  private final Set<String> _transitNodes;

  private final boolean _useCompression;

  private ResolvedReachabilityParameters(Builder builder) {
    _actions = builder._actions;
    _finalNodes = builder._finalNodes;
    _headerSpace = builder._headerSpace;
    _maxChunkSize = builder._maxChunkSize;
    _sourceIpSpaceByLocations = builder._sourceIpSpaceByLocations;
    _sourceNatted = builder._sourceNatted;
    _specialize = builder._specialize;
    _transitNodes = builder._transitNodes;
    _useCompression = builder._useCompression;
  }

  public static Builder builder() {
    return new Builder();
  }

  public SortedSet<ForwardingAction> getActions() {
    return _actions;
  }

  public Set<String> getFinalNodes() {
    return _finalNodes;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public int getMaxChunkSize() {
    return _maxChunkSize;
  }

  public Map<Set<Location>, IpSpace> getSourceIpSpaceByLocations() {
    return _sourceIpSpaceByLocations;
  }

  public Boolean getSourceNatted() {
    return _sourceNatted;
  }

  public boolean getSpecialize() {
    return _specialize;
  }

  public Set<String> getTransitNodes() {
    return _transitNodes;
  }

  public boolean getUseCompression() {
    return _useCompression;
  }
}
