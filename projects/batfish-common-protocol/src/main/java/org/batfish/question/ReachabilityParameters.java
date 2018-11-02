package org.batfish.question;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;

/**
 * A set of parameters for reachability analysis that uses high-level specifiers. It is a good
 * choice for questions because relatively little work is needed to map user input into it.
 *
 * <p>Nullable parameters are interpreted as "no constraint". If a (Nonnull) constraint is supplied,
 * then it must be satisfiable. For example, if a no nodes match a {@link NodeSpecifier}, it is
 * unsatisfiable. This requirement is enforced during resolution.
 */
public final class ReachabilityParameters {

  public static class Builder {
    private @Nonnull SortedSet<FlowDisposition> _actions = ImmutableSortedSet.of();

    private @Nonnull IpSpaceSpecifier _destinationIpSpaceSpecifier =
        new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE);

    private @Nullable NodeSpecifier _finalNodesSpecifier = null;

    private @Nullable NodeSpecifier _forbiddenTransitNodesSpecifier = null;

    private @Nullable AclLineMatchExpr _headerSpace;

    private boolean _ignoreFilters = false;

    private int _maxChunkSize;

    private @Nullable NodeSpecifier _requiredTransitNodesSpecifier = null;

    private @Nonnull LocationSpecifier _sourceLocationSpecifier =
        AllInterfacesLocationSpecifier.INSTANCE;

    private @Nonnull IpSpaceSpecifier _sourceIpSpaceSpecifier =
        InferFromLocationIpSpaceSpecifier.INSTANCE;

    private @Nonnull SrcNattedConstraint _srcNatted = SrcNattedConstraint.UNCONSTRAINED;

    private boolean _specialize = true;

    private boolean _useCompression = false;

    public ReachabilityParameters build() {
      return new ReachabilityParameters(this);
    }

    public Builder setActions(SortedSet<FlowDisposition> actions) {
      _actions = ImmutableSortedSet.copyOf(actions);
      return this;
    }

    public Builder setDestinationIpSpaceSpecifier(
        @Nonnull IpSpaceSpecifier destinationIpSpaceSpecifier) {
      _destinationIpSpaceSpecifier = destinationIpSpaceSpecifier;
      return this;
    }

    public Builder setFinalNodesSpecifier(@Nullable NodeSpecifier finalNodeSpecifier) {
      _finalNodesSpecifier = finalNodeSpecifier;
      return this;
    }

    public Builder setForbiddenTransitNodesSpecifier(
        @Nullable NodeSpecifier forbiddenTransitNodesSpecifier) {
      _forbiddenTransitNodesSpecifier = forbiddenTransitNodesSpecifier;
      return this;
    }

    public Builder setSourceIpSpaceSpecifier(@Nonnull IpSpaceSpecifier sourceIpSpaceSpecifier) {
      _sourceIpSpaceSpecifier = sourceIpSpaceSpecifier;
      return this;
    }

    public Builder setSourceLocationSpecifier(@Nonnull LocationSpecifier sourceLocationSpecifier) {
      _sourceLocationSpecifier = sourceLocationSpecifier;
      return this;
    }

    public Builder setHeaderSpace(@Nonnull HeaderSpace headerSpace) {
      _headerSpace = new MatchHeaderSpace(headerSpace);
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

    public Builder setIgnoreFilters(boolean ignoreFilters) {
      _ignoreFilters = ignoreFilters;
      return this;
    }
  }

  private final SortedSet<FlowDisposition> _actions;

  private final @Nonnull IpSpaceSpecifier _destinationIpSpaceSpecifier;

  private final @Nullable NodeSpecifier _finalNodesSpecifier;

  private final @Nullable NodeSpecifier _forbiddenTransitNodesSpecifier;

  private final AclLineMatchExpr _headerSpace;

  private final boolean _ignoreFilters;

  private final int _maxChunkSize;

  private final LocationSpecifier _sourceLocationSpecifier;

  private final IpSpaceSpecifier _sourceIpSpaceSpecifier;

  private final SrcNattedConstraint _sourceNatted;

  private final boolean _specialize;

  private final @Nullable NodeSpecifier _requiredTransitNodesSpecifier;

  private final boolean _useCompression;

  private ReachabilityParameters(Builder builder) {
    _actions = builder._actions;
    _destinationIpSpaceSpecifier = builder._destinationIpSpaceSpecifier;
    _finalNodesSpecifier = builder._finalNodesSpecifier;
    _forbiddenTransitNodesSpecifier = builder._forbiddenTransitNodesSpecifier;
    _headerSpace = builder._headerSpace;
    _ignoreFilters = builder._ignoreFilters;
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

  public SortedSet<FlowDisposition> getActions() {
    return _actions;
  }

  @Nonnull
  public IpSpaceSpecifier getDestinationIpSpaceSpecifier() {
    return _destinationIpSpaceSpecifier;
  }

  @Nullable
  public NodeSpecifier getFinalNodesSpecifier() {
    return _finalNodesSpecifier;
  }

  @Nullable
  public NodeSpecifier getForbiddenTransitNodesSpecifier() {
    return _forbiddenTransitNodesSpecifier;
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

  @Nullable
  public NodeSpecifier getRequiredTransitNodesSpecifier() {
    return _requiredTransitNodesSpecifier;
  }

  public boolean getUseCompression() {
    return _useCompression;
  }

  /**
   * Filter a set of dispositions only to the ones reachability questions support.
   *
   * @throws IllegalArgumentException if the set of supported dispositions is empty.
   */
  @Nonnull
  public static Set<FlowDisposition> filterDispositions(Set<FlowDisposition> dispositions) {
    checkArgument(!dispositions.isEmpty(), "Invalid empty set of actions specified");
    Set<FlowDisposition> result =
        dispositions
            .stream()
            .filter(disposition -> FlowDisposition.LOOP != disposition)
            .collect(Collectors.toSet());
    checkArgument(!result.isEmpty(), "Unsupported set of actions specified: %s", dispositions);
    return result;
  }
}
