package org.batfish.common.topology.bridge_domain.function;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToNonVlanAwareBridgeDomain;

/** A {@link StateFunction} that remove a given number of tags from the tag stack. */
public interface PopTag extends L2ToNonVlanAwareBridgeDomain.Function {

  static @Nonnull PopTag of(int count) {
    return count == 0 ? identity() : count == 1 ? PopTagImpl.POP_ONE_TAG : new PopTagImpl(count);
  }

  final class PopTagImpl implements PopTag {

    @Override
    public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
      return visitor.visitPopTag(this, arg);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof PopTagImpl)) {
        return false;
      }
      PopTagImpl popTag = (PopTagImpl) o;
      return _count == popTag._count;
    }

    @Override
    public int hashCode() {
      return _count;
    }

    /**
     * The number of outer tags to remove from the state.
     *
     * <p>The result is undefined if this number exceeds the number of outer tags in the state.
     */
    public int getCount() {
      return _count;
    }

    private static final PopTagImpl POP_ONE_TAG = new PopTagImpl(1);

    private PopTagImpl(int count) {
      _count = count;
    }

    private final int _count;
  }
}
