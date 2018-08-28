package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.LineAction;

@ParametersAreNonnullByDefault
public class IpAsPathAccessListLine implements Serializable {

  private static final long serialVersionUID = 1L;

  @Nonnull private LineAction _action;

  @Nonnull private String _regex;

  public IpAsPathAccessListLine(LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  public AsPathAccessListLine toAsPathAccessListLine() {
    String regex = CiscoConfiguration.toJavaRegex(_regex);
    return new AsPathAccessListLine(_action, regex);
  }
}
