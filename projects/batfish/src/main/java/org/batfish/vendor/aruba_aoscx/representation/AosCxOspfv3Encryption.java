package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Vendor-specific AOS-CX OSPFv3 IPsec ESP settings. */
@ParametersAreNonnullByDefault
public final class AosCxOspfv3Encryption
    implements Serializable {

  public enum AuthType {
    MD5,
    SHA1
  }

  public enum EncryptionType {
    DES,
    THREE_DES,
    AES,
    NULL
  }

  public enum KeyType {
    PLAINTEXT,
    HEX_STRING,
    CIPHERTEXT
  }

  public AosCxOspfv3Encryption(
      long spi,
      AuthType authType,
      @Nullable KeyType authKeyType,
      @Nullable String authKey,
      @Nullable EncryptionType encryptionType,
      @Nullable KeyType encryptionKeyType,
      @Nullable String encryptionKey) {

    _spi =
        spi;

    _authType =
        authType;

    _authKeyType =
        authKeyType;

    _authKey =
        authKey;

    _encryptionType =
        encryptionType;

    _encryptionKeyType =
        encryptionKeyType;

    _encryptionKey =
        encryptionKey;
  }

  public long getSpi() {
    return _spi;
  }

  public @Nonnull AuthType getAuthType() {
    return _authType;
  }

  public @Nullable KeyType getAuthKeyType() {
    return _authKeyType;
  }

  public @Nullable String getAuthKey() {
    return _authKey;
  }

  public @Nullable EncryptionType
      getEncryptionType() {

    return _encryptionType;
  }

  public @Nullable KeyType
      getEncryptionKeyType() {

    return _encryptionKeyType;
  }

  public @Nullable String getEncryptionKey() {
    return _encryptionKey;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof AosCxOspfv3Encryption)) {
      return false;
    }

    AosCxOspfv3Encryption rhs =
        (AosCxOspfv3Encryption) o;

    return _spi == rhs._spi
        && _authType == rhs._authType
        && _authKeyType == rhs._authKeyType
        && Objects.equals(
            _authKey,
            rhs._authKey)
        && _encryptionType == rhs._encryptionType
        && _encryptionKeyType
            == rhs._encryptionKeyType
        && Objects.equals(
            _encryptionKey,
            rhs._encryptionKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _spi,
        _authType,
        _authKeyType,
        _authKey,
        _encryptionType,
        _encryptionKeyType,
        _encryptionKey);
  }

  private final long _spi;
  private final @Nonnull AuthType _authType;
  private final @Nullable KeyType _authKeyType;
  private final @Nullable String _authKey;
  private final @Nullable EncryptionType
      _encryptionType;
  private final @Nullable KeyType
      _encryptionKeyType;
  private final @Nullable String _encryptionKey;
}
