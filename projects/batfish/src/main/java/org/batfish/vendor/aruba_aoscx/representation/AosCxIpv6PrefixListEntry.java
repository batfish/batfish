package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix6;

/** One entry in an Aruba AOS-CX IPv6 prefix list. */
public final class AosCxIpv6PrefixListEntry
    implements Serializable {

  public AosCxIpv6PrefixListEntry(
      long sequence,
      LineAction action,
      Prefix6 prefix,
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

  public Prefix6 getPrefix() {
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
  private final Prefix6 _prefix;
  private final @Nullable Integer _ge;
  private final @Nullable Integer _le;
}
