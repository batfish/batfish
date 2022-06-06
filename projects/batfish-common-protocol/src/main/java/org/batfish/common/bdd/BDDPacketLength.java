package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.BitSet;
import net.sf.javabdd.BDD;

/** Symbolic packet length variable represented by a 16-bit {@link BDD}. */
public final class BDDPacketLength implements Serializable {
  private final ImmutableBDDInteger _var;

  public BDDPacketLength(ImmutableBDDInteger var) {
    checkArgument(var.size() == 16, "Packet length field requires 16 bits");
    _var = var;
  }

  /**
   * @return a constraint that the packet length have the specified value.
   */
  public BDD value(int v) {
    checkArgument(v <= 65535, "Maximum packet length is 65535");
    return _var.value(v);
  }

  /**
   * @return a constraint that the packet length have the specified value.
   */
  public BDD geq(int v) {
    checkArgument(v <= 65535, "Maximum packet length is 65535");
    return _var.geq(v);
  }

  /**
   * @return a constraint that the packet length have the specified value.
   */
  public BDD leq(int v) {
    checkArgument(v <= 65535, "Maximum packet length is 65535");
    return _var.leq(v);
  }

  /**
   * @return a constraint that the packet length be within the specified range.
   */
  public BDD range(int low, int high) {
    return geq(low).and(leq(high));
  }

  /**
   * Extract the value from a satisfying assignment.
   *
   * @param satAssignment a satisfying assignment (i.e. produced by fullSat, allSat, etc)
   */
  public int satAssignmentToValue(BDD satAssignment) {
    return satAssignmentToValue(satAssignment.minAssignmentBits());
  }

  /**
   * Extract the value from the bits of a satisfying assignment.
   *
   * @param satAssignment see {@link BDD#minAssignmentBits()}.
   */
  public int satAssignmentToValue(BitSet satAssignment) {
    // the sat assignment will be the minimum value of the original BDD. If the BDD did not
    // constrain packet length, the sat assignment will have value 0, which is below the minimum
    // value of 20. Rather than maintaining this minimum in every BDD (expensive), we do it here.
    // Note that we can get weird behavior is clients constrain the underlying BDDs bits directly
    // (i.e. without going through this class).
    return Integer.max(_var.satAssignmentToInt(satAssignment), 20);
  }

  /**
   * @return the {@link BDDInteger} backing this.
   */
  public BDDInteger getBDDInteger() {
    return _var;
  }
}
