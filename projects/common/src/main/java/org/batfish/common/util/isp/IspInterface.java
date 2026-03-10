package org.batfish.common.util.isp;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.Layer1Node;
import org.batfish.datamodel.InterfaceAddress;

/** Captures settings needed to create an interface on the ISP node */
final class IspInterface {
  private final @Nonnull String _name;
  private final @Nonnull InterfaceAddress _address;
  private final @Nonnull Layer1Node _layer1Remote;
  private final @Nullable Integer _vlanTag;

  public IspInterface(
      String name, InterfaceAddress address, Layer1Node layer1Remote, @Nullable Integer vlanTag) {
    _name = name;
    _address = address;
    _layer1Remote = layer1Remote;
    _vlanTag = vlanTag;
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

  public @Nullable Integer getVlanTag() {
    return _vlanTag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspInterface)) {
      return false;
    }
    IspInterface that = (IspInterface) o;
    return _name.equals(that._name)
        && _address.equals(that._address)
        && _layer1Remote.equals(that._layer1Remote)
        && Objects.equals(_vlanTag, that._vlanTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _address, _layer1Remote, _vlanTag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", _name)
        .add("address", _address)
        .add("layer1Remote", _layer1Remote)
        .add("vlanTag", _vlanTag)
        .toString();
  }
}
