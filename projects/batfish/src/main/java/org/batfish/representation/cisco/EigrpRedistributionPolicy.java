package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetricValues;

public class EigrpRedistributionPolicy extends RedistributionPolicy {

  public static final String BGP_AS = "BGP_AS";
  public static final String OSPF_PROCESS_NUMBER = "OSPF_PROCESS_NUMBER";

  private @Nullable EigrpMetricValues _metric;

  public EigrpRedistributionPolicy(RoutingProtocolInstance instance) {
    super(instance);
  }

  public @Nullable EigrpMetricValues getMetric() {
    return _metric;
  }

  public void setMetric(@Nullable EigrpMetricValues metric) {
    _metric = metric;
  }
}
