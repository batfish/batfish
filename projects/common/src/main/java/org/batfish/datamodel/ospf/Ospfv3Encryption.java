package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Effective OSPFv3 IPsec ESP settings for an interface. */
@ParametersAreNonnullByDefault
public final class Ospfv3Encryption
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

  private static final String PROP_SPI =
      "spi";

  private static final String PROP_AUTH_TYPE =
      "authType";

  private static final String PROP_AUTH_KEY_TYPE =
      "authKeyType";

  private static final String PROP_AUTH_KEY =
      "authKey";

  private static final String PROP_ENCRYPTION_TYPE =
      "encryptionType";

  private static final String
      PROP_ENCRYPTION_KEY_TYPE =
          "encryptionKeyType";

  private static final String PROP_ENCRYPTION_KEY =
      "encryptionKey";

  @JsonCreator
  public Ospfv3Encryption(
      @JsonProperty(PROP_SPI)
          @Nullable Long spi,
      @JsonProperty(PROP_AUTH_TYPE)
          @Nullable AuthType authType,
      @JsonProperty(PROP_AUTH_KEY_TYPE)
          @Nullable KeyType authKeyType,
      @JsonProperty(PROP_AUTH_KEY)
          @Nullable String authKey,
      @JsonProperty(PROP_ENCRYPTION_TYPE)
          @Nullable EncryptionType encryptionType,
      @JsonProperty(PROP_ENCRYPTION_KEY_TYPE)
          @Nullable KeyType encryptionKeyType,
      @JsonProperty(PROP_ENCRYPTION_KEY)
          @Nullable String encryptionKey) {

    checkArgument(
        spi != null,
        "Missing OSPFv3 ESP SPI");

    checkArgument(
        spi >= 256L
            && spi <= 0xFFFFFFFFL,
        "Invalid OSPFv3 ESP SPI %s",
        spi);

    checkArgument(
        authType != null,
        "Missing OSPFv3 ESP authentication type");

    checkArgument(
        (authKeyType == null)
            == (authKey == null),
        "OSPFv3 ESP authentication key type and key must either both be present or both be absent");

    checkArgument(
        (encryptionKeyType == null)
            == (encryptionKey == null),
        "OSPFv3 ESP encryption key type and key must either both be present or both be absent");

    checkArgument(
        encryptionType != null
            || encryptionKey == null,
        "OSPFv3 ESP encryption key requires an encryption type");

    checkArgument(
        encryptionType != EncryptionType.NULL
            || encryptionKey == null,
        "OSPFv3 NULL ESP must not have an encryption key");

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

  @JsonProperty(PROP_SPI)
  public long getSpi() {
    return _spi;
  }

  @JsonProperty(PROP_AUTH_TYPE)
  public @Nonnull AuthType getAuthType() {
    return _authType;
  }

  @JsonProperty(PROP_AUTH_KEY_TYPE)
  public @Nullable KeyType getAuthKeyType() {
    return _authKeyType;
  }

  @JsonProperty(PROP_AUTH_KEY)
  public @Nullable String getAuthKey() {
    return _authKey;
  }

  @JsonProperty(PROP_ENCRYPTION_TYPE)
  public @Nullable EncryptionType
      getEncryptionType() {

    return _encryptionType;
  }

  @JsonProperty(PROP_ENCRYPTION_KEY_TYPE)
  public @Nullable KeyType
      getEncryptionKeyType() {

    return _encryptionKeyType;
  }

  @JsonProperty(PROP_ENCRYPTION_KEY)
  public @Nullable String getEncryptionKey() {
    return _encryptionKey;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Ospfv3Encryption)) {
      return false;
    }

    Ospfv3Encryption rhs =
        (Ospfv3Encryption) o;

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
