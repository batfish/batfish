package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/**
 * A line of an {@link IpAsPathAccessList}.
 *
 * <p>A route's as-path attribute must be matched by the this line's {@link #getRegex} to be matched
 * by this line.
 */
public final class IpAsPathAccessListLine implements Serializable {
  private final @Nonnull LineAction _action;
  private final @Nonnull String _regex;
  private final long _line;

  public IpAsPathAccessListLine(LineAction action, long line, String regex) {
    _action = action;
    _line = line;
    _regex = regex;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull String getRegex() {
    return _regex;
  }

  public long getLine() {
    return _line;
  }
}
