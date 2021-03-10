package org.batfish.representation.fortios;

public interface FortiosRenameableObject {
  String getName();

  /** Batfish-internal UUID. Persists across object rename. */
  String getBatfishUUID();

  void setName(String name);

  void setBatfishUUID(String uuid);
}
