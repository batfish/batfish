package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;

public final class CommunityList extends ComparableStructure<String> implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _invertMatch;

  private final List<CommunityListLine> _lines;

  public CommunityList(String name) {
    super(name);
    _lines = new ArrayList<>();
  }

  public boolean getInvertMatch() {
    return _invertMatch;
  }

  public List<CommunityListLine> getLines() {
    return _lines;
  }

  public void setInvertMatch(boolean invertMatch) {
    _invertMatch = invertMatch;
  }
}
