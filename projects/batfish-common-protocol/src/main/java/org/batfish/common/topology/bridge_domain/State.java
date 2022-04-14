package org.batfish.common.topology.bridge_domain;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link Search} state, including information about the current L2 frame tag(s) and assigned VLAN
 * if any.
 *
 * <p>TODO: include VNI to support VNI translations.
 */
final class State {

  public static @Nonnull State of(@Nullable Integer outerTag, @Nullable Integer vlan) {
    return outerTag == null && vlan == null ? EMPTY : new State(outerTag, vlan);
  }

  public static @Nonnull State empty() {
    return EMPTY;
  }

  public @Nullable Integer getOuterTag() {
    return _outerTag;
  }

  public @Nullable Integer getVlan() {
    return _vlan;
  }

  // Internal implementation details

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o == null || getClass() != o.getClass()) {
      return false;
    }
    State state = (State) o;
    return Objects.equals(_outerTag, state._outerTag) && Objects.equals(_vlan, state._vlan);
  }

  @Override
  public int hashCode() {
    return 31 * (_outerTag == null ? 0 : _outerTag) + (_vlan == null ? 0 : _vlan);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .omitNullValues()
        .add("_outerTag", _outerTag)
        .add("_vlan", _vlan)
        .toString();
  }

  private static final State EMPTY = new State(null, null);

  private State(@Nullable Integer outerTag, @Nullable Integer vlan) {
    _outerTag = outerTag;
    _vlan = vlan;
  }

  private final @Nullable Integer _outerTag;
  private final @Nullable Integer _vlan;
}
