package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.StubType;

public class OspfArea implements Serializable {

  private boolean _injectDefaultRoute = false;

  private int _metricOfDefaultRoute = 0;

  private NssaSettings _nssaSettings;

  private StubSettings _stubSettings;

  private StubType _stubType;

  private SortedMap<Prefix, OspfAreaSummary> _summaries;

  private final Long _name;

  public OspfArea(Long name) {
    _name = name;
    _stubType = StubType.NONE;
    _summaries = new TreeMap<>();
  }

  public boolean getInjectDefaultRoute() {
    return _injectDefaultRoute;
  }

  public int getMetricOfDefaultRoute() {
    return _metricOfDefaultRoute;
  }

  public NssaSettings getNssaSettings() {
    return _nssaSettings;
  }

  public StubSettings getStubSettings() {
    return _stubSettings;
  }

  public StubType getStubType() {
    return _stubType;
  }

  public SortedMap<Prefix, OspfAreaSummary> getSummaries() {
    return _summaries;
  }

  public void setInjectDefaultRoute(boolean injectDefaultRoute) {
    _injectDefaultRoute = injectDefaultRoute;
  }

  public void setMetricOfDefaultRoute(int metricOfDefaultRoute) {
    _metricOfDefaultRoute = metricOfDefaultRoute;
  }

  public void setNssaSettings(NssaSettings nssaSettings) {
    _nssaSettings = nssaSettings;
  }

  public void setStubSettings(StubSettings stubSettings) {
    _stubSettings = stubSettings;
  }

  public void setStubType(StubType stubType) {
    _stubType = stubType;
  }

  public Long getName() {
    return _name;
  }
}
