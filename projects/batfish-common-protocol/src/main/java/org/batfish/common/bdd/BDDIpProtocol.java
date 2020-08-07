package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IpProtocol;

/** Symbolic IpProtocol variable represented by an 8-bit BDD. */
public final class BDDIpProtocol {
  private final BDDInteger _var;

  public BDDIpProtocol(BDDInteger var) {
    checkArgument(var.size() == 8, "IpProtocol field requires 8 bits");
    _var = var;
  }

  /** @return a constraint that the IpProtocol have the specified value. */
  public BDD value(@Nullable IpProtocol v) {
    // null means any protocol
    return v == null ? _var.getFactory().one() : _var.value(v.number());
  }

  /**
   * Extract the value from a satisfying assignment.
   *
   * @param satAssignment a satisfying assignment (i.e. produced by fullSat, allSat, etc)
   */
  public IpProtocol satAssignmentToValue(BDD satAssignment) {
    return IpProtocol.fromNumber(_var.satAssignmentToLong(satAssignment).intValue());
  }

  /**
   * Extract the value from the bits of a satisfying assignment.
   *
   * @param satAssignment Produced by {@link BDD#minAssignmentBits()}.
   */
  public IpProtocol satAssignmentToValue(BitSet satAssignment) {
    return IpProtocol.fromNumber(_var.satAssignmentToLong(satAssignment).intValue());
  }

  /** @return the {@link BDDInteger} backing this. */
  public BDDInteger getBDDInteger() {
    return _var;
  }
}
