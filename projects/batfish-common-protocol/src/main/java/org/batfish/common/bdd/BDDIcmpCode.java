package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.BitSet;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IcmpCode;

/** Symbolic {@link IcmpCode} variable represented by an 8-bit BDD. */
public final class BDDIcmpCode {
  private final BDDInteger _var;

  public BDDIcmpCode(BDDInteger var) {
    checkArgument(var.size() == 8, "IcmpCode field requires 8 bits");
    _var = var;
  }

  /** @return a constraint that the IcmpType have the specified value. */
  public BDD value(int icmpCode) {
    return icmpCode == IcmpCode.UNSET ? _var.getFactory().one() : _var.value(icmpCode);
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

  /** @return a constraint that the IcmpCode be greater than or equal to the specified value. */
  public BDD geq(int start) {
    return start == IcmpCode.UNSET ? _var.getFactory().one() : _var.geq(start);
  }

  /** @return a constraint that the IcmpCode be less than or equal to the specified value. */
  public BDD leq(int end) {
    return end == IcmpCode.UNSET ? _var.getFactory().one() : _var.leq(end);
  }

  /** Returns the {@link BDDInteger} backing this. */
  public BDDInteger getBDDInteger() {
    return _var;
  }
}
