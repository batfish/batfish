package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.bitvector;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

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
  public Long satAssignmentToLong(BDD satAssignment) {
    checkArgument(satAssignment.isAssignment(), "not a satisfying assignment");
    return satAssignmentToLong(satAssignment.minAssignmentBits());
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
}
