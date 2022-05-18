package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.bitvector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDTraversal;
import org.batfish.datamodel.LongSpace;

public class ImmutableBDDInteger extends BDDInteger {
  // Lazy init
  private BDD _vars = null;
  private Map<Integer, Integer> _varToPosition = null;

  public ImmutableBDDInteger(BDDFactory factory, BDD[] bitvec) {
    super(factory, bitvec);
  }

  /**
   * Create an integer, and initialize its values as "don't care" This requires knowing the start
   * index variables the bitvector will use.
   */
  public static ImmutableBDDInteger makeFromIndex(BDDFactory factory, int length, int start) {
    return new ImmutableBDDInteger(factory, bitvector(factory, length, start, false));
  }

  @Override
  public Optional<Long> getValueSatisfying(BDD bdd) {
    return bdd.isZero()
        ? Optional.empty()
        : Optional.of(satAssignmentToLong(bdd.minAssignmentBits()));
  }

  /** Returns a {@link BDD} containing all the variables of this {@link BDDInteger}. */
  public @Nonnull BDD getVars() {
    if (_vars == null) {
      _vars = _factory.andAll(_bitvec);
    }
    return _vars;
  }

  /**
   * Returns a {@link BDD} containing the {@code n} high-order variables of this {@link BDDInteger}.
   */
  public @Nonnull BDD getMostSignificantVars(int n) {
    checkArgument(n <= _bitvec.length, "Cannot get more vars than exist");
    if (n == _bitvec.length) {
      return getVars();
    }
    return _factory.andAll(Arrays.copyOf(_bitvec, n));
  }

  @Override
  public long satAssignmentToLong(BDD satAssignment) {
    checkArgument(satAssignment.isAssignment(), "not a satisfying assignment");
    return satAssignmentToLong(satAssignment.minAssignmentBits());
  }

  public int satAssignmentToInt(BitSet bits) {
    checkArgument(
        _bitvec.length <= 31, "Only BDDInteger of 31 or fewer bits can be converted to int");
    return satAssignmentToLong(bits).intValue();
  }

  public Long satAssignmentToLong(BitSet bits) {
    long value = 0;
    for (int i = 0; i < _bitvec.length; i++) {
      BDD bitBDD = _bitvec[_bitvec.length - i - 1];
      if (bits.get(bitBDD.level())) {
        value |= 1L << i;
      }
    }
    return value;
  }

  private static class ToLongSpace implements BDDTraversal {
    private final Map<Integer, Integer> _varToPosition;
    private final int _numBits;
    private long _currentVal;
    private int _nextPos;
    LongSpace.Builder _rangesBuilder;

    private ToLongSpace(Map<Integer, Integer> varToPosition) {
      _varToPosition = ImmutableMap.copyOf(varToPosition);
      _currentVal = 0;
      _nextPos = 0;
      _numBits = _varToPosition.size();
      _rangesBuilder = LongSpace.builder();
    }

    public LongSpace build() {
      return _rangesBuilder.build();
    }

    private void addRange() {
      int unconstrainedBits = _numBits - _nextPos;
      long startInclusive = _currentVal << unconstrainedBits;
      long endExclusive = (_currentVal + 1) << unconstrainedBits;
      _rangesBuilder.including(Range.closedOpen(startInclusive, endExclusive));
    }

    @Override
    public void one() {
      addRange();
    }

    @Override
    public void zero() {}

    @Override
    public void backtrack() {
      _currentVal >>= 1;
      _nextPos--;
    }

    @Override
    public boolean traverse_high(int var) {
      @Nullable Integer pos = _varToPosition.get(var);
      if (pos == null) {
        addRange();
        return false;
      }

      checkArgument(pos >= _nextPos);

      if (pos == _nextPos) {
        _currentVal = (_currentVal << 1) + 1;
        _nextPos += 1;
        return true;
      } else {
        // there are at least 2^(pos - _nextPos) ranges within here. this should happen ~never, so
        // just skip
        return false;
      }
    }

    @Override
    public boolean traverse_low(int var) {
      @Nullable Integer pos = _varToPosition.get(var);
      if (pos == null) {
        addRange();
        return false;
      }

      checkArgument(pos >= _nextPos);
      if (pos == _nextPos) {
        _currentVal <<= 1;
        _nextPos++;
        return true;
      } else {
        // there are at least 2^(pos - _nextPos) ranges within here. this should happen ~never, so
        // just skip
        return false;
      }
    }
  }

  public LongSpace toLongSpace(BDD bdd) {
    if (_varToPosition == null) {
      ImmutableMap.Builder<Integer, Integer> builder =
          ImmutableMap.builderWithExpectedSize(_bitvec.length);
      for (int i = 0; i < _bitvec.length; i++) {
        builder.put(_bitvec[i].var(), i);
      }
      _varToPosition = builder.build();
    }

    ToLongSpace traversal = new ToLongSpace(_varToPosition);
    bdd.traverse(traversal);
    return traversal._rangesBuilder.build();
  }
}
