package org.batfish.representation.fortios;

/**
 * Interface for FortiOS objects that may be renamed. Contains both a mutable name and an internal
 * UUID which persists across renames and structure edits.
 */
public interface FortiosRenameableObject {
  String getName();

  /** Batfish-internal UUID. Persists across object rename. */
  BatfishUUID getBatfishUUID();

  void setName(String name);
}
