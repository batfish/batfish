package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A FastPath usergroup: a named user class associated with one or more taskgroups ({@code usergroup
 * "<name>"} followed by {@code taskgroup "<name>"}).
 */
public final class Usergroup implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull List<String> _taskgroups;

  public Usergroup(String name) {
    _name = name;
    _taskgroups = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull List<String> getTaskgroups() {
    return _taskgroups;
  }
}
