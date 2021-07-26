package org.batfish.question.filterlinereachability;

import org.batfish.datamodel.answers.AclSpecs;

/** Data about a filter line that is unreachable because it has an undefined reference. */
public class FilterLineWithUndefinedReference extends UnreachableFilterLine {
  // TODO include information about the undefined reference
  public FilterLineWithUndefinedReference(AclSpecs aclSpecs, int lineNumber) {
    super(aclSpecs, lineNumber);
  }

  @Override
  public <T> T accept(UnreachableFilterLineVisitor<T> visitor) {
    return visitor.visitFilterLineWithUndefinedReference(this);
  }
}
