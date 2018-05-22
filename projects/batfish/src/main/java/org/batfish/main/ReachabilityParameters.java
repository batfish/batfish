package org.batfish.main;

import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NameRegexVrfLocationSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;

public class ReachabilityParameters {

  public static class Builder {
    private @Nonnull SortedSet<ForwardingAction> _actions = ImmutableSortedSet.of();

    private @Nonnull NodeSpecifier _finalNodesSpecifier = NoNodesNodeSpecifier.INSTANCE;

    private @Nonnull NodeSpecifier _forbiddenTransitNodesSpecifier = NoNodesNodeSpecifier.INSTANCE;

    private HeaderSpace _headerSpace;

    private int _maxChunkSize;

    private @Nonnull NodeSpecifier _requiredTransitNodesSpecifier = NoNodesNodeSpecifier.INSTANCE;

    private @Nonnull LocationSpecifier _sourceLocationSpecifier =
        NameRegexVrfLocationSpecifier.ALL_VRFS;

    private @Nonnull IpSpaceSpecifier _sourceIpSpaceSpecifier =
        InferFromLocationIpSpaceSpecifier.INSTANCE;

    private @Nonnull SrcNattedConstraint _srcNatted = SrcNattedConstraint.UNCONSTRAINED;

    private boolean _specialize = false;

    private boolean _useCompression = false;

    public ReachabilityParameters build() {
      return new ReachabilityParameters(this);
    }

    public Builder setActions(SortedSet<ForwardingAction> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setSourceSpecifier(LocationSpecifier sourceLocationSpecifier) {
      _sourceLocationSpecifier = sourceLocationSpecifier;
      return this;
    }

    public Builder setFinalNodesSpecifier(NodeSpecifier finalNodeSpecifier) {
      _finalNodesSpecifier = finalNodeSpecifier;
      return this;
    }

    public Builder setForbiddenTransitNodesSpecifier(
        @Nonnull NodeSpecifier forbiddenTransitNodesSpecifier) {
      _forbiddenTransitNodesSpecifier = forbiddenTransitNodesSpecifier;
      return this;
    }

    public Builder setSourceIpSpaceSpecifier(IpSpaceSpecifier sourceIpSpaceSpecifier) {
      _sourceIpSpaceSpecifier = sourceIpSpaceSpecifier;
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

    public Builder setSrcNatted(SrcNattedConstraint srcNatted) {
      _srcNatted = srcNatted;
      return this;
    }

    public Builder setRequiredTransitNodesSpecifier(NodeSpecifier transitNodesSpecifier) {
      _requiredTransitNodesSpecifier = transitNodesSpecifier;
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

  private final NodeSpecifier _finalNodesSpecifier;

  private final @Nonnull NodeSpecifier _forbiddenTransitNodesSpecifier;

  private final HeaderSpace _headerSpace;

  private final int _maxChunkSize;

  private final LocationSpecifier _sourceLocationSpecifier;

  private final IpSpaceSpecifier _sourceIpSpaceSpecifier;

  private final SrcNattedConstraint _sourceNatted;

  private final boolean _specialize;

  private final NodeSpecifier _requiredTransitNodesSpecifier;

  private final boolean _useCompression;

  private ReachabilityParameters(Builder builder) {
    _actions = builder._actions;
    _finalNodesSpecifier = builder._finalNodesSpecifier;
    _forbiddenTransitNodesSpecifier = builder._forbiddenTransitNodesSpecifier;
    _headerSpace = builder._headerSpace;
    _maxChunkSize = builder._maxChunkSize;
    _sourceLocationSpecifier = builder._sourceLocationSpecifier;
    _sourceIpSpaceSpecifier = builder._sourceIpSpaceSpecifier;
    _sourceNatted = builder._srcNatted;
    _specialize = builder._specialize;
    _requiredTransitNodesSpecifier = builder._requiredTransitNodesSpecifier;
    _useCompression = builder._useCompression;
  }

  public static Builder builder() {
    return new Builder();
  }

  public SortedSet<ForwardingAction> getActions() {
    return _actions;
  }

  public NodeSpecifier getFinalNodesSpecifier() {
    return _finalNodesSpecifier;
  }

  public NodeSpecifier getForbiddenTransitNodesSpecifier() {
    return _forbiddenTransitNodesSpecifier;
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

  public SrcNattedConstraint getSrcNatted() {
    return _sourceNatted;
  }

  public boolean getSpecialize() {
    return _specialize;
  }

  public NodeSpecifier getRequiredTransitNodesSpecifier() {
    return _requiredTransitNodesSpecifier;
  }

  public boolean getUseCompression() {
    return _useCompression;
  }
}
