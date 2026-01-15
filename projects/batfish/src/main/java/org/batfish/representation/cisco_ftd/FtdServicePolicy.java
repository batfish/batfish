package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Cisco FTD service-policy line.
 */
public class FtdServicePolicy implements Serializable {
  public enum Scope {
    GLOBAL,
    INTERFACE,
    UNKNOWN
  }

  private final @Nonnull String _policyMapName;
  private final @Nonnull Scope _scope;
  private final @Nullable String _interfaceName;

  public FtdServicePolicy(
      @Nonnull String policyMapName, @Nonnull Scope scope, @Nullable String interfaceName) {
    _policyMapName = policyMapName;
    _scope = scope;
    _interfaceName = interfaceName;
  }

  public @Nonnull String getPolicyMapName() {
    return _policyMapName;
  }

  public @Nonnull Scope getScope() {
    return _scope;
  }

  public @Nullable String getInterfaceName() {
    return _interfaceName;
  }
}
