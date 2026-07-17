package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * A Cisco NX-OS NTP authentication key defined via {@code ntp authentication-key <number> md5
 * <value> [<type>]}.
 */
public final class NtpAuthenticationKey implements Serializable {

  public NtpAuthenticationKey(int keyNumber) {
    _keyNumber = keyNumber;
  }

  public int getKeyNumber() {
    return _keyNumber;
  }

  public @Nullable String getMd5Value() {
    return _md5Value;
  }

  public void setMd5Value(@Nullable String md5Value) {
    _md5Value = md5Value;
  }

  private final int _keyNumber;
  private @Nullable String _md5Value;
}
