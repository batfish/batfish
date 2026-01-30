package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents SNMP system settings for a Palo Alto device */
@ParametersAreNonnullByDefault
public final class SnmpSystem implements Serializable {

  private @Nullable String _contact;
  private @Nullable String _location;
  private boolean _sendEventSpecificTraps;

  public @Nullable String getContact() {
    return _contact;
  }

  public void setContact(@Nullable String contact) {
    _contact = contact;
  }

  public @Nullable String getLocation() {
    return _location;
  }

  public void setLocation(@Nullable String location) {
    _location = location;
  }

  public boolean getSendEventSpecificTraps() {
    return _sendEventSpecificTraps;
  }

  public void setSendEventSpecificTraps(boolean sendEventSpecificTraps) {
    _sendEventSpecificTraps = sendEventSpecificTraps;
  }
}
