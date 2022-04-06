package org.batfish.representation.cisco;

import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an AAA LDAP group */
@ParametersAreNonnullByDefault
public final class LdapServerGroup extends AaaServerGroup {

  public LdapServerGroup(String name) {
    super(name);
  }
}
