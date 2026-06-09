package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * An SR-OS {@code policy-options community "<name>"} list, keyed by name. Holds the ordered {@code
 * member} values (e.g. {@code "65001:100"}). A policy {@code action community add ["<name>"]}
 * references this list by name.
 */
public final class Community implements Serializable {

  public Community(String name) {
    _name = name;
    _members = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** The ordered {@code member} values of this community list. */
  public @Nonnull List<String> getMembers() {
    return _members;
  }

  private final @Nonnull String _name;
  private final @Nonnull List<String> _members;
}
