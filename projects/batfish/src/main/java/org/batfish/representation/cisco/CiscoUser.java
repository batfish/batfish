package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Vendor-specific model of a Cisco IOS locally-configured {@code username} */
@ParametersAreNonnullByDefault
public final class CiscoUser implements Serializable {

  private final @Nonnull String _name;
  private @Nullable String _password;
  private @Nullable String _role;

  public CiscoUser(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getPassword() {
    return _password;
  }

  public void setPassword(@Nullable String password) {
    _password = password;
  }

  public @Nullable String getRole() {
    return _role;
  }

  public void setRole(@Nullable String role) {
    _role = role;
  }
}
