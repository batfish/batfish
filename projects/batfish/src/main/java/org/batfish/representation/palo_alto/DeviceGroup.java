package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Palo Alto device-group, which */
@ParametersAreNonnullByDefault
public final class DeviceGroup extends PaloAltoConfiguration {
  private final String _name;
  private final Set<String> _devices;

  DeviceGroup(String name) {
    super();
    _devices = new HashSet<>();
    _name = name;
  }

  public void addDevice(String device) {
    _devices.add(device);
  }

  public Set<String> getDevices() {
    return ImmutableSet.copyOf(_devices);
  }

  public String getName() {
    return _name;
  }
}
