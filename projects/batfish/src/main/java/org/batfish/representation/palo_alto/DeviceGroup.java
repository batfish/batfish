package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a Palo Alto device-group, which contains some regular device configuration along with
 * some device-group specific configuration.
 *
 * <p>Device-group configuration is inherited by any devices associated with it.
 */
@ParametersAreNonnullByDefault
public final class DeviceGroup extends PaloAltoConfiguration {
  private @Nullable String _description;
  private final Set<String> _devices;
  private final String _name;

  /** The parent {@link DeviceGroup}. */
  private @Nullable String _parentDg;

  /** Map of Device name to set of Vsyses */
  private final Map<String, Set<String>> _vsys;

  public DeviceGroup(String name) {
    super();
    _devices = new HashSet<>();
    _vsys = new HashMap<>();
    _name = name;
  }

  public void addDevice(String device) {
    _devices.add(device);
  }

  public void addVsys(String device, String vsys) {
    Set<String> vsysSet = _vsys.computeIfAbsent(device, d -> new HashSet<>());
    vsysSet.add(vsys);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nonnull Set<String> getDevices() {
    return ImmutableSet.copyOf(_devices);
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getParentDg() {
    return _parentDg;
  }

  public void setParentDg(@Nullable String parentDg) {
    _parentDg = parentDg;
  }

  /** Return map of device name to set of vsys names, for vsys associated with this device-group. */
  public @Nonnull Map<String, Set<String>> getVsys() {
    return _vsys;
  }
}
