package org.batfish.common.topology;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Jackson-serializable representation of a {@link com.google.common.graph.ValueGraph} or {@link
 * com.google.common.graph.Network} edge.
 */
@ParametersAreNonnullByDefault
public final class ValueEdge<N, V> {
  private static final String PROP_SOURCE = "source";
  private static final String PROP_TARGET = "target";
  private static final String PROP_VALUE = "value";

  private final N _source;
  private final N _target;
  private final V _value;

  @JsonCreator
  private static @Nonnull <N, V> ValueEdge<N, V> create(
      @JsonProperty(PROP_SOURCE) @Nullable N source,
      @JsonProperty(PROP_TARGET) @Nullable N target,
      @JsonProperty(PROP_VALUE) @Nullable V value) {
    checkArgument(source != null, "Missing %s", PROP_SOURCE);
    checkArgument(target != null, "Missing %s", PROP_TARGET);
    checkArgument(value != null, "Missing %s", PROP_VALUE);
    return new ValueEdge<>(source, target, value);
  }

  public ValueEdge(N source, N target, V value) {
    _source = source;
    _target = target;
    _value = value;
  }

  @JsonProperty(PROP_SOURCE)
  public @Nonnull N getSource() {
    return _source;
  }

  @JsonProperty(PROP_TARGET)
  public @Nonnull N getTarget() {
    return _target;
  }

  @JsonProperty(PROP_VALUE)
  public @Nonnull V getValue() {
    return _value;
  }
}
