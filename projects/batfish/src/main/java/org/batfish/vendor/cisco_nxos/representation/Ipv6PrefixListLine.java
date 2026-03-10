package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.SubRange;

/** A line of an {@link Ipv6PrefixList}. */
public final class Ipv6PrefixListLine implements Serializable {

  private final @Nonnull LineAction _action;
  private final @Nonnull SubRange _lengthRange;
  private final long _line;
  private final @Nonnull Prefix6 _prefix6;

  public Ipv6PrefixListLine(LineAction action, long line, Prefix6 prefix6, SubRange lengthRange) {
    _action = action;
    _line = line;
    _prefix6 = prefix6;
    _lengthRange = lengthRange;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull SubRange getLengthRange() {
    return _lengthRange;
  }

  public long getLine() {
    return _line;
  }

  public @Nonnull Prefix6 getPrefix6() {
    return _prefix6;
  }
}
