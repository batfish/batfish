package org.batfish.main;

import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;

public class ReachabilityParameters {

  public static class Builder {
    private SortedSet<ForwardingAction> _actions;

    private LocationSpecifier _destinationSpecifier;

    private NodeSpecifier _finalNodeSpecifier;

    private HeaderSpace _headerSpace;

    private int _maxChunkSize;

    private LocationSpecifier _sourceLocationSpecifier;

    private IpSpaceSpecifier _sourceIpSpaceSpecifier;

    private Boolean _sourceNatted;

    private boolean _specialize;

    private NodeSpecifier _transitNodes;

    private boolean _useCompression;

    public ReachabilityParameters build() {
      return new ReachabilityParameters(this);
    }

    public Builder setActions(SortedSet<ForwardingAction> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setDestinationSpecifier(LocationSpecifier destinationSpecifier) {
      _destinationSpecifier = destinationSpecifier;
      return this;
    }

    public Builder setSourceSpecifier(LocationSpecifier sourceLocationSpecifier) {
      _sourceLocationSpecifier = sourceLocationSpecifier;
      return this;
    }

    public Builder setFinalNodesSpecifier(NodeSpecifier finalNodeSpecifier) {
      _finalNodeSpecifier = finalNodeSpecifier;
      return this;
    }

    public Builder setSourceIpSpaceSpecifier(IpSpaceSpecifier sourceIpSpaceSpecifier) {
      _sourceIpSpaceSpecifier = sourceIpSpaceSpecifier;
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

    public Builder setTransitNodesSpecifier(NodeSpecifier transitNodes) {
      _transitNodes = transitNodes;
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

  private final LocationSpecifier _destinationSpecifier;

  private final NodeSpecifier _finalNodeSpecifier;

  private final HeaderSpace _headerSpace;

  private final int _maxChunkSize;

  private final LocationSpecifier _sourceLocationSpecifier;

  private final IpSpaceSpecifier _sourceIpSpaceSpecifier;

  private final Boolean _sourceNatted;

  private final boolean _specialize;

  private final NodeSpecifier _transitNodes;

  private final boolean _useCompression;

  private ReachabilityParameters(Builder builder) {
    _actions = builder._actions;
    _destinationSpecifier = builder._destinationSpecifier;
    _finalNodeSpecifier = builder._finalNodeSpecifier;
    _headerSpace = builder._headerSpace;
    _maxChunkSize = builder._maxChunkSize;
    _sourceLocationSpecifier = builder._sourceLocationSpecifier;
    _sourceIpSpaceSpecifier = builder._sourceIpSpaceSpecifier;
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

  public LocationSpecifier getDestinationSpecifier() {
    return _destinationSpecifier;
  }

  public NodeSpecifier getFinalNodesSpecifier() {
    return _finalNodeSpecifier;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public int getMaxChunkSize() {
    return _maxChunkSize;
  }

  public LocationSpecifier getSourceLocationSpecifier() {
    return _sourceLocationSpecifier;
  }

  public IpSpaceSpecifier getSourceIpSpaceSpecifier() {
    return _sourceIpSpaceSpecifier;
  }

  public Boolean getSourceNatted() {
    return _sourceNatted;
  }

  public boolean getSpecialize() {
    return _specialize;
  }

  public NodeSpecifier getTransitNodes() {
    return _transitNodes;
  }

  public boolean getUseCompression() {
    return _useCompression;
  }
}
