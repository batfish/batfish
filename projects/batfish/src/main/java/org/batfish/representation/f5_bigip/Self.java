package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.InterfaceAddress;

@ParametersAreNonnullByDefault
public final class Self implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable InterfaceAddress _address;

  private final @Nonnull String _name;

  private @Nullable String _vlan;

  public Self(String name) {
    _name = name;
  }

  public @Nullable InterfaceAddress getAddress() {
    return _address;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getVlan() {
    return _vlan;
  }

  public void setAddress(@Nullable InterfaceAddress address) {
    _address = address;
  }

  public void setVlan(@Nullable String vlan) {
    _vlan = vlan;
  }
}
