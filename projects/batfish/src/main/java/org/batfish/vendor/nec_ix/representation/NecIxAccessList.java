package org.batfish.vendor.nec_ix.representation;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** An {@code ip access-list}: a named, ordered list of packet-matching entries. */
public final class NecIxAccessList implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull List<NecIxAccessListLine> _lines;

  public NecIxAccessList(String name) {
    _name = name;
    _lines = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Entries in the order they were configured. */
  public @Nonnull List<NecIxAccessListLine> getLines() {
    return _lines;
  }

  /**
   * Entries in evaluation order. A list in sequence mode is evaluated in sequence-number order;
   * otherwise entries are "evaluated in the order they were registered".
   */
  public @Nonnull List<NecIxAccessListLine> getOrderedLines() {
    return _lines.stream()
        .sorted(comparing(NecIxAccessListLine::getSequence, nullsLast(naturalOrder())))
        .collect(ImmutableList.toImmutableList());
  }
}
