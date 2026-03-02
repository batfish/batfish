package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * ACI L3Out (Layer 3 Outside) configuration.
 *
 * <p>An L3Out defines external connectivity for a tenant, including BGP peering, static routes,
 * OSPF configuration, and external EPGs (L3ExtEpg).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class L3Out implements Serializable {
  private final String _name;
  private String _tenant;
  private String _vrf;
  private String _description;
  private String _enforceRouteControl;
  private String _mplsEnabled;
  private String _targetDscp;
  private BgpProcess _bgpProcess;
  private List<BgpPeer> _bgpPeers;
  private List<StaticRoute> _staticRoutes;
  private OspfConfig _ospfConfig;
  private List<ExternalEpg> _externalEpgs;
  private List<PathAttachment> _pathAttachments;

  public L3Out(String name) {
    _name = name;
    _bgpPeers = new ArrayList<>();
    _staticRoutes = new ArrayList<>();
    _externalEpgs = new ArrayList<>();
    _pathAttachments = new ArrayList<>();
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getTenant() {
    return _tenant;
  }

  public void setTenant(String tenant) {
    _tenant = tenant;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nullable String getEnforceRouteControl() {
    return _enforceRouteControl;
  }

  public void setEnforceRouteControl(String enforceRouteControl) {
    _enforceRouteControl = enforceRouteControl;
  }

  public @Nullable String getMplsEnabled() {
    return _mplsEnabled;
  }

  public void setMplsEnabled(String mplsEnabled) {
    _mplsEnabled = mplsEnabled;
  }

  public @Nullable String getTargetDscp() {
    return _targetDscp;
  }

  public void setTargetDscp(String targetDscp) {
    _targetDscp = targetDscp;
  }

  public @Nullable BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public void setBgpProcess(BgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  public List<BgpPeer> getBgpPeers() {
    return _bgpPeers;
  }

  public void setBgpPeers(List<BgpPeer> bgpPeers) {
    _bgpPeers = new ArrayList<>(bgpPeers);
  }

  public List<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public void setStaticRoutes(List<StaticRoute> staticRoutes) {
    _staticRoutes = new ArrayList<>(staticRoutes);
  }

  public @Nullable OspfConfig getOspfConfig() {
    return _ospfConfig;
  }

  public void setOspfConfig(OspfConfig ospfConfig) {
    _ospfConfig = ospfConfig;
  }

  public List<ExternalEpg> getExternalEpgs() {
    return _externalEpgs;
  }

  public void setExternalEpgs(List<ExternalEpg> externalEpgs) {
    _externalEpgs = new ArrayList<>(externalEpgs);
  }

  public List<PathAttachment> getPathAttachments() {
    return _pathAttachments;
  }

  public void setPathAttachments(List<PathAttachment> pathAttachments) {
    _pathAttachments = new ArrayList<>(pathAttachments);
  }
}
