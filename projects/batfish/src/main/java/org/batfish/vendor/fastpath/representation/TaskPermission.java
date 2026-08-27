package org.batfish.vendor.fastpath.representation;

/** A permission that a FastPath {@code task} line can grant on a {@link TaskComponent}. */
public enum TaskPermission {
  READ,
  WRITE,
  EXECUTE,
  DEBUG
}
