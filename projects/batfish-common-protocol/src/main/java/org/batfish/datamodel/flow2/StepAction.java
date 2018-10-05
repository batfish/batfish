package org.batfish.datamodel.flow2;

public abstract class StepAction {

  private final String _name;

  public StepAction(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
