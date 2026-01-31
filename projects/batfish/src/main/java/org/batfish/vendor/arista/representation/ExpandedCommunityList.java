package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExpandedCommunityList implements Serializable {

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
