package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import net.sf.javabdd.BDD;
import org.batfish.datamodel.IcmpType;

public class BDDIcmpType {
  private final BDDInteger _var;

  public BDDIcmpType(BDDInteger var) {
    checkArgument(var.getBitvec().length == 8, "IcmpType field requires 8 bits");
    _var = var;
  }

  public BDD value(int icmpType) {
    return icmpType == IcmpType.UNSET ? _var.getFactory().one() : _var.value(icmpType);
  }

  public int satAssignmentToValue(BDD satAssignment) {
    return _var.satAssignmentToLong(satAssignment).intValue();
  }

  public BDD geq(int start) {
    return start == IcmpType.UNSET ? _var.getFactory().one() : _var.geq(start);
  }

  public BDD leq(int end) {
    return end == IcmpType.UNSET ? _var.getFactory().one() : _var.leq(end);
  }
}
