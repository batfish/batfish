package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/**
 * A line of an {@link IpCommunityListExpanded}.
 *
 * <p>A route's community attribute string rendering must contain a substring matched by {@link
 * #getRegex} to be matched by this line.
 */
public final class IpCommunityListExpandedLine implements Serializable {
  private final @Nonnull LineAction _action;
  private final @Nonnull String _regex;
  private final long _line;

  public IpCommunityListExpandedLine(LineAction action, long line, String regex) {
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
