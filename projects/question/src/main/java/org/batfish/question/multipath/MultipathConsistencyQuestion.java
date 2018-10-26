package org.batfish.question.multipath;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.specifiers.PathConstraintsInput;
import org.batfish.specifier.Location;

/**
 * A zero-input question to check for multipath inconsistencies. It computes all-pairs reachability
 * from any {@link Location source} to any destination, and returns traces for all detected
 * multipath inconsistencies.
 *
 * <p>In the future, we can consider adding a flag that would stop the reachability analysis after
 * the first multipath inconsistency is found.
 */
public class MultipathConsistencyQuestion extends Question {
  private static final String PROP_HEADERS = "headers";
  private static final String PROP_PATH_CONSTRAINTS = "pathConstraints";

  @Nonnull private final PathConstraintsInput _pathConstraints;
  @Nonnull private final PacketHeaderConstraints _headerConstraints;

  @JsonCreator
  public MultipathConsistencyQuestion(
      @Nullable @JsonProperty(PROP_HEADERS) PacketHeaderConstraints headerConstraints,
      @Nullable @JsonProperty(PROP_PATH_CONSTRAINTS) PathConstraintsInput pathConstraints) {
    setDifferential(false);
    _headerConstraints = firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained());
    _pathConstraints = firstNonNull(pathConstraints, PathConstraintsInput.unconstrained());
  }

  public MultipathConsistencyQuestion() {
    this(null, null);
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @JsonProperty(PROP_HEADERS)
  @Nonnull
  PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @Override
  public String getName() {
    return "multipath";
  }

  @JsonProperty(PROP_PATH_CONSTRAINTS)
  @Nonnull
  PathConstraintsInput getPathConstraints() {
    return _pathConstraints;
  }
}
