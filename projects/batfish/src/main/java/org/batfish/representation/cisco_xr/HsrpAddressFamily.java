package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Settings at the {@code router hsrp > interface > address-family} level. */
@ParametersAreNonnullByDefault
public final class HsrpAddressFamily implements Serializable {
  public enum Type {
    /** Config for address-family IPv4 */
    IPV4
  }

  public HsrpAddressFamily(Type type) {
    _type = type;
    _groups = new HashMap<>();
  }

  public @Nullable HsrpGroup getGroup(int groupNum) {
    return _groups.get(groupNum);
  }

  public @Nonnull HsrpGroup getOrCreateGroup(int groupNum) {
    return _groups.computeIfAbsent(groupNum, HsrpGroup::new);
  }

  public @Nonnull Map<Integer, HsrpGroup> getGroups() {
    return _groups;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  private final Map<Integer, HsrpGroup> _groups;
  private final Type _type;
}
