package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents the information about a route summary at an OSPF Area Border Router. */
@ParametersAreNonnullByDefault
public final class OspfAreaSummary implements Serializable {
  /** Describes which routes should be advertised and/or installed locally when summarizing. */
  public enum SummaryRouteBehavior {
    /**
     * Advertise the inter-area summary and install a discard route to prevent loops. This is the
     * Cisco-like and Juniper-like default.
     */
    ADVERTISE_AND_INSTALL_DISCARD,
    /**
     * Do not advertise the inter-area summary, but still install a discard route. This is like
     * Juniper "restrict" mode.
     */
    NOT_ADVERTISE_AND_INSTALL_DISCARD,
    /**
     * Do not advertise the inter-area summary and do not install a discard route. This is like
     * Cisco "no-advertise" mode.
     */
    NOT_ADVERTISE_AND_NO_DISCARD,
  }

  private static final String PROP_METRIC = "metric";
  private static final String PROP_BEHAVIOR = "behavior";

  private final SummaryRouteBehavior _behavior;
  private final @Nullable Long _metric;

  @JsonCreator
  private static OspfAreaSummary create(
      @JsonProperty(PROP_BEHAVIOR) @Nullable SummaryRouteBehavior behavior,
      @JsonProperty(PROP_METRIC) @Nullable Long metric) {
    checkArgument(behavior != null, "Missing %s", PROP_BEHAVIOR);
    return new OspfAreaSummary(behavior, metric);
  }

  @JsonIgnore
  public boolean isAdvertised() {
    return _behavior == SummaryRouteBehavior.ADVERTISE_AND_INSTALL_DISCARD;
  }

  @JsonIgnore
  public boolean installsDiscard() {
    return _behavior == SummaryRouteBehavior.ADVERTISE_AND_INSTALL_DISCARD
        || _behavior == SummaryRouteBehavior.NOT_ADVERTISE_AND_INSTALL_DISCARD;
  }

  public OspfAreaSummary(SummaryRouteBehavior behavior, @Nullable Long metric) {
    _behavior = behavior;
    _metric = metric;
  }

  /** Returns the {@link SummaryRouteBehavior} for this prefix. */
  @JsonProperty(PROP_BEHAVIOR)
  public SummaryRouteBehavior getBehavior() {
    return _behavior;
  }

  /**
   * Returns the OSPF metric for the summary route if overridden in the configuration. If {@code
   * null}, the summary metric will be computed on demand.
   */
  @JsonProperty(PROP_METRIC)
  public @Nullable Long getMetric() {
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
    return getBehavior() == that.getBehavior() && Objects.equals(getMetric(), that.getMetric());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_behavior.ordinal(), _metric);
  }
}
