package org.batfish.datamodel.questions;

public class TestQuestion extends Question {

  private final boolean _dataPlane;
  private final String _name;

  public TestQuestion() {
    this("____test_question____", false);
  }

  public TestQuestion(String name, boolean dataPlane) {
    _name = name;
    _dataPlane = dataPlane;
  }

  @Override
  public boolean getDataPlane() {
    return _dataPlane;
  }

  @Override
  public String getName() {
    return _name;
  }
}
