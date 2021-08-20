package org.batfish.vendor.check_point_management;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;

/** A CheckPoint management server. */
public final class Server implements Serializable {

  public Server(Map<String, ManagementDomain> domains, String name) {
    _domains = domains;
    _name = name;
  }

  public @Nonnull Map<String, ManagementDomain> getDomains() {
    return _domains;
  }

  public @Nonnull String getName() {
    return _name;
  }

  private final @Nonnull Map<String, ManagementDomain> _domains;
  private final @Nonnull String _name;
}
