package org.batfish.representation.cisco;

public class InspectClassMapMatchAccessGroup implements InspectClassMapMatch {

  /** */
  private static final long serialVersionUID = 1L;

  private String _name;

  public InspectClassMapMatchAccessGroup(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }
}
