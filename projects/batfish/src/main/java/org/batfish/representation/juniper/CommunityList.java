package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class CommunityList implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _invertMatch;

  private final List<CommunityListLine> _lines;

  private final String _name;

  public CommunityList(String name) {
    _name = name;
    _lines = new ArrayList<>();
  }

  public boolean getInvertMatch() {
    return _invertMatch;
  }

  public List<CommunityListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }

  public void setInvertMatch(boolean invertMatch) {
    _invertMatch = invertMatch;
  }
}
