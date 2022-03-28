package org.batfish.common.bdd;

import static org.junit.Assert.assertEquals;

import net.sf.javabdd.BDDFactory;
import org.junit.Test;

public class ImmutableBDDIntegerTest {

  @Test
  public void testGetVars_emptyVar() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 0, 0, false);
    assertEquals(factory.one(), x.getVars());
  }
}
