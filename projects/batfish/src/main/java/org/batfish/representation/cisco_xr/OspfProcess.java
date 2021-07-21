package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfMetricType;

public class OspfProcess implements Serializable {

  private static final long DEFAULT_DEFAULT_INFORMATION_METRIC = 1L;
  private static final OspfMetricType DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE = OspfMetricType.E2;
  // Although not clearly documented; from GNS3 emulation and CiscoXr forum
  // (https://community.cisco_xr.com/t5/switching/ospf-cost-calculation/td-p/2917356)
  public static final int DEFAULT_LOOPBACK_OSPF_COST = 1;
  public static final long DEFAULT_MAX_METRIC_EXTERNAL_LSA = 0xFF0000L;
  public static final long DEFAULT_MAX_METRIC_SUMMARY_LSA = 0xFF0000L;
  // https://www.cisco.com/c/en/us/td/docs/ios_xr_sw/iosxr_r3-7/routing/command/reference/rr37ospf.html
  public static final double DEFAULT_OSPF_REFERENCE_BANDWIDTH = 100E6D;
  public static final long MAX_METRIC_ROUTER_LSA = 0xFFFFL;

  private final Map<Long, OspfArea> _areas;
  private long _defaultInformationMetric;
  private OspfMetricType _defaultInformationMetricType;
  private boolean _defaultInformationOriginate;
  private boolean _defaultInformationOriginateAlways;
  private String _defaultInformationOriginateMap;
  private Long _defaultMetric;
  private Long _maxMetricExternalLsa;
  private boolean _maxMetricIncludeStub;
  private boolean _maxMetricRouterLsa;
  private Long _maxMetricSummaryLsa;
  private final String _name;
  private final OspfSettings _ospfSettings;
  private Map<RoutingProtocol, OspfRedistributionPolicy> _redistributionPolicies;
  private double _referenceBandwidth;
  private Ip _routerId;

  public long getDefaultMetric(RoutingProtocol protocol) {
    if (_defaultMetric != null) {
      return _defaultMetric;
    }

    // https://www.cisco.com/c/en/us/support/docs/ip/open-shortest-path-first-ospf/7039-1.html
    // "the cost allocated to the external route is 20 (the default is 1 for BGP)."
    if (protocol == RoutingProtocol.BGP) {
      return 1;
    }
    return 20;
  }

  public OspfProcess(String name) {
    _name = name;
    _referenceBandwidth = DEFAULT_OSPF_REFERENCE_BANDWIDTH;
    _defaultInformationMetric = DEFAULT_DEFAULT_INFORMATION_METRIC;
    _defaultInformationMetricType = DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE;
    _areas = new TreeMap<>();
    _ospfSettings = new OspfSettings();
    _redistributionPolicies = new EnumMap<>(RoutingProtocol.class);
  }

  public Map<Long, OspfArea> getAreas() {
    return _areas;
  }

  public long getDefaultInformationMetric() {
    return _defaultInformationMetric;
  }

  public OspfMetricType getDefaultInformationMetricType() {
    return _defaultInformationMetricType;
  }

  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public boolean getDefaultInformationOriginateAlways() {
    return _defaultInformationOriginateAlways;
  }

  public String getDefaultInformationOriginateMap() {
    return _defaultInformationOriginateMap;
  }

  public Long getDefaultMetric() {
    return _defaultMetric;
  }

  public Long getMaxMetricExternalLsa() {
    return _maxMetricExternalLsa;
  }

  public boolean getMaxMetricIncludeStub() {
    return _maxMetricIncludeStub;
  }

  public boolean getMaxMetricRouterLsa() {
    return _maxMetricRouterLsa;
  }

  public Long getMaxMetricSummaryLsa() {
    return _maxMetricSummaryLsa;
  }

  public String getName() {
    return _name;
  }

  public OspfSettings getOspfSettings() {
    return _ospfSettings;
  }

  public Map<RoutingProtocol, OspfRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  public Ip getRouterId() {
    return _routerId;
  }

  public void setDefaultInformationMetric(int metric) {
    _defaultInformationMetric = metric;
  }

  public void setDefaultInformationMetricType(OspfMetricType metricType) {
    _defaultInformationMetricType = metricType;
  }

  public void setDefaultInformationOriginate(boolean b) {
    _defaultInformationOriginate = b;
  }

  public void setDefaultInformationOriginateAlways(boolean b) {
    _defaultInformationOriginateAlways = b;
  }

  public void setDefaultInformationOriginateMap(String name) {
    _defaultInformationOriginateMap = name;
  }

  public void setDefaultMetric(Long metric) {
    _defaultMetric = metric;
  }

  public void setMaxMetricExternalLsa(Long maxMetricExternalLsa) {
    _maxMetricExternalLsa = maxMetricExternalLsa;
  }

  public void setMaxMetricIncludeStub(boolean maxMetricIncludeStub) {
    _maxMetricIncludeStub = maxMetricIncludeStub;
  }

  public void setMaxMetricRouterLsa(boolean maxMetricRouterLsa) {
    _maxMetricRouterLsa = maxMetricRouterLsa;
  }

  public void setMaxMetricSummaryLsa(Long maxMetricSummaryLsa) {
    _maxMetricSummaryLsa = maxMetricSummaryLsa;
  }

  public void setReferenceBandwidth(double referenceBandwidth) {
    _referenceBandwidth = referenceBandwidth;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }
}
