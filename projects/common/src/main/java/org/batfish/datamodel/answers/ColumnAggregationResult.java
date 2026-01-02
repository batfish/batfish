package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BfConsts;

public class ColumnAggregationResult {

  @JsonCreator
  private static @Nonnull ColumnAggregationResult create(
      @JsonProperty(BfConsts.PROP_AGGREGATION) Aggregation aggregation,
      @JsonProperty(BfConsts.PROP_COLUMN) String column,
      @JsonProperty(BfConsts.PROP_VALUE) Optional<? extends Object> value) {
    return new ColumnAggregationResult(
        requireNonNull(aggregation), requireNonNull(column), requireNonNull(value));
  }

  private final Aggregation _aggregation;

  private final String _column;

  private final Object _value;

  public ColumnAggregationResult(
      @Nonnull Aggregation aggregation, @Nonnull String column, @Nonnull Object value) {
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

  @JsonProperty(BfConsts.PROP_AGGREGATION)
  public @Nonnull Aggregation getAggregation() {
    return _aggregation;
  }

  @JsonProperty(BfConsts.PROP_COLUMN)
  public @Nonnull String getColumn() {
    return _column;
  }

  @JsonProperty(BfConsts.PROP_VALUE)
  public @Nonnull Object getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aggregation.ordinal(), _column, _value);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(BfConsts.PROP_AGGREGATION, _aggregation)
        .add(BfConsts.PROP_COLUMN, _column)
        .add(BfConsts.PROP_VALUE, _value)
        .toString();
  }
}
