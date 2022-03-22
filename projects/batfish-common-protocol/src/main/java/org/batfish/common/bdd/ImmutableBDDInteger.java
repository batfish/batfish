package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.bitvector;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;
import javax.annotation.Nonnull;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDTraversal;

public class ImmutableBDDInteger extends BDDInteger {
  private BDD _vars;

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

  private static class ToRangeSet implements BDDTraversal {
    private final int _firstVar;
    private final int _numBits;
    private int _currentVal;
    private int _nextVar;
    ImmutableRangeSet.Builder<Integer> _rangesBuilder;

    private ToRangeSet(int firstVar, int numBits) {
      _firstVar = firstVar;
      _numBits = numBits;

      _currentVal = 0;
      _nextVar = firstVar;
      _rangesBuilder = ImmutableRangeSet.builder();
    }

    public RangeSet<Integer> build() {
      return _rangesBuilder.build();
    }

    private void addRange() {
      int unconstrainedBits = _numBits - (_nextVar - _firstVar);
      int startInclusive = _currentVal << unconstrainedBits;
      int endExclusive = (_currentVal + 1) << unconstrainedBits;
      _rangesBuilder.add(
              Range.closedOpen(startInclusive, endExclusive).canonical(DiscreteDomain.integers()));
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
      _nextVar--;
    }

    @Override
    public boolean high(int var) {
      if (var >= _firstVar + _numBits) {
        addRange();

        // for backtracking
        _currentVal <<= 1;
        _nextVar++;

        return false;
      }

      checkArgument(var == _nextVar);
      _currentVal = (_currentVal << 1) + 1;
      _nextVar += 1;
      return true;
    }

    @Override
    public boolean low(int var) {
      if (var >= _firstVar + _numBits) {
        addRange();

        // for backtracking
        _currentVal <<= 1;
        _nextVar++;

        return false;
      }

      checkArgument(var == _nextVar);
      _currentVal <<= 1;
      _nextVar++;
      return true;
    }
  }

  RangeSet<Integer> toRangeSet(BDD bdd) {
    ToRangeSet traversal = new ToRangeSet(_bitvec[0].level(), _bitvec.length);
    bdd.traverse(traversal);
    return traversal._rangesBuilder.build();
  }
}
