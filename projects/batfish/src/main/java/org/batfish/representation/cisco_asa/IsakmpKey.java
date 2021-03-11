package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IpSpace;

/** Stores the ISAKMP key which are not part of any keyring or RSA pub keys */
public class IsakmpKey implements Serializable {
  @Nonnull private IpSpace _address;
  @Nonnull private String _key;
  @Nonnull private IkeKeyType _ikeKeyType;

  public IsakmpKey(@Nonnull IpSpace address, @Nonnull String key, @Nonnull IkeKeyType ikeKeyType) {
    _address = address;
    _key = key;
    _ikeKeyType = ikeKeyType;
  }

  @Nonnull
  public IpSpace getAddress() {
    return _address;
  }

  @Nonnull
  public String getKey() {
    return _key;
  }

  @Nonnull
  public IkeKeyType getIkeKeyType() {
    return _ikeKeyType;
  }
}
