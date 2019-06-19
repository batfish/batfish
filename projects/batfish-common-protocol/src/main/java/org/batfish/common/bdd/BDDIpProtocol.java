package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import net.sf.javabdd.BDD;
import org.batfish.datamodel.IpProtocol;

/** Symbolic IpProtocol variable represented by an 8-bit BDD. */
public final class BDDIpProtocol {
  private final BDDInteger _var;

  public BDDIpProtocol(BDDInteger var) {
    checkArgument(var.getBitvec().length == 8, "IpProtocol field requires 8 bits");
    _var = var;
  }

  /** @return a constraint that the IpProtocol have the specified value. */
  public BDD value(IpProtocol v) {
    // IP is a special case, meaning "any protocol". It's also an invalid value for _var
    return v == IpProtocol.IP ? _var.getFactory().one() : _var.value(v.number());
  }

  /**
   * Extract the value from a satisfying assignment.
   *
   * @param satAssignment a satisfying assignment (i.e. produced by fullSat, allSat, etc)
   */
  public IpProtocol satAssignmentToValue(BDD satAssignment) {
    return IpProtocol.fromNumber(_var.satAssignmentToLong(satAssignment).intValue());
  }

  /** @return the {@link BDDInteger} backing this. */
  public BDDInteger getBDDInteger() {
    return _var;
  }
}
