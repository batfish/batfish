package org.batfish.question.specifiers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.PathConstraints;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ReachabilityParameters;
import org.batfish.specifier.FlexibleInferFromLocationIpSpaceSpecifierFactory;
import org.batfish.specifier.FlexibleLocationSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.FlexibleUniverseIpSpaceSpecifierFactory;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationSpecifierFactory;
import org.batfish.specifier.NodeSpecifierFactory;

/**
 * A version of reachability question that supports {@link LocationSpecifier location} and {@link
 * IpSpaceSpecifier ipSpace} specifiers.
 */
public final class SpecifiersReachabilityQuestion extends Question {
  private static final String PROP_ACTIONS = "actions";
  private static final String PROP_HEADER_CONSTRAINT = "headers";
  private static final String PROP_PATH_CONSTRAINT = "pathConstraints";

  private static final LocationSpecifierFactory LOCATION_SPECIFIER_FACTORY =
      LocationSpecifierFactory.load(FlexibleLocationSpecifierFactory.NAME);
  private static final NodeSpecifierFactory NODE_SPECIFIER_FACTORY =
      NodeSpecifierFactory.load(FlexibleNodeSpecifierFactory.NAME);

  @Nonnull private final DispositionSpecifier _actions;
  @Nonnull private final PacketHeaderConstraints _headerConstraints;
  @Nonnull private final PathConstraintsInput _pathConstraints;

  /**
   * Create a new reachability question. {@code null} values result in default parameter values.
   *
   * @param actions set of actions/flow dispositions to search for (default is {@code success})
   * @param headerConstraints header constraints that constrain the search space of valid flows.
   *     Default is unconstrained.
   * @param pathConstraints path constraints dictating where a flow can originate/terminate/transit.
   *     Default is unconstrained.
   */
  @JsonCreator
  public SpecifiersReachabilityQuestion(
      @Nullable @JsonProperty(PROP_ACTIONS) DispositionSpecifier actions,
      @Nullable @JsonProperty(PROP_HEADER_CONSTRAINT) PacketHeaderConstraints headerConstraints,
      @Nullable @JsonProperty(PROP_PATH_CONSTRAINT) PathConstraintsInput pathConstraints) {
    _actions = firstNonNull(actions, DispositionSpecifier.SUCCESS_SPECIFIER);
    _headerConstraints = firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained());
    _pathConstraints = firstNonNull(pathConstraints, PathConstraintsInput.unconstrained());
  }

  SpecifiersReachabilityQuestion() {
    this(
        DispositionSpecifier.SUCCESS_SPECIFIER,
        PacketHeaderConstraints.unconstrained(),
        PathConstraintsInput.unconstrained());
  }

  @Nonnull
  @JsonProperty(PROP_ACTIONS)
  public DispositionSpecifier getActions() {
    return _actions;
  }

  @Nonnull
  @JsonProperty(PROP_HEADER_CONSTRAINT)
  public PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @JsonProperty(PROP_PATH_CONSTRAINT)
  @Nonnull
  private PathConstraintsInput getPathConstraintsInput() {
    return _pathConstraints;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  public static HeaderSpace toHeaderSpace(PacketHeaderConstraints headerConstraints) {
    // Note: headerspace builder does not accept nulls, so we have to convert nulls to empty sets
    HeaderSpace.Builder builder =
        HeaderSpace.builder()
            .setIpProtocols(
                firstNonNull(headerConstraints.resolveIpProtocols(), ImmutableSortedSet.of()))
            .setSrcPorts(firstNonNull(headerConstraints.getSrcPorts(), ImmutableSortedSet.of()))
            .setDstPorts(firstNonNull(headerConstraints.resolveDstPorts(), ImmutableSortedSet.of()))
            .setIcmpCodes(firstNonNull(headerConstraints.getIcmpCodes(), ImmutableSortedSet.of()))
            .setIcmpTypes(firstNonNull(headerConstraints.getIcmpTypes(), ImmutableSortedSet.of()))
            .setDstProtocols(
                firstNonNull(headerConstraints.getApplications(), ImmutableSortedSet.of()));

    if (headerConstraints.getDscps() != null) {
      builder.setDscps(
          ImmutableSortedSet.copyOf(
              headerConstraints.getDscps().stream().flatMapToInt(SubRange::asStream).iterator()));
    }
    if (headerConstraints.getEcns() != null) {
      builder.setEcns(
          ImmutableSortedSet.copyOf(
              headerConstraints.getEcns().stream().flatMapToInt(SubRange::asStream).iterator()));
    }
    return builder.build();
  }

  @VisibleForTesting
  HeaderSpace getHeaderSpace() {
    return toHeaderSpace(getHeaderConstraints());
  }

  @VisibleForTesting
  PathConstraints getPathConstraints() {
    PathConstraints.Builder builder =
        PathConstraints.builder()
            .withStartLocation(
                LOCATION_SPECIFIER_FACTORY.buildLocationSpecifier(
                    _pathConstraints.getStartLocation()))
            .withEndLocation(
                NODE_SPECIFIER_FACTORY.buildNodeSpecifier(_pathConstraints.getEndLocation()));
    /*
     * Explicit check for null, because null expands into ALL nodes, which is usually not the
     * desired behavior for waypointing constraints
     */

    if (_pathConstraints.getTransitLocations() != null) {
      builder.through(
          NODE_SPECIFIER_FACTORY.buildNodeSpecifier(_pathConstraints.getTransitLocations()));
    }
    if (_pathConstraints.getForbiddenLocations() != null) {
      builder.avoid(
          NODE_SPECIFIER_FACTORY.buildNodeSpecifier(_pathConstraints.getForbiddenLocations()));
    }
    return builder.build();
  }

  @Override
  public String getName() {
    return "specifiersReachability";
  }

  private IpSpaceSpecifier getDestinationIpSpaceSpecifier() {
    return IpSpaceSpecifierFactory.load(FlexibleUniverseIpSpaceSpecifierFactory.NAME)
        .buildIpSpaceSpecifier(_headerConstraints.getDstIps());
  }

  private IpSpaceSpecifier getSourceIpSpaceSpecifier() {
    return IpSpaceSpecifierFactory.load(FlexibleInferFromLocationIpSpaceSpecifierFactory.NAME)
        .buildIpSpaceSpecifier(_headerConstraints.getSrcIps());
  }

  ReachabilityParameters getReachabilityParameters() {
    PathConstraints pathConstraints = getPathConstraints();

    Set<FlowDisposition> actions =
        getActions()
            .getDispositions()
            .stream()
            .filter(disposition -> FlowDisposition.LOOP != disposition)
            .collect(Collectors.toSet());
    checkArgument(!actions.isEmpty());
    return ReachabilityParameters.builder()
        .setActions(ImmutableSortedSet.copyOf(actions))
        .setDestinationIpSpaceSpecifier(getDestinationIpSpaceSpecifier())
        .setFinalNodesSpecifier(pathConstraints.getEndLocation())
        .setForbiddenTransitNodesSpecifier(pathConstraints.getForbiddenLocations())
        .setHeaderSpace(getHeaderSpace())
        .setRequiredTransitNodesSpecifier(pathConstraints.getTransitLocations())
        .setSourceLocationSpecifier(pathConstraints.getStartLocation())
        .setSourceIpSpaceSpecifier(getSourceIpSpaceSpecifier())
        .setSpecialize(true)
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

    public Builder setPathConstraints(PathConstraintsInput pathConstraintsInput) {
      _pathConstraints = pathConstraintsInput;
      return this;
    }

    public SpecifiersReachabilityQuestion build() {
      return new SpecifiersReachabilityQuestion(_actions, _headerConstraints, _pathConstraints);
    }
  }
}
