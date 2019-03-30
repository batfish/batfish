package org.batfish.datamodel.ospf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents the information about a route summary at an OSPF Area Border Router. */
@ParametersAreNonnullByDefault
public final class OspfAreaSummary implements Serializable {
  private static final String PROP_ADVERTISE = "advertise";
  private static final String PROP_METRIC = "metric";

  private static final long serialVersionUID = 1L;

  private final boolean _advertised;
  @Nullable private final Long _metric;

  @JsonCreator
  private static OspfAreaSummary create(
      @JsonProperty(PROP_ADVERTISE) boolean advertised,
      @JsonProperty(PROP_METRIC) @Nullable Long metric) {
    return new OspfAreaSummary(advertised, metric);
  }

  public OspfAreaSummary(boolean advertised, @Nullable Long metric) {
    _advertised = advertised;
    _metric = metric;
  }

  /** Returns true if the summarized route should be advertised externally. */
  @JsonProperty(PROP_ADVERTISE)
  public boolean getAdvertised() {
    return _advertised;
  }

  /**
   * Returns the OSPF metric for the summary route if overridden in the configuration. If {@code
   * null}, the summary metric will be computed on demand.
   */
  @Nullable
  @JsonProperty(PROP_METRIC)
  public Long getMetric() {
    return _metric;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfAreaSummary)) {
      return false;
    }
    OspfAreaSummary that = (OspfAreaSummary) o;
    return getAdvertised() == that.getAdvertised() && Objects.equals(getMetric(), that.getMetric());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_advertised, _metric);
  }
}
