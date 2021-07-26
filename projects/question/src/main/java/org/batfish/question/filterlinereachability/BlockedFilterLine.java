package org.batfish.question.filterlinereachability;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.answers.AclSpecs;

/** Data about blocked/shadowed filter lines. */
public final class BlockedFilterLine extends UnreachableFilterLine {
  private final List<Integer> _blockingLines;
  private final boolean _diffAction;

  public BlockedFilterLine(
      AclSpecs aclSpecs, int lineNumber, List<Integer> blockingLines, boolean diffAction) {
    super(aclSpecs, lineNumber);
    checkArgument(!blockingLines.isEmpty(), "blockingLines must not be empty for a blocked line");
    _blockingLines = blockingLines;
    _diffAction = diffAction;
  }

  public List<Integer> getBlockingLines() {
    return _blockingLines;
  }

  /**
   * Whether one of the blocking lines has (or can have) a different action. Note: this is
   * non-trivial because {@link AclAclLine} can permit or deny.
   */
  public boolean hasDiffAction() {
    return _diffAction;
  }

  @Override
  public <T> T accept(UnreachableFilterLineVisitor<T> visitor) {
    return visitor.visitBlockedFilterLine(this);
  }
}
