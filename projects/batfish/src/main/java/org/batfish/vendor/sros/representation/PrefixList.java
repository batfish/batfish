package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * An SR-OS {@code policy-options prefix-list}, keyed by name. Holds its match {@link
 * PrefixListEntry entries} (each a prefix + match type).
 */
public final class PrefixList implements Serializable {

  public PrefixList(String name) {
    _name = name;
    _entries = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** The entries of this prefix-list, in configuration order. */
  public @Nonnull List<PrefixListEntry> getEntries() {
    return _entries;
  }

  private final @Nonnull String _name;
  private final @Nonnull List<PrefixListEntry> _entries;
}
