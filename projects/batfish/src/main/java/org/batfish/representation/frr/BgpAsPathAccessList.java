package org.batfish.representation.frr;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Cumulus AS-path access list. */
@ParametersAreNonnullByDefault
public class BgpAsPathAccessList implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull SortedMap<Long, BgpAsPathAccessListLine> _lines;

  public BgpAsPathAccessList(String name) {
    _name = name;
    _lines = new TreeMap<>();
  }

  /** The lines of this list, by sequence number. */
  public @Nonnull SortedMap<Long, BgpAsPathAccessListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
