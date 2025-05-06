package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a Juniper MPLS admin-group. Admin-groups are used to color links for constraint-based
 * routing.
 */
@ParametersAreNonnullByDefault
public final class AdminGroup implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull String _name;
  private final int _value;

  public AdminGroup(String name, int value) {
    _name = name;
    _value = value;
  }

  /** Returns the name of this admin-group. */
  public @Nonnull String getName() {
    return _name;
  }

  /** Returns the value of this admin-group (0-31). */
  public int getValue() {
    return _value;
  }
}
