package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.BitSet;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IcmpType;

/** Symbolic IcmpType variable represented by an 8-bit BDD. */
public final class BDDIcmpType {
  private final BDDInteger _var;

  public BDDIcmpType(BDDInteger var) {
    checkArgument(var.size() == 8, "IcmpType field requires 8 bits");
    _var = var;
  }

  /** @return a constraint that the IcmpType have the specified value. */
  public BDD value(int icmpType) {
    return icmpType == IcmpType.UNSET ? _var.getFactory().one() : _var.value(icmpType);
  }

  /**
   * Extract the value from a satisfying assignment.
   *
   * @param satAssignment a satisfying assignment (i.e. produced by fullSat, allSat, etc)
   */
  public int satAssignmentToValue(BDD satAssignment) {
    return _var.satAssignmentToLong(satAssignment).intValue();
  }

  /**
   * Extract the value from the bits of a satisfying assignment.
   *
   * @param satAssignment see {@link BDD#minAssignmentBits()}.
   */
  public int satAssignmentToValue(BitSet satAssignment) {
    return _var.satAssignmentToLong(satAssignment).intValue();
  }

  /** @return a constraint that the IcmpType be greater than or equal to the specified value. */
  public BDD geq(int start) {
    return start == IcmpType.UNSET ? _var.getFactory().one() : _var.geq(start);
  }

  /** @return a constraint that the IcmpType be less than or equal to the specified value. */
  public BDD leq(int end) {
    return end == IcmpType.UNSET ? _var.getFactory().one() : _var.leq(end);
  }

  /** Returns the {@link BDDInteger} backing this. */
  public BDDInteger getBDDInteger() {
    return _var;
  }
}
