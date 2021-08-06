package org.batfish.representation.cisco_xr;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfMetricType;

/** Represents options for default-information originate in OSPF */
public class OspfDefaultInformationOriginate implements Serializable {
  @VisibleForTesting public static final long DEFAULT_METRIC = 1L;
  @VisibleForTesting public static final OspfMetricType DEFAULT_METRIC_TYPE = OspfMetricType.E2;

  public OspfDefaultInformationOriginate() {
    _metric = DEFAULT_METRIC;
    _metricType = DEFAULT_METRIC_TYPE;
  }

  public boolean getAlways() {
    return _always;
  }

  public long getMetric() {
    return _metric;
  }

  public @Nonnull OspfMetricType getMetricType() {
    return _metricType;
  }

  public void setAlways(boolean b) {
    _always = b;
  }

  public void setMetric(int metric) {
    _metric = metric;
  }

  public void setMetricType(@Nonnull OspfMetricType metricType) {
    _metricType = metricType;
  }

  private boolean _always;
  private long _metric;
  private @Nonnull OspfMetricType _metricType;
}
