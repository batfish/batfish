package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * BGP peer configuration for L3Out.
 *
 * <p>Defines a BGP peer within an L3Out including peer address, AS numbers, policies, and route
 * target (route-map) configurations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BgpPeer implements Serializable {
  private String _peerAddress;
  private String _remoteAs;
  private String _localAs;
  private String _updateSourceInterface;
  private String _password;
  private String _description;
  private Boolean _ebgpMultihop;
  private Integer _ttl;
  private Boolean _routeReflectorClient;
  private Boolean _nextHopSelf;
  private Boolean _sendCommunities;
  private String _localPreference;
  private String _med;
  private String _importRouteMap;
  private String _exportRouteMap;
  private List<String> _routeTargets;
  private Integer _keepalive;
  private Integer _holdTime;

  public BgpPeer() {
    _routeTargets = new ArrayList<>();
  }

  public @Nullable String getPeerAddress() {
    return _peerAddress;
  }

  public void setPeerAddress(String peerAddress) {
    _peerAddress = peerAddress;
  }

  public @Nullable String getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(String remoteAs) {
    _remoteAs = remoteAs;
  }

  public @Nullable String getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(String localAs) {
    _localAs = localAs;
  }

  public @Nullable String getUpdateSourceInterface() {
    return _updateSourceInterface;
  }

  public void setUpdateSourceInterface(String updateSourceInterface) {
    _updateSourceInterface = updateSourceInterface;
  }

  public @Nullable String getPassword() {
    return _password;
  }

  public void setPassword(String password) {
    _password = password;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nullable Boolean getEbgpMultihop() {
    return _ebgpMultihop;
  }

  public void setEbgpMultihop(Boolean ebgpMultihop) {
    _ebgpMultihop = ebgpMultihop;
  }

  public @Nullable Integer getTtl() {
    return _ttl;
  }

  public void setTtl(Integer ttl) {
    _ttl = ttl;
  }

  public @Nullable Boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  public void setRouteReflectorClient(Boolean routeReflectorClient) {
    _routeReflectorClient = routeReflectorClient;
  }

  public @Nullable Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(Boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  public @Nullable Boolean getSendCommunities() {
    return _sendCommunities;
  }

  public void setSendCommunities(Boolean sendCommunities) {
    _sendCommunities = sendCommunities;
  }

  public @Nullable String getLocalPreference() {
    return _localPreference;
  }

  public void setLocalPreference(String localPreference) {
    _localPreference = localPreference;
  }

  public @Nullable String getMed() {
    return _med;
  }

  public void setMed(String med) {
    _med = med;
  }

  public @Nullable String getImportRouteMap() {
    return _importRouteMap;
  }

  public void setImportRouteMap(String importRouteMap) {
    _importRouteMap = importRouteMap;
  }

  public @Nullable String getExportRouteMap() {
    return _exportRouteMap;
  }

  public void setExportRouteMap(String exportRouteMap) {
    _exportRouteMap = exportRouteMap;
  }

  /**
   * Returns the list of route targets (RTs) configured for this BGP peer. Route targets are used in
   * BGP route-maps to control route import/export.
   *
   * @return List of route target strings (e.g., "route-target:65000:1")
   */
  public List<String> getRouteTargets() {
    return _routeTargets;
  }

  public void setRouteTargets(List<String> routeTargets) {
    _routeTargets = new ArrayList<>(routeTargets);
  }

  public void addRouteTarget(String routeTarget) {
    if (_routeTargets == null) {
      _routeTargets = new ArrayList<>();
    }
    _routeTargets.add(routeTarget);
  }

  public @Nullable Integer getKeepalive() {
    return _keepalive;
  }

  public void setKeepalive(Integer keepalive) {
    _keepalive = keepalive;
  }

  public @Nullable Integer getHoldTime() {
    return _holdTime;
  }

  public void setHoldTime(Integer holdTime) {
    _holdTime = holdTime;
  }
}
