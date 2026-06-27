package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SwitchportMode;

/** Interface settings for the {@code family bridge} L2 switching family. */
@ParametersAreNonnullByDefault
public final class BridgeSwitching implements Serializable {

  public BridgeSwitching() {
    _vlanMembers = new LinkedList<>();
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

  private @Nullable SwitchportMode _switchportMode;
  private final @Nonnull List<VlanMember> _vlanMembers;
}
