package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IpSpace;

/** Stores the ISAKMP key which are not part of any keyring or RSA pub keys */
public class IsakmpKey implements Serializable {
  private @Nonnull IpSpace _address;
  private @Nonnull String _key;
  private @Nonnull IkeKeyType _ikeKeyType;

  public IsakmpKey(@Nonnull IpSpace address, @Nonnull String key, @Nonnull IkeKeyType ikeKeyType) {
    _address = address;
    _key = key;
    _ikeKeyType = ikeKeyType;
  }

  public @Nonnull IpSpace getAddress() {
    return _address;
  }

  public @Nonnull String getKey() {
    return _key;
  }

  public @Nonnull IkeKeyType getIkeKeyType() {
    return _ikeKeyType;
  }
}
