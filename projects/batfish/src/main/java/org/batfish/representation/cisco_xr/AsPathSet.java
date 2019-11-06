package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.routing_policy.expr.AsPathSetElem;

public class AsPathSet implements Serializable {

  private final List<AsPathSetElem> _elements;
  private final String _name;

  public AsPathSet(String name) {
    _name = name;
    _elements = new ArrayList<>();
  }

  public List<AsPathSetElem> getElements() {
    return _elements;
  }

  public String getName() {
    return _name;
  }
}
