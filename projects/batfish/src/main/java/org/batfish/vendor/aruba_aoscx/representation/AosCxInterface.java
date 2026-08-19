package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Vendor-specific representation of an Aruba AOS-CX interface. */
public final class AosCxInterface implements Serializable {

  public AosCxInterface(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getEnabled() {
    return _enabled;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  public void setAddress(ConcreteInterfaceAddress address) {
    _address = address;
  }

  private final @Nonnull String _name;
  private @Nullable Boolean _enabled;
  private @Nullable ConcreteInterfaceAddress _address;
}
