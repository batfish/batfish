package org.batfish.question.filterlinereachability;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.AclSpecs;

/** Stores data about an unreachable filter line. */
@ParametersAreNonnullByDefault
public abstract class UnreachableFilterLine {
  private final AclSpecs _aclSpecs;
  private final int _lineNumber;

  public UnreachableFilterLine(AclSpecs aclSpecs, int lineNumber) {
    _aclSpecs = aclSpecs;
    _lineNumber = lineNumber;
  }

  public abstract <T> T accept(UnreachableFilterLineVisitor<T> visitor);

  public static UnreachableFilterLine forUnmatchableLine(AclSpecs aclSpecs, int lineNumber) {
    if (aclSpecs.acl.hasUndefinedRef(lineNumber)) {
      // TODO include information about the undefined reference
      return new FilterLineWithUndefinedReference(aclSpecs, lineNumber);
    } else {
      return new IndependentlyUnmatchableFilterLine(aclSpecs, lineNumber);
    }
  }

  public AclSpecs getAclSpecs() {
    return _aclSpecs;
  }

  public int getLineNumber() {
    return _lineNumber;
  }
}
