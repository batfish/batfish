package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Palo Alto device-group, which */
@ParametersAreNonnullByDefault
public final class DeviceGroup extends PaloAltoConfiguration {
  private String _description;
  private final Set<String> _devices;
  private final String _name;

  public DeviceGroup(String name) {
    super();
    _devices = new HashSet<>();
    _name = name;
  }

  public void addDevice(String device) {
    _devices.add(device);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nonnull Set<String> getDevices() {
    return ImmutableSet.copyOf(_devices);
  }

  public String getName() {
    return _name;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
