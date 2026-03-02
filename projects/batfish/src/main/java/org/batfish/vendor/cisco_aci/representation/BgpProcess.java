package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * BGP process configuration for L3Out.
 *
 * <p>Defines BGP process-level settings for an L3Out including AS number, router ID, administrative
 * distances, and BGP timers.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BgpProcess implements Serializable {
  private Long _as;
  private String _routerId;
  private Integer _ebgpAdminCost;
  private Integer _ibgpAdminCost;
  private Integer _vrfAdminCost;
  private Integer _keepalive;
  private Integer _holdTime;

  public @Nullable Long getAs() {
    return _as;
  }

  public void setAs(Long as) {
    _as = as;
  }

  public @Nullable String getRouterId() {
    return _routerId;
  }

  public void setRouterId(String routerId) {
    _routerId = routerId;
  }

  public @Nullable Integer getEbgpAdminCost() {
    return _ebgpAdminCost;
  }

  public void setEbgpAdminCost(Integer ebgpAdminCost) {
    _ebgpAdminCost = ebgpAdminCost;
  }

  public @Nullable Integer getIbgpAdminCost() {
    return _ibgpAdminCost;
  }

  public void setIbgpAdminCost(Integer ibgpAdminCost) {
    _ibgpAdminCost = ibgpAdminCost;
  }

  public @Nullable Integer getVrfAdminCost() {
    return _vrfAdminCost;
  }

  public void setVrfAdminCost(Integer vrfAdminCost) {
    _vrfAdminCost = vrfAdminCost;
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
