package org.batfish.vendor.nec_ix.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;

/** One entry of an {@code ip access-list}. */
public final class NecIxAccessListLine implements Serializable {

  private final @Nullable Long _sequence;
  private final @Nonnull LineAction _action;
  private final @Nonnull HeaderSpace _headerSpace;
  private final @Nonnull String _text;

  public NecIxAccessListLine(
      @Nullable Long sequence, LineAction action, HeaderSpace headerSpace, String text) {
    _sequence = sequence;
    _action = action;
    _headerSpace = headerSpace;
    _text = text;
  }

  /** The sequence number, only present for lists in sequence mode. */
  public @Nullable Long getSequence() {
    return _sequence;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  /** The packets this entry matches. */
  public @Nonnull HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  /** The entry as written in the configuration, without {@code ip access-list <name>}. */
  public @Nonnull String getText() {
    return _text;
  }
}
