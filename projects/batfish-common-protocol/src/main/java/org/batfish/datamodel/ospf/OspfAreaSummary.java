package org.batfish.datamodel.ospf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Represents the information about a particular router summarization at an OSPF Area Border Router.
 */
public final class OspfAreaSummary implements Serializable {
  private static final String PROP_ADVERTISE = "advertise";

  private static final String PROP_METRIC = "metric";

  private static final long serialVersionUID = 1L;

  private final boolean _advertised;
  @Nullable private final Long _metric;

  @JsonCreator
  public OspfAreaSummary(
      @JsonProperty(PROP_ADVERTISE) boolean advertised,
      @JsonProperty(PROP_METRIC) @Nullable Long metric) {
    this._advertised = advertised;
    this._metric = metric;
  }

  /** Returns true if this summarized route is advertised externally. */
  @JsonProperty(PROP_ADVERTISE)
  public boolean getAdvertised() {
    return _advertised;
  }

  /**
   * Returns the OSPF metric for the summary route if overridden in the configuration. If {@code
   * null}, the summary metric will be computed on demand.
   */
  @JsonProperty(PROP_METRIC)
  @Nullable
  public Long getMetric() {
    return _metric;
  }
}
