package org.batfish.question.differentialreachability;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;
import static org.batfish.specifier.DispositionSpecifier.SUCCESS_SPECIFIER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.specifiers.PathConstraintsInput;
import org.batfish.specifier.DispositionSpecifier;

/** A zero-input question to check for reduced reachability between base and delta snapshots. */
public final class DifferentialReachabilityQuestion extends Question {
  private static final String PROP_ACTIONS = "actions";
  private static final String PROP_HEADERS = "headers";
  private static final String PROP_IGNORE_FILTERS = "ignoreFilters";
  private static final String PROP_INVERT_SEARCH = "invertSearch";
  private static final String PROP_MAX_TRACES = "maxTraces";
  private static final String PROP_PATH_CONSTRAINTS = "pathConstraints";

  @Nonnull private final DispositionSpecifier _actions;
  @Nonnull private final PacketHeaderConstraints _headerConstraints;
  private final boolean _ignoreFilters;
  private final boolean _invertSearch;
  private final int _maxTraces;
  @Nonnull private final PathConstraintsInput _pathConstraints;

  @JsonCreator
  public DifferentialReachabilityQuestion(
      @Nullable @JsonProperty(PROP_ACTIONS) DispositionSpecifier actions,
      @Nullable @JsonProperty(PROP_HEADERS) PacketHeaderConstraints headerConstraints,
      @Nullable @JsonProperty(PROP_IGNORE_FILTERS) Boolean ignoreFilters,
      @Nullable @JsonProperty(PROP_INVERT_SEARCH) Boolean invertSearch,
      @Nullable @JsonProperty(PROP_MAX_TRACES) Integer maxTraces,
      @Nullable @JsonProperty(PROP_PATH_CONSTRAINTS) PathConstraintsInput pathConstraints) {
    setDifferential(true);
    _actions = firstNonNull(actions, SUCCESS_SPECIFIER);
    _headerConstraints = firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained());
    _ignoreFilters = firstNonNull(ignoreFilters, false);
    _invertSearch = firstNonNull(invertSearch, false);
    _maxTraces = firstNonNull(maxTraces, DEFAULT_MAX_TRACES);
    _pathConstraints = firstNonNull(pathConstraints, PathConstraintsInput.unconstrained());
  }

  public DifferentialReachabilityQuestion() {
    this(null, null, null, null, null, null);
  }

  @Nonnull
  DispositionSpecifier getActions() {
    return _actions;
  }

  @JsonProperty(PROP_HEADERS)
  @Nonnull
  PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @JsonProperty(PROP_IGNORE_FILTERS)
  boolean getIgnoreFilters() {
    return _ignoreFilters;
  }

  @JsonProperty(PROP_INVERT_SEARCH)
  public boolean getInvertSearch() {
    return _invertSearch;
  }

  @JsonProperty(PROP_MAX_TRACES)
  int getMaxTraces() {
    return _maxTraces;
  }

  @JsonProperty(PROP_PATH_CONSTRAINTS)
  @Nonnull
  PathConstraintsInput getPathConstraints() {
    return _pathConstraints;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "reducedreachability";
  }
}
