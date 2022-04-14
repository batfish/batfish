package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToL1;
import org.batfish.common.topology.bridge_domain.edge.L3ToL1;

/** A {@link StateFunction} that pushes a fixed outer tag. */
public final class PushTag implements L2ToL1.Function, L3ToL1.Function {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitPushTag(this, arg);
  }

  /** The tag to push that will become the outer tag. */
  public int getTagToPush() {
    return _tagToPush;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PushTag)) {
      return false;
    }
    PushTag pushTag = (PushTag) o;
    return _tagToPush == pushTag._tagToPush;
  }

  @Override
  public int hashCode() {
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
