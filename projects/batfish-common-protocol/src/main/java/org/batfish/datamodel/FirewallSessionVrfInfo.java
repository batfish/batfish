package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Data that determines how to create sessions for flows that are ACCEPTED by a VRF. In particular,
 * sessions are needed for bidirectional traceroute and reachability, specifically for return flows.
 */
public final class FirewallSessionVrfInfo {
  private static final String PROP_FIB_LOOKUP = "fibLookup";

  private final boolean _fibLookup;

  public FirewallSessionVrfInfo(boolean fibLookup) {
    _fibLookup = fibLookup;
  }

  @JsonCreator
  private static FirewallSessionVrfInfo jsonCreator(
      @JsonProperty(PROP_FIB_LOOKUP) boolean fibLookup) {
    return new FirewallSessionVrfInfo(fibLookup);
  }

  /** Whether session return traffic should be forwarded via a FIB lookup */
  @JsonProperty(PROP_FIB_LOOKUP)
  public boolean getFibLookup() {
    return _fibLookup;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FirewallSessionVrfInfo)) {
      return false;
    }
    FirewallSessionVrfInfo that = (FirewallSessionVrfInfo) o;
    return _fibLookup == that._fibLookup;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_fibLookup);
  }
}
