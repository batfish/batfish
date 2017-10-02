package org.batfish.symbolic.answers;

import org.batfish.datamodel.answers.AnswerElement;

public class AbstractionAnswerElement implements AnswerElement {

  protected int _originalSize;

  protected int _numClasses;

  protected int _min;

  protected int _max;

  protected double _average;

  public int getOriginalSize() {
    return _originalSize;
  }

  public void setOriginalSize(int originalSize) {
    this._originalSize = originalSize;
  }

  public int getNumClasses() {
    return _numClasses;
  }

  public void setNumClasses(int numClasses) {
    this._numClasses = numClasses;
  }

  public int getMin() {
    return _min;
  }

  public void setMin(int min) {
    this._min = min;
  }

  public int getMax() {
    return _max;
  }

  public void setMax(int max) {
    this._max = max;
  }

  public double getAverage() {
    return _average;
  }

  public void setAverage(double average) {
    this._average = average;
  }

  @Override
  public String prettyPrint() {
    return "";
  }

}
