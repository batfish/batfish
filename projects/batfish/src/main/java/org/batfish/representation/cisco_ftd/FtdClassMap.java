package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a Cisco FTD class-map stanza. */
public class FtdClassMap implements Serializable {
  private final @Nonnull String _name;
  private @Nullable String _type;
  private final @Nonnull List<String> _matchLines;
  private final @Nonnull List<String> _accessListReferences;

  public FtdClassMap(@Nonnull String name) {
    _name = name;
    _matchLines = new ArrayList<>();
    _accessListReferences = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getType() {
    return _type;
  }

  public void setType(@Nullable String type) {
    _type = type;
  }

  public @Nonnull List<String> getMatchLines() {
    return _matchLines;
  }

  public void addMatchLine(@Nonnull String line) {
    _matchLines.add(line);
  }

  public @Nonnull List<String> getAccessListReferences() {
    return _accessListReferences;
  }

  public void addAccessListReference(@Nonnull String name) {
    _accessListReferences.add(name);
  }
}
