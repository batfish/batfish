package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Static route configuration for L3Out.
 *
 * <p>Defines a static route within an L3Out including prefix, next hop, and associated parameters.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StaticRoute implements Serializable {
  private String _prefix;
  private String _nextHop;
  private String _nextHopInterface;
  private String _administrativeDistance;
  private String _tag;
  private String _track;

  public @Nullable String getPrefix() {
    return _prefix;
  }

  public void setPrefix(String prefix) {
    _prefix = prefix;
  }

  public @Nullable String getNextHop() {
    return _nextHop;
  }

  public void setNextHop(String nextHop) {
    _nextHop = nextHop;
  }

  public @Nullable String getNextHopInterface() {
    return _nextHopInterface;
  }

  public void setNextHopInterface(String nextHopInterface) {
    _nextHopInterface = nextHopInterface;
  }

  public @Nullable String getAdministrativeDistance() {
    return _administrativeDistance;
  }

  public void setAdministrativeDistance(String administrativeDistance) {
    _administrativeDistance = administrativeDistance;
  }

  public @Nullable String getTag() {
    return _tag;
  }

  public void setTag(String tag) {
    _tag = tag;
  }

  public @Nullable String getTrack() {
    return _track;
  }

  public void setTrack(String track) {
    _track = track;
  }
}
