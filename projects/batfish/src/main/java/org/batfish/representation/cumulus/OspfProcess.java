package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfMetricType;

/** Class for ospf process */
public class OspfProcess implements Serializable {

  private final @Nonnull OspfVrf _defaultVrf;
  private boolean _defaultPassiveInterface;
  private final @Nonnull Map<String, OspfVrf> _vrfs;
  public static final double DEFAULT_REFERENCE_BANDWIDTH =
      100E9D; // https://docs.cumulusnetworks.com/cumulus-linux/Layer-3/Open-Shortest-Path-First-OSPF/#auto-cost-reference-bandwidth
  public static String DEFAULT_OSPF_PROCESS_NAME = "default";

  private static final OspfMetricType DEFAULT_REDISTRIBUTE_METRIC_TYPE = OspfMetricType.E2;

  private static final Long DEFAULT_METRIC = 20L;

  private Map<RoutingProtocol, OspfRedistributionPolicy> _redistributionPolicies;

  private Long _defaultMetric;

  private OspfMetricType _defaultRedistributeMetricType;

  public OspfProcess() {
    _defaultVrf = new OspfVrf(Configuration.DEFAULT_VRF_NAME);
    // default value
    _defaultPassiveInterface = false;
    _vrfs = new HashMap<>();
    _redistributionPolicies = new EnumMap<>(RoutingProtocol.class);
    _defaultMetric = DEFAULT_METRIC;
    _defaultRedistributeMetricType = DEFAULT_REDISTRIBUTE_METRIC_TYPE;
  }

  public @Nonnull OspfVrf getDefaultVrf() {
    return _defaultVrf;
  }

  public @Nonnull Map<String, OspfVrf> getVrfs() {
    return _vrfs;
  }

  public boolean getDefaultPassiveInterface() {
    return _defaultPassiveInterface;
  }

  public void setDefaultPassiveInterface(boolean defaultPassiveInterface) {
    _defaultPassiveInterface = defaultPassiveInterface;
  }

  public long getDefaultMetric() {
    return _defaultMetric;
  }

  public OspfMetricType getDefaultRedistributeMetricType() {
    return _defaultRedistributeMetricType;
  }

  public void setDefaultMetric(Long metric) {
    _defaultMetric = metric;
  }

  public void setDefaultRedistributeMetricType(OspfMetricType metricType) {
    _defaultRedistributeMetricType = metricType;
  }

  public Map<RoutingProtocol, OspfRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }
}
