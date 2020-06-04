package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SwitchportMode;

/** Interface settings for the ethernet-switching family. */
@ParametersAreNonnullByDefault
public final class EthernetSwitching implements Serializable {

  public EthernetSwitching() {
    _vlanMembers = new LinkedList<>();
  }

  public @Nullable Integer getNativeVlan() {
    return _nativeVlan;
  }

  public void setNativeVlan(int nativeVlan) {
    _nativeVlan = nativeVlan;
  }

  public @Nullable SwitchportMode getSwitchportMode() {
    return _switchportMode;
  }

  public void setSwitchportMode(SwitchportMode switchportMode) {
    _switchportMode = switchportMode;
  }

  public @Nonnull List<VlanMember> getVlanMembers() {
    return _vlanMembers;
  }

  private @Nullable Integer _nativeVlan;
  private @Nullable SwitchportMode _switchportMode;
  private final @Nonnull List<VlanMember> _vlanMembers;
}
