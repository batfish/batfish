package org.batfish.datamodel.eigrp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.primitives.UnsignedLong;
import java.io.Serializable;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ClassicMetric.class, name = "classic"),
  @JsonSubTypes.Type(value = WideMetric.class, name = "wide")
})
public interface EigrpMetric extends Serializable {

  /** Return the metric as a single value, used for route preference tie breaking. */
  UnsignedLong cost(EigrpMetricVersion version);

  /**
   * Return metric as a single value, scaled as needed to fit into a 4-byte value. Used as a metric
   * expected to be seen in the main RIB.
   */
  long ribMetric(EigrpMetricVersion version);

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
}
