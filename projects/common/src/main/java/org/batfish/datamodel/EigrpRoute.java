package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.UnsignedLong;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpMetricVersion;
import org.batfish.datamodel.route.nh.NextHop;

/** Represents an EIGRP route, internal or external */
public abstract class EigrpRoute extends AbstractRoute {

  static final String PROP_EIGRP_METRIC = "eigrp-metric";
  static final String PROP_EIGRP_METRIC_VERSION = "eigrp-metric-version";
  static final String PROP_PROCESS_ASN = "process-asn";

  protected final long _admin;
  protected final @Nonnull EigrpMetric _metric;
  protected final @Nonnull EigrpMetricVersion _metricVersion;

  /** AS number of the EIGRP process that installed this route in the RIB */
  final long _processAsn;

  EigrpRoute(
      long admin,
      Prefix network,
      NextHop nextHop,
      @Nonnull EigrpMetric metric,
      @Nonnull EigrpMetricVersion metricVersion,
      long processAsn,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, admin, tag, nonRouting, nonForwarding);
    _admin = admin;
    _metric = metric;
    _metricVersion = metricVersion;
    _nextHop = nextHop;
    _processAsn = processAsn;
  }

  @JsonIgnore
  public final UnsignedLong getCompositeCost() {
    return _metric.cost(_metricVersion);
  }

  @JsonProperty(PROP_EIGRP_METRIC)
  public final @Nonnull EigrpMetric getEigrpMetric() {
    return _metric;
  }

  @JsonProperty(PROP_EIGRP_METRIC_VERSION)
  public final @Nonnull EigrpMetricVersion getEigrpMetricVersion() {
    return _metricVersion;
  }

  @Override
  public final long getMetric() {
    return _metric.ribMetric(_metricVersion);
  }

  @JsonProperty(PROP_PROCESS_ASN)
  public long getProcessAsn() {
    return _processAsn;
  }

  @Override
  public abstract RoutingProtocol getProtocol();

  public abstract static class Builder<B extends Builder<B, R>, R extends EigrpRoute>
      extends AbstractRouteBuilder<B, R> {
    protected @Nullable Long _destinationAsn;
    protected @Nullable EigrpMetric _eigrpMetric;
    protected @Nullable EigrpMetricVersion _eigrpMetricVersion;
    protected @Nullable Long _processAsn;

    public B setDestinationAsn(@Nonnull Long destinationAsn) {
      _destinationAsn = destinationAsn;
      return getThis();
    }

    public B setEigrpMetric(@Nonnull EigrpMetric metric) {
      _eigrpMetric = metric;
      return getThis();
    }

    public B setEigrpMetricVersion(@Nonnull EigrpMetricVersion version) {
      _eigrpMetricVersion = version;
      return getThis();
    }

    public B setProcessAsn(@Nullable Long processAsn) {
      _processAsn = processAsn;
      return getThis();
    }
  }
}
