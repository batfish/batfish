package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Settings at the {@code router hsrp > interface > address-family > hsrp group-num} level. */
@ParametersAreNonnullByDefault
public final class HsrpGroup implements Serializable {
  public HsrpGroup(int number) {
    _number = number;
    _interfaceTracks = new HashMap<>();
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  public @Nonnull Map<String, HsrpInterfaceTrack> getInterfaceTracks() {
    return Collections.unmodifiableMap(_interfaceTracks);
  }

  public void setInterfaceTrack(String name, @Nullable Integer decrementPriority) {
    HsrpInterfaceTrack track = new HsrpInterfaceTrack(name);
    track.setDecrementPriority(decrementPriority);
    _interfaceTracks.put(track.getName(), track);
  }

  public int getNumber() {
    return _number;
  }

  public @Nullable Boolean getPreempt() {
    return _preempt;
  }

  public void setPreempt(@Nullable Boolean preempt) {
    _preempt = preempt;
  }

  public @Nullable Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  private @Nullable Ip _address;
  private final Map<String, HsrpInterfaceTrack> _interfaceTracks;
  private final int _number;
  private @Nullable Boolean _preempt;
  private @Nullable Integer _priority;
}
