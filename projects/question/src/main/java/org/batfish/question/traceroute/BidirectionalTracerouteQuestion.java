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
 * A question to perform a bidirectional traceroute.
 *
 * <p>This question performs a virtual traceroute through the network, similar to the {@link
 * TracerouteQuestion}. Additionally, if the trace is successful (i.e. has disposition {@value
 * org.batfish.specifier.DispositionSpecifier#SUCCESS}), then the question also performs a
 * traceroute in the reverse direction.
 */
public final class BidirectionalTracerouteQuestion extends Question {
  private static final String PROP_IGNORE_FILTERS = "ignoreFilters";
  private static final String PROP_SOURCE_LOCATION = "startLocation";
  private static final String PROP_HEADER_CONSTRAINTS = "headers";
  private static final String PROP_MAX_TRACES = "maxTraces";

  private final boolean _ignoreFilters;
  private final @Nullable String _sourceLocationStr;
  private final PacketHeaderConstraints _headerConstraints;
  private final int _maxTraces;

  @JsonCreator
  private static BidirectionalTracerouteQuestion create(
      @JsonProperty(PROP_IGNORE_FILTERS) boolean ignoreFilters,
      @JsonProperty(PROP_SOURCE_LOCATION) @Nullable String sourceLocationStr,
      @JsonProperty(PROP_HEADER_CONSTRAINTS) @Nullable PacketHeaderConstraints headerConstraints,
      @JsonProperty(PROP_MAX_TRACES) @Nullable Integer maxTraces) {
    return new BidirectionalTracerouteQuestion(
        sourceLocationStr,
        firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained()),
        ignoreFilters,
        firstNonNull(maxTraces, DEFAULT_MAX_TRACES));
  }

  /**
   * Create a new bidirectional traceroute question.
   *
   * @param sourceLocationStr string representation of location that results in a {@link
   *     org.batfish.specifier.LocationSpecifier}
   * @param headerConstraints {@link PacketHeaderConstraints} specifying what flow to construct when
   * @param ignoreFilters whether or not to evaluate ACLs on interfaces when performing a traceroute
   * @param maxTraces the maximum number of traces to include in the answer
   */
  public BidirectionalTracerouteQuestion(
      @Nullable String sourceLocationStr,
      @Nonnull PacketHeaderConstraints headerConstraints,
      boolean ignoreFilters,
      int maxTraces) {
    _ignoreFilters = ignoreFilters;
    _sourceLocationStr = sourceLocationStr;
    _headerConstraints = headerConstraints;
    _maxTraces = maxTraces;
  }

  BidirectionalTracerouteQuestion() {
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
    return "bidirectionalTraceroute";
  }
}
