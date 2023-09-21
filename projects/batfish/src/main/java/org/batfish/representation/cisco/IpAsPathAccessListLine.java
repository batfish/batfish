package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.LineAction;

@ParametersAreNonnullByDefault
public class IpAsPathAccessListLine implements Serializable {

  private @Nonnull LineAction _action;

  private @Nonnull String _regex;

  public IpAsPathAccessListLine(LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  public AsPathAccessListLine toAsPathAccessListLine() {
    String regex = CiscoConversions.toJavaRegex(_regex);
    return new AsPathAccessListLine(_action, regex);
  }
}
