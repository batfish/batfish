package org.batfish.representation.frr;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/**
 * A line of an {@link BgpCommunityListExpanded}.
 *
 * <p>A route's community attribute string rendering must contain a substring matched by {@link
 * #getRegex} to be matched by this line.
 */
public final class BgpCommunityListExpandedLine implements Serializable {
  private final @Nonnull LineAction _action;
  private final @Nonnull String _regex;

  public BgpCommunityListExpandedLine(LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull String getRegex() {
    return _regex;
  }
}
