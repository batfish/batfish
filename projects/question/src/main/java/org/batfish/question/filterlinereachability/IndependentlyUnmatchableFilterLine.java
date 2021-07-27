package org.batfish.question.filterlinereachability;

import org.batfish.datamodel.answers.AclSpecs;

public class IndependentlyUnmatchableFilterLine extends UnreachableFilterLine {
  public IndependentlyUnmatchableFilterLine(AclSpecs aclSpecs, int lineNumber) {
    super(aclSpecs, lineNumber);
  }

  @Override
  public <T> T accept(UnreachableFilterLineVisitor<T> visitor) {
    return visitor.visitIndependentlyUnmatchableFilterLine(this);
  }
}
