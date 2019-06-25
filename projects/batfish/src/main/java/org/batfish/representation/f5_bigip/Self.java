package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * Configuration for a layer-3 endpoint on the device itself. Typically manifests as an IRB
 * interface for a particular VLAN.
 *
 * @see <a
 *     href="https://support.f5.com/kb/en-us/products/big-ip_ltm/manuals/product/tmos-routing-administration-11-6-0/5.html">Self
 *     IP Addresses</a>
 */
@ParametersAreNonnullByDefault
public final class Self implements Serializable {

  private @Nullable ConcreteInterfaceAddress _address;

  private final @Nonnull String _name;

  private @Nullable String _vlan;

  public Self(String name) {
    _name = name;
  }

  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getVlan() {
    return _vlan;
  }

  public void setAddress(@Nullable ConcreteInterfaceAddress address) {
    _address = address;
  }

  public void setVlan(@Nullable String vlan) {
    _vlan = vlan;
  }
}
