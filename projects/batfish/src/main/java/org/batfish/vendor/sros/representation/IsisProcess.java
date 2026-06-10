package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS {@code router "<name>" isis <instance>} process, keyed by its integer instance. Holds
 * the {@code system-id}, {@code area-address}(es), level-capability, and the ISIS-enabled
 * interfaces. The NET (used to build the VI {@link org.batfish.datamodel.isis.IsisProcess}) is the
 * area-address + system-id + an {@code 00} N-selector. Only the subset needed to form adjacencies
 * and compute IS-IS routes is modeled.
 */
public final class IsisProcess implements Serializable {

  /** IS-IS routing level capability. */
  public enum LevelCapability {
    LEVEL_1,
    LEVEL_2,
    LEVEL_1_2;
  }

  public IsisProcess(int instance) {
    _instance = instance;
    _areaAddresses = new ArrayList<>();
    _interfaces = new LinkedHashMap<>();
  }

  public int getInstance() {
    return _instance;
  }

  /**
   * Whether the process is {@code admin-state enable}; defaults true when admin-state is absent.
   */
  public boolean getAdminStateEnable() {
    return _adminStateEnable;
  }

  public void setAdminStateEnable(boolean adminStateEnable) {
    _adminStateEnable = adminStateEnable;
  }

  /** The {@code system-id} (e.g. {@code 0100.1000.0001}), or {@code null} if unset. */
  public @Nullable String getSystemId() {
    return _systemId;
  }

  public void setSystemId(@Nullable String systemId) {
    _systemId = systemId;
  }

  /**
   * The {@code area-address}(es) (NSAP area, e.g. {@code 49.0001}); the first is used for the NET.
   */
  public @Nonnull List<String> getAreaAddresses() {
    return _areaAddresses;
  }

  /** The {@code level-capability}; defaults to {@link LevelCapability#LEVEL_2} when unset. */
  public @Nonnull LevelCapability getLevelCapability() {
    return _levelCapability;
  }

  public void setLevelCapability(LevelCapability levelCapability) {
    _levelCapability = levelCapability;
  }

  /** ISIS-enabled interfaces, keyed by the router-interface name. */
  public @Nonnull Map<String, IsisProcessInterface> getInterfaces() {
    return _interfaces;
  }

  private final int _instance;
  private boolean _adminStateEnable = true;
  private @Nullable String _systemId;
  private final @Nonnull List<String> _areaAddresses;
  private @Nonnull LevelCapability _levelCapability = LevelCapability.LEVEL_2;
  private final @Nonnull Map<String, IsisProcessInterface> _interfaces;
}
