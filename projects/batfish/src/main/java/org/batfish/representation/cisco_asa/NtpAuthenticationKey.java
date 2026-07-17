package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * A Cisco ASA NTP authentication key defined via {@code ntp authentication-key <key-id> {md5 | sha1
 * | sha256 | sha512 | cmac} <value>}.
 */
public class NtpAuthenticationKey implements Serializable {

  /** The hashing algorithm used to authenticate with an NTP key. */
  public enum HashAlgorithm {
    CMAC,
    MD5,
    SHA1,
    SHA256,
    SHA512
  }

  private final long _keyNumber;
  private @Nullable HashAlgorithm _hashAlgorithm;
  private @Nullable String _value;

  public NtpAuthenticationKey(long keyNumber) {
    _keyNumber = keyNumber;
  }

  /** The key number this authentication key is identified by. */
  public long getKeyNumber() {
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
