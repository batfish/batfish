package org.batfish.vendor.nec_ix.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

/** An {@code ip prefix-list}. Entries are evaluated in sequence-number order. */
public final class NecIxPrefixList implements Serializable {

  /** One {@code ip prefix-list <name> <seq> ...} entry. */
  public static final class Line implements Serializable {
    private final @Nonnull LineAction _action;
    private final @Nonnull Prefix _prefix;
    private final @Nonnull SubRange _lengthRange;

    public Line(LineAction action, Prefix prefix, SubRange lengthRange) {
      _action = action;
      _prefix = prefix;
      _lengthRange = lengthRange;
    }

    public @Nonnull LineAction getAction() {
      return _action;
    }

    public @Nonnull Prefix getPrefix() {
      return _prefix;
    }

    /** The prefix lengths matched, from {@code min} and {@code max}. */
    public @Nonnull SubRange getLengthRange() {
      return _lengthRange;
    }
  }

  private final @Nonnull String _name;
  private final @Nonnull SortedMap<Integer, Line> _lines;

  public NecIxPrefixList(String name) {
    _name = name;
    _lines = new TreeMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Entries keyed by sequence number. */
  public @Nonnull SortedMap<Integer, Line> getLines() {
    return _lines;
  }
}
