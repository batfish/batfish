package org.batfish.vendor.cisco_nxos.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.InterfaceAddress;

/** Describes the configuration for an IP address assigned to an {@link Interface}. */
@ParametersAreNonnullByDefault
public final class InterfaceAddressWithAttributes implements Serializable {
  public InterfaceAddressWithAttributes(InterfaceAddress address) {
    _address = address;
  }

  public @Nonnull InterfaceAddress getAddress() {
    return _address;
  }

  public int getRoutePreference() {
    return _routePreference;
  }

  public void setRoutePreference(@Nullable Integer routePreference) {
    _routePreference = firstNonNull(routePreference, 0);
  }

  public long getTag() {
    return _tag;
  }

  public void setTag(@Nullable Long tag) {
    _tag = firstNonNull(tag, 0L);
  }

  //////////////////////////////////////////////////
  ///// Private implementation details         /////
  //////////////////////////////////////////////////

  private final @Nonnull InterfaceAddress _address;
  private int _routePreference;
  private long _tag;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof InterfaceAddressWithAttributes)) {
      return false;
    }
    InterfaceAddressWithAttributes that = (InterfaceAddressWithAttributes) o;
    return _address.equals(that._address)
        && _routePreference == that._routePreference
        && _tag == that._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_address, _routePreference, _tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("address", _address)
        .add("routePreference", _routePreference)
        .add("tag", _tag)
        .toString();
  }
}
