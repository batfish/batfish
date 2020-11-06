package org.batfish.datamodel.eigrp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.primitives.UnsignedLong;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ClassicMetric.class, name = "classic"),
  @JsonSubTypes.Type(value = WideMetric.class, name = "wide")
})
public interface EigrpMetric extends Serializable {

  /** Return metric as a single value, used for route preference tie breaking. */
  UnsignedLong cost();

  /**
   * Return metric as a single value, scaled as needed to fit into a 4-byte value. Used for route
   * preference tie breaking.
   */
  long ribMetric();

  /**
   * Check if this metric and {@code other} are compatible, i.e., have the same type and k values
   * (multipliers). Only compatible metrics can be {@link #add(EigrpMetric) added}, and only EIGRP
   * adjacencies with compatible metrics should be established.
   */
  boolean isCompatible(EigrpMetric other);

  /**
   * Accumulate this metric with another metric (e.g., during path metric computation)
   *
   * @param other metric to add
   * @throws IllegalArgumentException if {@code other} is not a valid metric type to add (see {@link
   *     #isCompatible(EigrpMetric)})
   */
  EigrpMetric add(EigrpMetric other);

  /**
   * Return EIGRP {@link EigrpMetricValues metric values}, prior to any scaling or composite cost
   * calculation. See {@link EigrpMetricValues} for units.
   */
  EigrpMetricValues getValues();

  Builder<?, ?> toBuilder();

  abstract class Builder<B extends Builder<B, M>, M extends EigrpMetric> {
    @Nullable protected EigrpMetricValues _values;
    protected short _k1 = 1;
    protected short _k3 = 1;

    @Nonnull
    protected abstract B getThis();

    @Nonnull
    public B setValues(EigrpMetricValues values) {
      _values = values;
      return getThis();
    }

    @Nonnull
    public B setK1(short k1) {
      checkArgument(k1 <= 255);
      _k1 = k1;
      return getThis();
    }

    @Nonnull
    public B setK1(int k1) {
      checkArgument(k1 <= 255);
      _k1 = (short) k1;
      return getThis();
    }

    @Nonnull
    public B setK3(short k3) {
      checkArgument(k3 <= 255);
      _k3 = k3;
      return getThis();
    }

    @Nonnull
    public B setK3(int k3) {
      checkArgument(k3 <= 255);
      _k3 = (short) k3;
      return getThis();
    }

    public abstract M build();
  }
}
