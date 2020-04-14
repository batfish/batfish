package org.batfish.representation.arista;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.LineAction;

@ParametersAreNonnullByDefault
public class IpAsPathAccessListLine implements Serializable {

  @Nonnull private LineAction _action;

  @Nonnull private String _regex;

  public IpAsPathAccessListLine(LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  public AsPathAccessListLine toAsPathAccessListLine() {
    String regex = AristaConfiguration.toJavaRegex(_regex);
    return new AsPathAccessListLine(_action, regex);
  }
}
