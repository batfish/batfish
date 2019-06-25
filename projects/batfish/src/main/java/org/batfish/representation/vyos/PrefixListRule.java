package org.batfish.representation.vyos;

import java.io.Serializable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

public class PrefixListRule implements Serializable {

  private LineAction _action;

  private String _description;

  private int _ge;

  private int _le;

  private Prefix _prefix;

  public PrefixListRule() {
    _ge = 0;
    _le = 32;
  }

  public LineAction getAction() {
    return _action;
  }

  public String getDescription() {
    return _description;
  }

  public SubRange getLengthRange() {
    if (_prefix.getPrefixLength() <= _ge && _ge <= _le) {
      return new SubRange(_ge, _le);
    } else {
      throw new BatfishException("Invalid length range restriction");
    }
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setGe(int ge) {
    _ge = ge;
    _le = Math.max(_ge, _le);
  }

  public void setLe(int le) {
    _le = le;
  }

  public void setPrefix(Prefix prefix) {
    _prefix = prefix;
    _ge = Math.max(_ge, _prefix.getPrefixLength());
    _le = Math.max(_ge, _le);
  }
}
