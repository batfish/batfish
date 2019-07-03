package org.batfish.representation.cisco;

import java.util.LinkedList;
import java.util.List;
import org.batfish.datamodel.IpSpace;

public class NetworkObjectGroup extends ObjectGroup {

  private List<IpSpace> _lines;

  public NetworkObjectGroup(String name) {
    super(name);
    _lines = new LinkedList<>();
  }

  public List<IpSpace> getLines() {
    return _lines;
  }
}
