package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfMetricType;

public class OspfProcess implements Serializable {

  private static final long DEFAULT_DEFAULT_INFORMATION_METRIC = 1L;

  private static final OspfMetricType DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE = OspfMetricType.E2;

  // Although not clearly documented; from GNS3 emulation and Cisco forum
  // (https://community.cisco.com/t5/switching/ospf-cost-calculation/td-p/2917356)
  public static final int DEFAULT_LOOPBACK_OSPF_COST = 1;

  public static final long DEFAULT_MAX_METRIC_EXTERNAL_LSA = 0xFF0000L;

  public static final long DEFAULT_MAX_METRIC_SUMMARY_LSA = 0xFF0000L;

  private static final double DEFAULT_REFERENCE_BANDWIDTH_10_MBPS = 10E6D;

  public static final long MAX_METRIC_ROUTER_LSA = 0xFFFFL;

  private long _defaultInformationMetric;

  private OspfMetricType _defaultInformationMetricType;

  private boolean _defaultInformationOriginate;

  private boolean _defaultInformationOriginateAlways;

  private String _defaultInformationOriginateMap;

  private Long _defaultMetric;

  private @Nullable DistributeList _inboundGlobalDistributeList;

  private @Nonnull Map<String, DistributeList> _inboundIInterfaceDistributeLists;

  private Long _maxMetricExternalLsa;

  private boolean _maxMetricIncludeStub;

  private boolean _maxMetricRouterLsa;

  private Long _maxMetricSummaryLsa;

  private final String _name;

  private Set<OspfNetwork> _networks;

  private Set<String> _nonDefaultInterfaces;

  private Map<Long, NssaSettings> _nssas;

  private Map<Long, StubSettings> _stubs;

  private @Nullable DistributeList _outboundGlobalDistributeList;

  private @Nonnull Map<String, DistributeList> _outboundInterfaceDistributeLists;

  private boolean _passiveInterfaceDefault;

  private Set<String> _passiveInterfaces;

  private Map<RedistributionSourceProtocol, OspfRedistributionPolicy> _redistributionPolicies;

  private double _referenceBandwidth;

  private @Nullable Boolean _rfc1583Compatible;

  private Ip _routerId;

  private Map<Long, Map<Prefix, OspfAreaSummary>> _summaries;

  public static double getReferenceOspfBandwidth() {
    // EOS manual, Chapter 27, "auto-cost reference-bandwidth (OSPFv2)"
    return DEFAULT_REFERENCE_BANDWIDTH_10_MBPS;
  }

  public long getEffectiveDefaultMetric() {
    if (_defaultMetric != null) {
      return _defaultMetric;
    }

    // Inferred from Arista manual OSPF v3 default-metric comment.
    return 10;
  }

  public OspfProcess(String name) {
    _name = name;
    _referenceBandwidth = getReferenceOspfBandwidth();
    _networks = new TreeSet<>();
    _defaultInformationMetric = DEFAULT_DEFAULT_INFORMATION_METRIC;
    _defaultInformationMetricType = DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE;
    _inboundIInterfaceDistributeLists = new HashMap<>();
    _nonDefaultInterfaces = new HashSet<>();
    _nssas = new HashMap<>();
    _outboundInterfaceDistributeLists = new HashMap<>();
    _passiveInterfaces = new HashSet<>();
    _stubs = new HashMap<>();
    _redistributionPolicies = new EnumMap<>(RedistributionSourceProtocol.class);
    _summaries = new TreeMap<>();
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

  public @Nullable DistributeList getInboundGlobalDistributeList() {
    return _inboundGlobalDistributeList;
  }

  public @Nonnull Map<String, DistributeList> getInboundInterfaceDistributeLists() {
    return _inboundIInterfaceDistributeLists;
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

  public Set<OspfNetwork> getNetworks() {
    return _networks;
  }

  public Set<String> getNonDefaultInterfaces() {
    return _nonDefaultInterfaces;
  }

  public Map<Long, NssaSettings> getNssas() {
    return _nssas;
  }

  public @Nullable DistributeList getOutboundGlobalDistributeList() {
    return _outboundGlobalDistributeList;
  }

  public @Nonnull Map<String, DistributeList> getOutboundInterfaceDistributeLists() {
    return _outboundInterfaceDistributeLists;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public Set<String> getPassiveInterfaces() {
    return _passiveInterfaces;
  }

  public Map<RedistributionSourceProtocol, OspfRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  public @Nullable Boolean getRfc1583Compatible() {
    return _rfc1583Compatible;
  }

  public Ip getRouterId() {
    return _routerId;
  }

  public Map<Long, StubSettings> getStubs() {
    return _stubs;
  }

  public Map<Long, Map<Prefix, OspfAreaSummary>> getSummaries() {
    return _summaries;
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

  public void setInboundGlobalDistributeList(@Nullable DistributeList inboundGlobalDistributeList) {
    _inboundGlobalDistributeList = inboundGlobalDistributeList;
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

  public void setOutboundGlobalDistributeList(
      @Nullable DistributeList outboundGlobalDistributeList) {
    _outboundGlobalDistributeList = outboundGlobalDistributeList;
  }

  public void setPassiveInterfaceDefault(boolean b) {
    _passiveInterfaceDefault = b;
  }

  public void setReferenceBandwidth(double referenceBandwidth) {
    _referenceBandwidth = referenceBandwidth;
  }

  public void setRfc1583Compatible(@Nullable Boolean rfc1583Compatible) {
    _rfc1583Compatible = rfc1583Compatible;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }
}
