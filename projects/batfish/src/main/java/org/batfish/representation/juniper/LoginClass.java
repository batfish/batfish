package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * Vendor-specific model of a Junos {@code [edit system login class <name>]} block.
 *
 * <p>Only the {@code permissions} bit-list is modeled. Permissions are stored as open strings
 * rather than a fixed enum: Junos defines ~40 permission flags, each with a read-only "plain" form
 * (e.g. {@code interface}) and a read-write {@code -control} form (e.g. {@code interface-control}),
 * plus flags such as {@code all}, {@code view}, {@code configure}, {@code secret}, and {@code
 * maintenance}.
 */
public class LoginClass implements Serializable {

  private final String _name;

  private final Set<String> _permissions;

  public LoginClass(String name) {
    _name = name;
    _permissions = new TreeSet<>();
  }

  public String getName() {
    return _name;
  }

  /** The permission flags granted to this class. */
  public Set<String> getPermissions() {
    return _permissions;
  }
}
