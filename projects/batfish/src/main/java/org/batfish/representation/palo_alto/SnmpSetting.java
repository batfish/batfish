package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents SNMP settings for a Palo Alto device */
@ParametersAreNonnullByDefault
public final class SnmpSetting implements Serializable {

  private final @Nonnull List<SnmpAccessSetting> _accessSettings;
  private @Nullable SnmpSystem _snmpSystem;

  public SnmpSetting() {
    _accessSettings = new ArrayList<>();
  }

  public void addAccessSetting(SnmpAccessSetting accessSetting) {
    _accessSettings.add(accessSetting);
  }

  public @Nonnull List<SnmpAccessSetting> getAccessSettings() {
    return _accessSettings;
  }

  @Nullable
  public SnmpSystem getSnmpSystem() {
    return _snmpSystem;
  }

  public void setSnmpSystem(@Nullable SnmpSystem snmpSystem) {
    _snmpSystem = snmpSystem;
  }
}
