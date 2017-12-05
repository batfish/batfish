package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.DefinedStructure;

public class PrefixList extends DefinedStructure<String> {

  private static final long serialVersionUID = 1L;

  private List<PrefixListLine> _lines;

  public PrefixList(String name, int definitionLine) {
    super(name, definitionLine);
    _lines = new ArrayList<>();
  }

  public void addLine(PrefixListLine r) {
    _lines.add(r);
  }

  public void addLines(List<PrefixListLine> r) {
    _lines.addAll(r);
  }

  public List<PrefixListLine> getLines() {
    return _lines;
  }
}
