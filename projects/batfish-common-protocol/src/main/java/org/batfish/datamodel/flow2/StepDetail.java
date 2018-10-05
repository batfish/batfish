package org.batfish.datamodel.flow2;

public abstract class StepDetail {

  private final String _name;

  public StepDetail(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
