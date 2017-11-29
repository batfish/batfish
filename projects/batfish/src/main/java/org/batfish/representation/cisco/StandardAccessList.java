package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.DefinedStructure;

public class StandardAccessList extends DefinedStructure<String> {

  private static final long serialVersionUID = 1L;

  private List<StandardAccessListLine> _lines;

  public StandardAccessList(String id, int definitionLine) {
    super(id, definitionLine);
    _lines = new ArrayList<>();
  }

  public void addLine(StandardAccessListLine all) {
    _lines.add(all);
  }

  public List<StandardAccessListLine> getLines() {
    return _lines;
  }

  public ExtendedAccessList toExtendedAccessList() {
    ExtendedAccessList eal = new ExtendedAccessList(_key, getDefinitionLine());
    eal.setParent(this);
    eal.getLines().clear();
    for (StandardAccessListLine sall : _lines) {
      eal.addLine(sall.toExtendedAccessListLine());
    }
    return eal;
  }
}
