package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/** Per-family packet processing modes under {@code security forwarding-options}. */
public final class SecurityForwardingOptions implements Serializable {

  public enum Family {
    INET,
    INET6,
    ISO,
    MPLS
  }

  public enum Mode {
    DROP,
    FLOW_BASED,
    PACKET_BASED
  }

  private final Map<Family, Mode> _familyModes;

  public SecurityForwardingOptions() {
    _familyModes = new EnumMap<>(Family.class);
  }

  public Map<Family, Mode> getFamilyModes() {
    return _familyModes;
  }
}
