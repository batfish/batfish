package org.batfish.common.topology.bridge_domain.function;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;

/** A {@link StateFunction} that remove a given number of tags from the tag stack. */
public final class PopTagImpl implements PopTag {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitPopTag(this, arg);
  }

  /**
   * The number of outer tags to remove from the state.
   *
   * <p>The result is undefined if this number exceeds the number of outer tags in the state.
   */
  public int getCount() {
    return _count;
  }

  static @Nonnull PopTag of(int count) {
    return count == 0 ? identity() : count == 1 ? POP_ONE_TAG : new PopTagImpl(count);
  }

  private static final PopTagImpl POP_ONE_TAG = new PopTagImpl(1);

  private PopTagImpl(int count) {
    _count = count;
  }

  private final int _count;
}
