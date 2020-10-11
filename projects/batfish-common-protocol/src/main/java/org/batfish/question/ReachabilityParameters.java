package org.batfish.question;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
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

    private @Nonnull NodeSpecifier _finalNodesSpecifier = AllNodesNodeSpecifier.INSTANCE;

    private @Nonnull NodeSpecifier _forbiddenTransitNodesSpecifier = NoNodesNodeSpecifier.INSTANCE;

    private @Nonnull AclLineMatchExpr _headerSpace = AclLineMatchExprs.TRUE;

    private boolean _ignoreFilters = false;

    private boolean _invertSearch = false;

    private @Nonnull NodeSpecifier _requiredTransitNodesSpecifier = NoNodesNodeSpecifier.INSTANCE;

    private @Nonnull LocationSpecifier _sourceLocationSpecifier =
        AllInterfacesLocationSpecifier.INSTANCE;

    private @Nonnull IpSpaceSpecifier _sourceIpSpaceSpecifier =
        InferFromLocationIpSpaceSpecifier.INSTANCE;

    private @Nonnull SrcNattedConstraint _srcNatted = SrcNattedConstraint.UNCONSTRAINED;

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

    public Builder setFinalNodesSpecifier(@Nonnull NodeSpecifier finalNodeSpecifier) {
      _finalNodesSpecifier = finalNodeSpecifier;
      return this;
    }

    public Builder setForbiddenTransitNodesSpecifier(
        @Nonnull NodeSpecifier forbiddenTransitNodesSpecifier) {
      _forbiddenTransitNodesSpecifier = forbiddenTransitNodesSpecifier;
      return this;
    }

    public Builder setIgnoreFilters(boolean ignoreFilters) {
      _ignoreFilters = ignoreFilters;
      return this;
    }

    public Builder setInvertSearch(boolean invertSearch) {
      _invertSearch = invertSearch;
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

    public Builder setHeaderSpace(@Nonnull AclLineMatchExpr headerSpace) {
      _headerSpace = headerSpace;
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
  }

  private final SortedSet<FlowDisposition> _actions;

  private final @Nonnull IpSpaceSpecifier _destinationIpSpaceSpecifier;

  private final @Nonnull NodeSpecifier _finalNodesSpecifier;

  private final @Nonnull NodeSpecifier _forbiddenTransitNodesSpecifier;

  private final AclLineMatchExpr _headerSpace;

  private final boolean _ignoreFilters;

  private final boolean _invertSearch;

  private final LocationSpecifier _sourceLocationSpecifier;

  private final IpSpaceSpecifier _sourceIpSpaceSpecifier;

  private final SrcNattedConstraint _sourceNatted;

  private final @Nonnull NodeSpecifier _requiredTransitNodesSpecifier;

  private ReachabilityParameters(Builder builder) {
    _actions = builder._actions;
    _destinationIpSpaceSpecifier = builder._destinationIpSpaceSpecifier;
    _finalNodesSpecifier = builder._finalNodesSpecifier;
    _forbiddenTransitNodesSpecifier = builder._forbiddenTransitNodesSpecifier;
    _headerSpace = builder._headerSpace;
    _ignoreFilters = builder._ignoreFilters;
    _invertSearch = builder._invertSearch;
    _sourceLocationSpecifier = builder._sourceLocationSpecifier;
    _sourceIpSpaceSpecifier = builder._sourceIpSpaceSpecifier;
    _sourceNatted = builder._srcNatted;
    _requiredTransitNodesSpecifier = builder._requiredTransitNodesSpecifier;
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

  @Nonnull
  public NodeSpecifier getFinalNodesSpecifier() {
    return _finalNodesSpecifier;
  }

  @Nonnull
  public NodeSpecifier getForbiddenTransitNodesSpecifier() {
    return _forbiddenTransitNodesSpecifier;
  }

  public AclLineMatchExpr getHeaderSpace() {
    return _headerSpace;
  }

  public boolean getIgnoreFilters() {
    return _ignoreFilters;
  }

  public boolean getInvertSearch() {
    return _invertSearch;
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

  @Nonnull
  public NodeSpecifier getRequiredTransitNodesSpecifier() {
    return _requiredTransitNodesSpecifier;
  }

  /**
   * Filter a set of dispositions only to the ones reachability questions support.
   *
   * @throws IllegalArgumentException if the set of supported dispositions is empty.
   */
  @Nonnull
  public static Set<FlowDisposition> filterDispositions(Set<FlowDisposition> dispositions) {
    checkArgument(!dispositions.isEmpty(), "Invalid empty set of actions specified");
    return dispositions;
  }
}
