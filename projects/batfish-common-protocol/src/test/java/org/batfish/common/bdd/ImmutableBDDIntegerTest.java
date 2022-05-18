package org.batfish.common.bdd;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Range;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LongSpace;
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

    assertEquals(LongSpace.of(Range.closedOpen(0L, 3L)), x.toLongSpace(x.range(0, 2)));

    for (long a = 0; a < 32; ++a) {
      for (long b = a; b < 32; ++b) {
        BDD range = x.range(a, b);
        assertEquals(LongSpace.of(Range.closedOpen(a, b + 1L)), x.toLongSpace(range));
      }
    }
  }

  @Test
  public void testToRangeSet_wildcard() {
    BDDPacket pkt = new BDDPacket();
    ImmutableBDDInteger x = pkt.getDstIp();

    // there are 2^16 ranges in this wildcard. just skip them
    BDD bdd = x.toBDD(IpWildcard.parse("1.0.0.1:0.255.255.0"));
    assertEquals(LongSpace.EMPTY, x.toLongSpace(bdd));

    Ip ip = Ip.parse("2.2.2.2");
    bdd.orWith(x.toBDD(ip));
    assertEquals(LongSpace.of(Range.closedOpen(ip.asLong(), ip.asLong() + 1)), x.toLongSpace(bdd));
  }

  @Test
  public void testToRangeSet_primed() {
    BDDPacket pkt = new BDDPacket();
    ImmutableBDDInteger x = pkt.getDstIp();

    assertEquals(LongSpace.of(Range.closedOpen(0L, 3L)), x.toLongSpace(x.range(0, 2)));

    for (long a = 0; a < 32; ++a) {
      for (long b = a; b < 32; ++b) {
        BDD range = x.range(a, b);
        assertEquals(LongSpace.of(Range.closedOpen(a, b + 1L)), x.toLongSpace(range));
      }
    }
  }
}
