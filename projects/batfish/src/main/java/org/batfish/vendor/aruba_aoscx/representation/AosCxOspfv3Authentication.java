package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Vendor-specific AOS-CX OSPFv3 IPsec AH authentication settings. */
@ParametersAreNonnullByDefault
public final class AosCxOspfv3Authentication
    implements Serializable {

  public enum AuthType {
    MD5,
    SHA1
  }

  public enum KeyType {
    PLAINTEXT,
    HEX_STRING,
    CIPHERTEXT
  }

  public AosCxOspfv3Authentication(
      long spi,
      AuthType authType,
      @Nullable KeyType keyType,
      @Nullable String key) {

    _spi = spi;
    _authType = authType;
    _keyType = keyType;
    _key = key;
  }

  public long getSpi() {
    return _spi;
  }

  public @Nonnull AuthType getAuthType() {
    return _authType;
  }

  public @Nullable KeyType getKeyType() {
    return _keyType;
  }

  public @Nullable String getKey() {
    return _key;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o
        instanceof AosCxOspfv3Authentication)) {
      return false;
    }

    AosCxOspfv3Authentication rhs =
        (AosCxOspfv3Authentication) o;

    return _spi == rhs._spi
        && _authType == rhs._authType
        && _keyType == rhs._keyType
        && Objects.equals(
            _key,
            rhs._key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _spi,
        _authType,
        _keyType,
        _key);
  }

  private final long _spi;
  private final @Nonnull AuthType _authType;
  private final @Nullable KeyType _keyType;
  private final @Nullable String _key;
}
