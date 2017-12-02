package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.DefinedStructure;

public class ExpandedCommunityList extends DefinedStructure<String> {

  private static final long serialVersionUID = 1L;

  private List<ExpandedCommunityListLine> _lines;

  public ExpandedCommunityList(String name, int definitionLine) {
    super(name, definitionLine);
    _lines = new ArrayList<>();
  }

  public void addLine(ExpandedCommunityListLine line) {
    _lines.add(line);
  }

  public List<ExpandedCommunityListLine> getLines() {
    return _lines;
  }
}
