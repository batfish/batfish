package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing zone configuration */
public final class Zone implements FortiosRenameableObject, InterfaceOrZone, Serializable {

  public enum IntrazoneAction {
    ALLOW,
    DENY,
  }

  public static final IntrazoneAction DEFAULT_INTRAZONE_ACTION = IntrazoneAction.DENY;

  @Override
  public <T> T accept(InterfaceOrZoneVisitor<T> visitor) {
    return visitor.visitZone(this);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable IntrazoneAction getIntrazone() {
    return _intrazone;
  }

  /**
   * Get the effective intrazone behavior for the zone, inferring the value even if not explicitly
   * configured.
   */
  public IntrazoneAction getIntrazoneEffective() {
    return firstNonNull(_intrazone, DEFAULT_INTRAZONE_ACTION);
  }

  public @Nonnull Set<String> getInterface() {
    return _interface;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public BatfishUUID getBatfishUUID() {
    return _uuid;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIntrazone(IntrazoneAction intrazone) {
    _intrazone = intrazone;
  }

  @Override
  public void setName(String name) {
    _name = name;
  }

  public Zone(String name, BatfishUUID uuid) {
    _name = name;
    _uuid = uuid;
    _interface = new HashSet<>();
  }

  private @Nullable String _description;
  private final @Nonnull Set<String> _interface;
  private @Nullable IntrazoneAction _intrazone;
  private @Nonnull String _name;
  private final @Nonnull BatfishUUID _uuid;
}
