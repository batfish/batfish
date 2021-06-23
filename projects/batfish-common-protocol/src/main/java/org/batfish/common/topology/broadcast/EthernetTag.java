package org.batfish.common.topology.broadcast;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Objects;
import javax.annotation.Nullable;

/** Represents an optional 802.1q tag in an Ethernet frame. */
public final class EthernetTag {
  public static EthernetTag tagged(int vlan) {
    checkArgument(vlan >= 0 && vlan <= 4095, "Invalid tag: %s", vlan);
    return new EthernetTag(vlan);
  }

  public static EthernetTag untagged() {
    return new EthernetTag(null);
  }

  public boolean hasTag() {
    return _tag != null;
  }

  public int getTag() {
    checkState(_tag != null, "Cannot get tag from untagged frame");
    return _tag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof EthernetTag)) {
      return false;
    }
    EthernetTag that = (EthernetTag) o;
    return Objects.equals(_tag, that._tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(EthernetTag.class, _tag);
  }

  private EthernetTag(@Nullable Integer tag) {
    _tag = tag;
  }

  private final @Nullable Integer _tag;
}
