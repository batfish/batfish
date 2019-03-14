package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import net.sf.javabdd.BDD;
import org.batfish.datamodel.IcmpCode;

/** Symbolic {@link IcmpCode} variable represented by an 8-bit BDD. */
public final class BDDIcmpCode {
  private final BDDInteger _var;

  public BDDIcmpCode(BDDInteger var) {
    checkArgument(var.getBitvec().length == 8, "IcmpCode field requires 8 bits");
    _var = var;
  }

  public BDD value(int icmpCode) {
    return icmpCode == IcmpCode.UNSET ? _var.getFactory().one() : _var.value(icmpCode);
  }

  public int satAssignmentToValue(BDD satAssignment) {
    return _var.satAssignmentToLong(satAssignment).intValue();
  }

  public BDD geq(int start) {
    return start == IcmpCode.UNSET ? _var.getFactory().one() : _var.geq(start);
  }

  public BDD leq(int end) {
    return end == IcmpCode.UNSET ? _var.getFactory().one() : _var.leq(end);
  }
}
