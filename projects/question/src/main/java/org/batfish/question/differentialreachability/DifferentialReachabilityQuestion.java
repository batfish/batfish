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

  private final @Nonnull DispositionSpecifier _actions;
  private final @Nonnull PacketHeaderConstraints _headerConstraints;
  private final boolean _ignoreFilters;
  private final boolean _invertSearch;
  private final int _maxTraces;
  private final @Nonnull PathConstraintsInput _pathConstraints;

  @JsonCreator
  public DifferentialReachabilityQuestion(
      @JsonProperty(PROP_ACTIONS) @Nullable DispositionSpecifier actions,
      @JsonProperty(PROP_HEADERS) @Nullable PacketHeaderConstraints headerConstraints,
      @JsonProperty(PROP_IGNORE_FILTERS) @Nullable Boolean ignoreFilters,
      @JsonProperty(PROP_INVERT_SEARCH) @Nullable Boolean invertSearch,
      @JsonProperty(PROP_MAX_TRACES) @Nullable Integer maxTraces,
      @JsonProperty(PROP_PATH_CONSTRAINTS) @Nullable PathConstraintsInput pathConstraints) {
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
