package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * A Cisco IOS NTP authentication key defined via {@code ntp authentication-key <number> {md5 |
 * cmac-aes-128 | hmac-sha1 | hmac-sha2-256} <value>}.
 */
public class NtpAuthenticationKey implements Serializable {

  /** The hashing algorithm used to authenticate with an NTP key. */
  public enum HashAlgorithm {
    CMAC_AES_128,
    HMAC_SHA1,
    HMAC_SHA2_256,
    MD5
  }

  private final int _keyNumber;
  private @Nullable HashAlgorithm _hashAlgorithm;
  private @Nullable String _value;

  public NtpAuthenticationKey(int keyNumber) {
    _keyNumber = keyNumber;
  }

  /** The key number this authentication key is identified by. */
  public int getKeyNumber() {
    return _keyNumber;
  }

  /** The hashing algorithm used with this key. */
  public @Nullable HashAlgorithm getHashAlgorithm() {
    return _hashAlgorithm;
  }

  public void setHashAlgorithm(@Nullable HashAlgorithm hashAlgorithm) {
    _hashAlgorithm = hashAlgorithm;
  }

  /** The key value. */
  public @Nullable String getValue() {
    return _value;
  }

  public void setValue(@Nullable String value) {
    _value = value;
  }
}
