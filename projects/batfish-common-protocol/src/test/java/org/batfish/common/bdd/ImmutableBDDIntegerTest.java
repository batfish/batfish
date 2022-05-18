package org.batfish.common.bdd;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.junit.Test;

public class ImmutableBDDIntegerTest {

  @Test
  public void testGetVars_emptyVar() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 0, 0);
    assertEquals(factory.one(), x.getVars());
  }

  @Test
  public void testToRangeSet() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    ImmutableBDDInteger x = ImmutableBDDInteger.makeFromIndex(factory, 5, 0);

    assertEquals(ImmutableRangeSet.of(Range.closedOpen(0, 3)), x.toRangeSet(x.range(0, 2)));

    for (int a = 0; a < 32; ++a) {
      for (int b = a; b < 32; ++b) {
        BDD range = x.range(a, b);
        assertEquals(ImmutableRangeSet.of(Range.closedOpen(a, b + 1)), x.toRangeSet(range));
      }
    }
  }

  @Test
  public void testToRangeSet_primed() {
    BDDPacket pkt = new BDDPacket();
    ImmutableBDDInteger x = pkt.getDstIp();

    assertEquals(ImmutableRangeSet.of(Range.closedOpen(0, 3)), x.toRangeSet(x.range(0, 2)));

    for (int a = 0; a < 32; ++a) {
      for (int b = a; b < 32; ++b) {
        BDD range = x.range(a, b);
        assertEquals(ImmutableRangeSet.of(Range.closedOpen(a, b + 1)), x.toRangeSet(range));
      }
    }
  }
}
