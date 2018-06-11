package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;

public class IpAsPathAccessList extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private List<IpAsPathAccessListLine> _lines;

  public IpAsPathAccessList(String name) {
    super(name);
    _lines = new ArrayList<>();
  }

  public void addLine(IpAsPathAccessListLine line) {
    _lines.add(line);
  }

  public List<IpAsPathAccessListLine> getLines() {
    return _lines;
  }
}
