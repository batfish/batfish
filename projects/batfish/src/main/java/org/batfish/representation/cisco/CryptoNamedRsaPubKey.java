package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class CryptoNamedRsaPubKey implements Serializable {
  private static final long serialVersionUID = 1L;
  @Nonnull private String _name;
  @Nullable private Ip _address;
  @Nullable private String _key;

  public CryptoNamedRsaPubKey(@Nonnull String name) {
    _name = name;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public void setName(@Nonnull String name) {
    _name = name;
  }

  @Nullable
  public Ip getAddress() {
    return _address;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  @Nullable
  public String getKey() {
    return _key;
  }

  public void setKey(@Nullable String key) {
    _key = key;
  }
}
