package org.batfish.vendor.arista.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.vendor.arista.representation.Vlan.State.ACTIVE;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * All the configuration associated with a specific vlan object. {@code vlan 5}, not {@code
 * interface Vlan5}.
 */
@ParametersAreNonnullByDefault
public final class Vlan implements Serializable {
  public enum State {
    /** The switch forwards traffic on the VLAN (default). */
    ACTIVE,
    /** The switch blocks traffic on the VLAN. */
    SUSPEND
  }

  public Vlan(int id) {
    _id = id;
    _state = ACTIVE;
  }

  public int getId() {
    return _id;
  }

  public @Nullable String getName() {
    return _name;
  }

  public void setName(@Nullable String name) {
    if (name == null && _id == 1) {
      _name = "default";
    } else {
      _name = name;
    }
  }

  public @Nonnull State getState() {
    return _state;
  }

  public void setState(@Nullable State state) {
    _state = firstNonNull(state, State.ACTIVE);
  }

  public @Nullable String getTrunkGroup() {
    return _trunkGroup;
  }

  public void setTrunkGroup(@Nullable String trunkGroup) {
    _trunkGroup = trunkGroup;
  }

  private final int _id;
  private @Nullable String _name;
  private @Nonnull State _state;
  private @Nullable String _trunkGroup;
}
