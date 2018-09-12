package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDUtils.isAssignment;
import static org.hamcrest.MatcherAssert.assertThat;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.junit.Test;

public class BDDUtilsTest {
  @Test
  public void testIsAssignment_trivial() {
    BDDFactory factory = BDDUtils.bddFactory(1);
    assertThat("one is an assignment (that assigns nothing)", isAssignment(factory.one()));
    assertThat("zero is not an assignment", !isAssignment(factory.zero()));
  }

  @Test
  public void testIsAssignment() {
    BDDFactory factory = BDDUtils.bddFactory(2);
    BDD v0 = factory.ithVar(0);
    BDD v1 = factory.ithVar(1);
    BDD xor = v0.xor(v1);
    assertThat("xor is not an assignment", !isAssignment(xor));
    assertThat("xor.fullSatOne is an assignment", isAssignment(xor.fullSatOne()));
  }
}
