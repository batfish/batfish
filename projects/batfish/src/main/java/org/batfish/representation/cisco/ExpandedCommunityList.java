package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExpandedCommunityList implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<ExpandedCommunityListLine> _lines;

  private final String _name;

  public ExpandedCommunityList(String name) {
    _name = name;
    _lines = new ArrayList<>();
  }

  public void addLine(ExpandedCommunityListLine line) {
    _lines.add(line);
  }

  public List<ExpandedCommunityListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }
}
