package org.batfish.question.specifiers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.specifiers.PathConstraintsUtil.createPathConstraints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.TracePruner;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PacketHeaderConstraintsUtil;
import org.batfish.datamodel.PathConstraints;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ReachabilityParameters;
import org.batfish.specifier.ConstantIpSpaceAssignmentSpecifier;
import org.batfish.specifier.DispositionSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceAssignmentSpecifier;
import org.batfish.specifier.IpSpaceAssignmentSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A version of reachability question that supports {@link LocationSpecifier location} and {@link
 * IpSpaceAssignmentSpecifier ipSpace} specifiers.
 */
public final class SpecifiersReachabilityQuestion extends Question {
  private static final String PROP_ACTIONS = "actions";
  private static final String PROP_HEADER_CONSTRAINT = "headers";
  private static final String PROP_IGNORE_FILTERS = "ignoreFilters";
  private static final String PROP_INVERT_SEARCH = "invertSearch";
  private static final String PROP_MAX_TRACES = "maxTraces";
  private static final String PROP_PATH_CONSTRAINT = "pathConstraints";

  private final @Nonnull DispositionSpecifier _actions;
  private final @Nonnull PacketHeaderConstraints _headerConstraints;
  private final boolean _ignoreFilters;
  private final boolean _invertSearch;
  private final int _maxTraces;
  private final @Nonnull PathConstraintsInput _pathConstraints;

  /**
   * Create a new reachability question. {@code null} values result in default parameter values.
   *
   * @param actions set of actions/flow dispositions to search for (default is {@code success})
   * @param headerConstraints header constraints that constrain the search space of valid flows.
   *     Default is unconstrained.
   * @param ignoreFilters whether to ignore ingress and egress ACLs.
   * @param pathConstraints path constraints dictating where a flow can originate/terminate/transit.
   *     Default is unconstrained.
   */
  @JsonCreator
  public SpecifiersReachabilityQuestion(
      @JsonProperty(PROP_ACTIONS) @Nullable DispositionSpecifier actions,
      @JsonProperty(PROP_HEADER_CONSTRAINT) @Nullable PacketHeaderConstraints headerConstraints,
      @JsonProperty(PROP_IGNORE_FILTERS) @Nullable Boolean ignoreFilters,
      @JsonProperty(PROP_INVERT_SEARCH) @Nullable Boolean invertSearch,
      @JsonProperty(PROP_MAX_TRACES) @Nullable Integer maxTraces,
      @JsonProperty(PROP_PATH_CONSTRAINT) @Nullable PathConstraintsInput pathConstraints) {
    _actions = firstNonNull(actions, DispositionSpecifier.SUCCESS_SPECIFIER);
    _headerConstraints = firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained());
    _ignoreFilters = firstNonNull(ignoreFilters, false);
    _invertSearch = firstNonNull(invertSearch, false);
    _maxTraces = firstNonNull(maxTraces, TracePruner.DEFAULT_MAX_TRACES);
    _pathConstraints = firstNonNull(pathConstraints, PathConstraintsInput.unconstrained());
  }

  SpecifiersReachabilityQuestion() {
    this(null, null, null, null, null, null);
  }

  @JsonProperty(PROP_ACTIONS)
  public @Nonnull DispositionSpecifier getActions() {
    return _actions;
  }

  @JsonProperty(PROP_HEADER_CONSTRAINT)
  public @Nonnull PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @JsonProperty(PROP_IGNORE_FILTERS)
  public boolean getIgnoreFilters() {
    return _ignoreFilters;
  }

  @JsonProperty(PROP_INVERT_SEARCH)
  public boolean getInvertSearch() {
    return _invertSearch;
  }

  @JsonProperty(PROP_MAX_TRACES)
  public int getMaxTraces() {
    return _maxTraces;
  }

  @JsonProperty(PROP_PATH_CONSTRAINT)
  private @Nonnull PathConstraintsInput getPathConstraintsInput() {
    return _pathConstraints;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @VisibleForTesting
  AclLineMatchExpr getHeaderSpace() {
    return PacketHeaderConstraintsUtil.toAclLineMatchExpr(getHeaderConstraints());
  }

  @VisibleForTesting
  PathConstraints getPathConstraints() {
    return createPathConstraints(_pathConstraints);
  }

  @Override
  public String getName() {
    return "specifiersReachability";
  }

  private IpSpaceAssignmentSpecifier getDestinationIpSpaceSpecifier() {
    return SpecifierFactories.getIpSpaceAssignmentSpecifierOrDefault(
        _headerConstraints.getDstIps(),
        new ConstantIpSpaceAssignmentSpecifier(UniverseIpSpace.INSTANCE));
  }

  private IpSpaceAssignmentSpecifier getSourceIpSpaceSpecifier() {
    return SpecifierFactories.getIpSpaceAssignmentSpecifierOrDefault(
        _headerConstraints.getSrcIps(), InferFromLocationIpSpaceAssignmentSpecifier.INSTANCE);
  }

  ReachabilityParameters getReachabilityParameters() {
    PathConstraints pathConstraints = getPathConstraints();

    return ReachabilityParameters.builder()
        .setActions(
            ImmutableSortedSet.copyOf(
                ReachabilityParameters.filterDispositions(getActions().getDispositions())))
        .setDestinationIpSpaceSpecifier(getDestinationIpSpaceSpecifier())
        .setFinalNodesSpecifier(pathConstraints.getEndLocation())
        .setForbiddenTransitNodesSpecifier(pathConstraints.getForbiddenLocations())
        .setHeaderSpace(getHeaderSpace())
        .setIgnoreFilters(getIgnoreFilters())
        .setInvertSearch(getInvertSearch())
        .setRequiredTransitNodesSpecifier(pathConstraints.getTransitLocations())
        .setSourceLocationSpecifier(pathConstraints.getStartLocation())
        .setSourceIpSpaceSpecifier(getSourceIpSpaceSpecifier())
        .build();
  }

  @VisibleForTesting
  static Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  static final class Builder {
    private DispositionSpecifier _actions;
    private PacketHeaderConstraints _headerConstraints;
    private Boolean _ignoreFilters;
    private Boolean _invertSearch;
    private Integer _maxTraces;
    private PathConstraintsInput _pathConstraints;

    private Builder() {}

    public Builder setActions(DispositionSpecifier actions) {
      _actions = actions;
      return this;
    }

    public Builder setHeaderConstraints(PacketHeaderConstraints headerConstraints) {
      _headerConstraints = headerConstraints;
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

    public Builder setMaxTraces(Integer maxTraces) {
      _maxTraces = maxTraces;
      return this;
    }

    public Builder setPathConstraints(PathConstraintsInput pathConstraintsInput) {
      _pathConstraints = pathConstraintsInput;
      return this;
    }

    public SpecifiersReachabilityQuestion build() {
      return new SpecifiersReachabilityQuestion(
          _actions,
          _headerConstraints,
          _ignoreFilters,
          _invertSearch,
          _maxTraces,
          _pathConstraints);
    }
  }
}
