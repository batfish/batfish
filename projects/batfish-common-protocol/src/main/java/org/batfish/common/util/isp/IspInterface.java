package org.batfish.common.util.isp;

import com.google.common.base.MoreObjects;
import org.batfish.common.topology.Layer1Node;
import org.batfish.datamodel.InterfaceAddress;

import javax.annotation.Nonnull;
import java.util.Objects;

/** Captures settings needed to create an interface on the ISP node */
final class IspInterface {
  @Nonnull private final String _name;
  @Nonnull private final InterfaceAddress _address;
  @Nonnull private final Layer1Node _layer1Remote;

  public IspInterface(String name, InterfaceAddress address, Layer1Node layer1Remote) {
    _name = name;
    _address = address;
    _layer1Remote = layer1Remote;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull InterfaceAddress getAddress() {
    return _address;
  }

  public @Nonnull Layer1Node getLayer1Remote() {
    return _layer1Remote;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IspInterface)) {
      return false;
    }
    IspInterface that = (IspInterface) o;
    return _name.equals(that._name)
        && _address.equals(that._address)
        && _layer1Remote.equals(that._layer1Remote);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _address, _layer1Remote);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", _name)
        .add("address", _address)
        .add("layer1Remote", _layer1Remote)
        .toString();
  }
}
