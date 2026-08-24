package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Effective OSPFv3 IPsec AH authentication settings for an interface. */
@ParametersAreNonnullByDefault
public final class Ospfv3Authentication
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

  private static final String PROP_SPI =
      "spi";

  private static final String PROP_AUTH_TYPE =
      "authType";

  private static final String PROP_KEY_TYPE =
      "keyType";

  private static final String PROP_KEY =
      "key";

  @JsonCreator
  public Ospfv3Authentication(
      @JsonProperty(PROP_SPI)
          @Nullable Long spi,
      @JsonProperty(PROP_AUTH_TYPE)
          @Nullable AuthType authType,
      @JsonProperty(PROP_KEY_TYPE)
          @Nullable KeyType keyType,
      @JsonProperty(PROP_KEY)
          @Nullable String key) {

    checkArgument(
        spi != null,
        "Missing OSPFv3 authentication SPI");

    checkArgument(
        spi >= 256L
            && spi <= 0xFFFFFFFFL,
        "Invalid OSPFv3 authentication SPI %s",
        spi);

    checkArgument(
        authType != null,
        "Missing OSPFv3 authentication type");

    checkArgument(
        (keyType == null)
            == (key == null),
        "OSPFv3 authentication key type and key must either both be present or both be absent");

    _spi =
        spi;

    _authType =
        authType;

    _keyType =
        keyType;

    _key =
        key;
  }

  @JsonProperty(PROP_SPI)
  public long getSpi() {
    return _spi;
  }

  @JsonProperty(PROP_AUTH_TYPE)
  public @Nonnull AuthType getAuthType() {
    return _authType;
  }

  @JsonProperty(PROP_KEY_TYPE)
  public @Nullable KeyType getKeyType() {
    return _keyType;
  }

  @JsonProperty(PROP_KEY)
  public @Nullable String getKey() {
    return _key;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Ospfv3Authentication)) {
      return false;
    }

    Ospfv3Authentication rhs =
        (Ospfv3Authentication) o;

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
