package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StandardIpv6AccessList implements Serializable {

  private List<StandardIpv6AccessListLine> _lines;

  private final String _name;

  public StandardIpv6AccessList(String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(StandardIpv6AccessListLine all) {
    _lines.add(all);
  }

  public List<StandardIpv6AccessListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }

  public ExtendedIpv6AccessList toExtendedIpv6AccessList() {
    ExtendedIpv6AccessList eal = new ExtendedIpv6AccessList(_name);
    eal.setParent(this);
    eal.getLines().clear();
    for (StandardIpv6AccessListLine sall : _lines) {
      eal.addLine(sall.toExtendedIpv6AccessListLine());
    }
    return eal;
  }
}
