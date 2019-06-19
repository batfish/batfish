package org.batfish.question.traceroute;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;

/**
 * A question to perform a traceroute.
 *
 * <p>This question performs a virtual traceroute in the network from ingress node. The destination
 * IP is randomly picked if not explicitly specified. Other IP headers are also randomly picked if
 * unspecified, with a bias toward generating packets similar to a real traceroute (see below).
 *
 * <p>Unlike a real traceroute, this traceroute is directional. That is, for it to succeed, the
 * reverse connectivity is not needed. This feature can help debug connectivity issues by decoupling
 * the two directions.
 */
public final class TracerouteQuestion extends Question {
  private static final String PROP_IGNORE_FILTERS = "ignoreFilters";
  private static final String PROP_SOURCE_LOCATION = "startLocation";
  private static final String PROP_HEADER_CONSTRAINTS = "headers";
  private static final String PROP_MAX_TRACES = "maxTraces";

  private final boolean _ignoreFilters;
  private final @Nullable String _sourceLocationStr;
  private final PacketHeaderConstraints _headerConstraints;
  private final int _maxTraces;

  @JsonCreator
  private static TracerouteQuestion create(
      @JsonProperty(PROP_IGNORE_FILTERS) boolean ignoreFilters,
      @JsonProperty(PROP_SOURCE_LOCATION) @Nullable String sourceLocationStr,
      @JsonProperty(PROP_HEADER_CONSTRAINTS) @Nullable PacketHeaderConstraints headerConstraints,
      @JsonProperty(PROP_MAX_TRACES) @Nullable Integer maxTraces) {
    return new TracerouteQuestion(
        sourceLocationStr,
        firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained()),
        ignoreFilters,
        firstNonNull(maxTraces, DEFAULT_MAX_TRACES));
  }

  /**
   * Create a new traceroute question.
   *
   * @param sourceLocationStr string representation of location that results in a {@link
   *     org.batfish.specifier.LocationSpecifier}
   * @param headerConstraints {@link PacketHeaderConstraints} specifying what flow to construct when
   * @param ignoreFilters whether or not to evaluate ACLs on interfaces when performing a traceroute
   * @param maxTraces the maximum number of traces to include in the answer
   */
  public TracerouteQuestion(
      @Nullable String sourceLocationStr,
      @Nonnull PacketHeaderConstraints headerConstraints,
      boolean ignoreFilters,
      int maxTraces) {
    _ignoreFilters = ignoreFilters;
    _sourceLocationStr = sourceLocationStr;
    _headerConstraints = headerConstraints;
    _maxTraces = maxTraces;
  }

  TracerouteQuestion() {
    this(null, PacketHeaderConstraints.unconstrained(), false, DEFAULT_MAX_TRACES);
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Nonnull
  @JsonProperty(PROP_HEADER_CONSTRAINTS)
  PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @JsonProperty(PROP_IGNORE_FILTERS)
  public boolean getIgnoreFilters() {
    return _ignoreFilters;
  }

  @JsonProperty(PROP_MAX_TRACES)
  public int getMaxTraces() {
    return _maxTraces;
  }

  @JsonProperty(PROP_SOURCE_LOCATION)
  public @Nullable String getSourceLocationStr() {
    return _sourceLocationStr;
  }

  @Override
  public String getName() {
    return "traceroute";
  }
}
