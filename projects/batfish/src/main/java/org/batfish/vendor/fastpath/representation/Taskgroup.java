package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A FastPath taskgroup: a named set of CLI task permissions ({@code taskgroup "<name>"} followed by
 * one or more {@code task ...} lines). Each {@code task} line grants a set of {@link
 * TaskPermission}s on a single {@link TaskComponent}.
 */
public final class Taskgroup implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull Map<TaskComponent, Set<TaskPermission>> _tasks;

  public Taskgroup(String name) {
    _name = name;
    _tasks = new EnumMap<>(TaskComponent.class);
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Map<TaskComponent, Set<TaskPermission>> getTasks() {
    return _tasks;
  }
}
