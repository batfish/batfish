package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.L2ToPhysical;
import org.batfish.common.topology.bridge_domain.edge.NonBridgedL3ToPhysical;

/** A {@link StateFunction} that pushes a fixed outer tag. */
public final class PushTag implements L2ToPhysical.Function, NonBridgedL3ToPhysical.Function {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitPushTag(this, arg);
  }

  /** The tag to push that will become the outer tag. */
  public int getTagToPush() {
    return _tagToPush;
  }

  static @Nonnull PushTag of(int tagToPush) {
    return new PushTag(tagToPush);
  }

  private PushTag(int tagToPush) {
    _tagToPush = tagToPush;
  }

  private final int _tagToPush;
}
