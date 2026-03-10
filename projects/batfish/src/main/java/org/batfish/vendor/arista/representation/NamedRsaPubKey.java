package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Used during IKE phase 1 negotiation for authentication */
public class NamedRsaPubKey implements Serializable {

  private @Nonnull String _name;
  private @Nullable Ip _address;
  private @Nullable String _key;

  public NamedRsaPubKey(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setName(@Nonnull String name) {
    _name = name;
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  public @Nullable String getKey() {
    return _key;
  }

  public void setKey(@Nullable String key) {
    _key = key;
  }
}
