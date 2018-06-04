package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.routing_policy.expr.AsPathSetElem;

public class AsPathSet extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private final List<AsPathSetElem> _elements;

  public AsPathSet(String name) {
    super(name);
    _elements = new ArrayList<>();
  }

  public List<AsPathSetElem> getElements() {
    return _elements;
  }
}
