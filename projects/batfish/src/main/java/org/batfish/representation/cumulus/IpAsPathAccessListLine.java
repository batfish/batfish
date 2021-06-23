package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AsPathAccessListLine;
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

  public AsPathAccessListLine toAsPathAccessListLine() {
    String regex = CumulusConversions.toJavaRegex(_regex);
    return new AsPathAccessListLine(_action, regex);
  }
}
