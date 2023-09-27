package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.vendor.a10.representation.TrunkGroup.DEFAULT_TRUNK_TYPE;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Datamodel class representing a {@code trunk} {@link Interface}.
 *
 * <p>This class contains metadata regarding a {@code trunk}'s configuration. This metadata is not
 * explicitly configured under the {@code trunk} {@link Interface} itself, but instead within its
 * member {@link Interface}s.
 */
public final class TrunkInterface extends Interface {

  /** Interfaces that are members of this trunk. */
  public Set<InterfaceReference> getMembers() {
    return _members;
  }

  /** Number of members of this trunk that must be up for the trunk to be considered up. */
  public @Nullable Integer getPortsThreshold() {
    return _portsThreshold;
  }

  /**
   * The type for this trunk. This isn't explicitly configured on the trunk interface, but is a
   * property inherited from its link to its children interfaces.
   *
   * <p>If the type for this trunk is not explicitly configured in its children interface(s), then
   * it will have the default type.
   */
  public @Nonnull TrunkGroup.Type getTrunkTypeEffective() {
    return firstNonNull(_trunkType, DEFAULT_TRUNK_TYPE);
  }

  public void setPortsThreshold(Integer portsThreshold) {
    _portsThreshold = portsThreshold;
  }

  public TrunkInterface(int number, @Nullable TrunkGroup.Type trunkType) {
    super(Type.TRUNK, number);
    _trunkType = trunkType;
    _members = new HashSet<>();
  }

  private final @Nonnull Set<InterfaceReference> _members;
  private @Nullable Integer _portsThreshold;
  private final @Nullable TrunkGroup.Type _trunkType;
}
