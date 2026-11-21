package org.batfish.representation.frr;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

/** Represents one line of a Cumulus AS-path access list. */
public class BgpAsPathAccessListLine implements Serializable {
  private final @Nonnull LineAction _action;
  private final String _regex;

  public BgpAsPathAccessListLine(@Nonnull LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public String getRegex() {
    return _regex;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BgpAsPathAccessListLine)) {
      return false;
    }
    BgpAsPathAccessListLine that = (BgpAsPathAccessListLine) o;
    return _action == that._action && _regex.equals(that._regex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action.ordinal(), _regex);
  }
}
