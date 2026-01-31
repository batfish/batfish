package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

/** A line of an {@link IpPrefixList}. */
public final class IpPrefixListLine implements Serializable {

  private final @Nonnull LineAction _action;
  private final @Nonnull SubRange _lengthRange;
  private final long _line;
  private final @Nonnull Prefix _prefix;

  public IpPrefixListLine(LineAction action, long line, Prefix prefix, SubRange lengthRange) {
    _action = action;
    _line = line;
    _prefix = prefix;
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

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }
}
