package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;

/** An entry in an Aruba AOS-CX IPv4 prefix list. */
public final class AosCxPrefixListEntry implements Serializable {

  public AosCxPrefixListEntry(
      long sequence,
      LineAction action,
      Prefix prefix,
      @Nullable Integer ge,
      @Nullable Integer le) {
    _sequence = sequence;
    _action = action;
    _prefix = prefix;
    _ge = ge;
    _le = le;
  }

  public long getSequence() {
    return _sequence;
  }

  public LineAction getAction() {
    return _action;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public @Nullable Integer getGe() {
    return _ge;
  }

  public @Nullable Integer getLe() {
    return _le;
  }

  private final long _sequence;
  private final LineAction _action;
  private final Prefix _prefix;
  private final @Nullable Integer _ge;
  private final @Nullable Integer _le;
}
