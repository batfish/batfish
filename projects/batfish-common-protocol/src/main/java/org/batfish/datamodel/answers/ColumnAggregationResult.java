package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ColumnAggregationResult {

  public static final String PROP_AGGREGATION = "aggregation";

  public static final String PROP_COLUMN = "column";

  public static final String PROP_VALUE = "value";

  @JsonCreator
  private static @Nonnull ColumnAggregationResult create(
      @JsonProperty(PROP_AGGREGATION) Aggregation aggregation,
      @JsonProperty(PROP_COLUMN) String column,
      @JsonProperty(PROP_VALUE) Object value) {
    return new ColumnAggregationResult(requireNonNull(aggregation), requireNonNull(column), value);
  }

  private final Aggregation _aggregation;

  private final String _column;

  private final Object _value;

  public ColumnAggregationResult(
      @Nonnull Aggregation aggregation, @Nonnull String column, @Nullable Object value) {
    _aggregation = aggregation;
    _column = column;
    _value = value;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ColumnAggregationResult)) {
      return false;
    }
    ColumnAggregationResult rhs = (ColumnAggregationResult) obj;
    return _aggregation == rhs._aggregation
        && _column.equals(rhs._column)
        && Objects.equals(_value, rhs._value);
  }

  @JsonProperty(PROP_AGGREGATION)
  public @Nonnull Aggregation getAggregation() {
    return _aggregation;
  }

  @JsonProperty(PROP_COLUMN)
  public @Nonnull String getColumn() {
    return _column;
  }

  @JsonProperty(PROP_VALUE)
  public @Nullable Object getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aggregation.ordinal(), _column, _value);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_AGGREGATION, _aggregation)
        .add(PROP_COLUMN, _column)
        .add(PROP_VALUE, _value)
        .toString();
  }
}
