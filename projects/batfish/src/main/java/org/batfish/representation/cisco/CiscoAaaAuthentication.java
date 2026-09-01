package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Vendor-specific model of Cisco IOS {@code aaa authentication} configuration */
@ParametersAreNonnullByDefault
public final class CiscoAaaAuthentication implements Serializable {

  private @Nullable CiscoAaaAuthenticationLogin _login;

  public @Nullable CiscoAaaAuthenticationLogin getLogin() {
    return _login;
  }

  public void setLogin(@Nullable CiscoAaaAuthenticationLogin login) {
    _login = login;
  }
}
