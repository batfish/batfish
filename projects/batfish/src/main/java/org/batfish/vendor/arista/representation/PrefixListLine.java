package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

public class PrefixListLine implements Serializable {

  private final @Nonnull LineAction _action;

  private final @Nonnull SubRange _lengthRange;

  private final @Nonnull Prefix _prefix;

  private final long _seq;

  public PrefixListLine(
      long seq, @Nonnull LineAction action, @Nonnull Prefix prefix, @Nonnull SubRange lengthRange) {
    _action = action;
    _prefix = prefix;
    _lengthRange = lengthRange;
    _seq = seq;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull SubRange getLengthRange() {
    return _lengthRange;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  public long getSeq() {
    return _seq;
  }
}
