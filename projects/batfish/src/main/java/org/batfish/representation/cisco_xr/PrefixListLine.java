package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

public class PrefixListLine implements Serializable {

  private LineAction _action;

  private SubRange _lengthRange;

  private Prefix _prefix;

  public PrefixListLine(LineAction action, Prefix prefix, SubRange lengthRange) {
    _action = action;
    _prefix = prefix;
    _lengthRange = lengthRange;
  }

  public LineAction getAction() {
    return _action;
  }

  public SubRange getLengthRange() {
    return _lengthRange;
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
