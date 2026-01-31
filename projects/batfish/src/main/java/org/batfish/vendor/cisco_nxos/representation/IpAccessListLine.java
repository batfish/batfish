package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** A line of an {@link IpAccessList}. */
public abstract class IpAccessListLine implements Serializable {

  private final long _line;
  private final @Nonnull String _text;

  protected IpAccessListLine(long line, String text) {
    _line = line;
    _text = text.trim();
  }

  public abstract <T> T accept(IpAccessListLineVisitor<T> visitor);

  public long getLine() {
    return _line;
  }

  public @Nonnull String getText() {
    return _text;
  }
}
