package org.batfish.representation.fortios;

public interface FortiosRenameableObject {
  String getName();

  String getUUID();

  void setName(String name);

  void setUUID(String uuid);
}
