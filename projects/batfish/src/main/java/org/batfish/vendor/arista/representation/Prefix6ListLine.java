package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.SubRange;

public class Prefix6ListLine implements Serializable {

  private LineAction _action;

  private SubRange _lengthRange;

  private Prefix6 _prefix;

  public Prefix6ListLine(LineAction action, Prefix6 prefix, SubRange lengthRange) {
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

  public Prefix6 getPrefix() {
    return _prefix;
  }
}
