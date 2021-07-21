package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ospf.OspfAreaSummary;

/** Represents an XR OSPF area */
public class OspfArea implements Serializable {
  private final long _areaNum;
  private final Map<String, OspfInterfaceSettings> _interfaceSettings;
  @Nullable private NssaSettings _nssaSettings;
  private final OspfSettings _ospfSettings;
  @Nullable private StubSettings _stubSettings;
  private final Map<Prefix, OspfAreaSummary> _summaries;

  public OspfArea(long areaNum) {
    _areaNum = areaNum;
    _interfaceSettings = new TreeMap<>();
    _ospfSettings = new OspfSettings();
    _summaries = new TreeMap<>();
  }

  public long getAreaNum() {
    return _areaNum;
  }

  /** Mapping of interface name to {@link OspfInterfaceSettings} */
  public Map<String, OspfInterfaceSettings> getInterfaceSettings() {
    return _interfaceSettings;
  }

  public @Nullable NssaSettings getNssaSettings() {
    return _nssaSettings;
  }

  public @Nonnull NssaSettings getOrCreateNssaSettings() {
    if (_nssaSettings == null) {
      _nssaSettings = new NssaSettings();
    }
    // Area cannot be both an NSSA and a stub
    _stubSettings = null;
    return _nssaSettings;
  }

  public OspfSettings getOspfSettings() {
    return _ospfSettings;
  }

  public @Nullable StubSettings getStubSettings() {
    return _stubSettings;
  }

  public @Nonnull StubSettings getOrCreateStubSettings() {
    if (_stubSettings == null) {
      _stubSettings = new StubSettings();
    }
    // Area cannot be both an NSSA and a stub
    _nssaSettings = null;
    return _stubSettings;
  }

  public Map<Prefix, OspfAreaSummary> getSummaries() {
    return _summaries;
  }
}
