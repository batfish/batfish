package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.LineAction;

public class IpAsPathAccessListLine implements Serializable {

  private static final long serialVersionUID = 1L;

  private LineAction _action;

  private String _regex;

  public IpAsPathAccessListLine(LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  public AsPathAccessListLine toAsPathAccessListLine() {
    AsPathAccessListLine line = new AsPathAccessListLine();
    line.setAction(_action);
    String regex = CiscoConfiguration.toJavaRegex(_regex);
    line.setRegex(regex);
    return line;
  }

  public LineAction getAction() {
    return _action;
  }
}
