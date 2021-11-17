package org.batfish.representation.frr;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/** Represents one line of a Cumulus AS-path access list. */
public class IpAsPathAccessListLine implements Serializable {
  private final @Nonnull LineAction _action;
  private final String _regex;

  public IpAsPathAccessListLine(@Nonnull LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  @Nonnull
  public LineAction getAction() {
    return _action;
  }

  public String getRegex() {
    return _regex;
  }
}
