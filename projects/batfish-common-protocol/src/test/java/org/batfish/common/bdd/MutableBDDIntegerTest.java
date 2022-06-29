package org.batfish.common.bdd;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class MutableBDDIntegerTest {
  @Test
  public void testIpToBDD() {
    BDDFactory factory = BDDUtils.bddFactory(32);
    Ip ip = Ip.parse("1.2.3.4");

    // reverse=false
    {
      MutableBDDInteger x = MutableBDDInteger.makeFromIndex(factory, 32, 0, false);
      BDD bdd = x.toBDD(ip);
      assertThat(x.getValuesSatisfying(bdd, 100), contains(ip.asLong()));
    }

    // reverse=true
    {
      MutableBDDInteger x = MutableBDDInteger.makeFromIndex(factory, 32, 0, true);
      BDD bdd = x.toBDD(ip);
      assertThat(x.getValuesSatisfying(bdd, 100), contains(ip.asLong()));
    }
  }

  @Test
  public void testPrefixToBDD() {
    BDDFactory factory = BDDUtils.bddFactory(32);
    Prefix p = Prefix.parse("1.2.3.64/30");
    Long[] expected =
        new Long[] {
          Ip.parse("1.2.3.64").asLong(),
          Ip.parse("1.2.3.65").asLong(),
          Ip.parse("1.2.3.66").asLong(),
          Ip.parse("1.2.3.67").asLong()
        };

    // reverse=false
    {
      MutableBDDInteger x = MutableBDDInteger.makeFromIndex(factory, 32, 0, false);
      BDD bdd = x.toBDD(p);
      assertThat(x.getValuesSatisfying(bdd, 100), contains(expected));
    }

    // reverse=true
    {
      MutableBDDInteger x = MutableBDDInteger.makeFromIndex(factory, 32, 0, true);
      BDD bdd = x.toBDD(p);
      List<Long> valuesSatisfying = x.getValuesSatisfying(bdd, 100);
      assertThat(
          valuesSatisfying,
          // reverse=true changes the order
          containsInAnyOrder(expected));
    }
  }

  @Test
  public void testAdd() {
    BDDFactory factory = BDDUtils.bddFactory(10);
    MutableBDDInteger x = MutableBDDInteger.makeFromIndex(factory, 5, 0, false);
    MutableBDDInteger constant1 = MutableBDDInteger.makeFromValue(factory, 5, 1);
    BDDInteger xPlus1 = x.add(constant1);

    assertTrue(x.value(0).equals(xPlus1.value(1))); // x == 0 <==> x+1 == 1
    assertTrue(x.value(1).equals(xPlus1.value(2))); // x == 1 <==> x+1 == 2
    assertTrue(x.value(31).equals(xPlus1.value(0))); // x == 31 <==> x+1 == 0

    // Check that each variable's bitvec is properly used with satisfying assignment.
    assertThat(x.getValuesSatisfying(x.value(3L), 100), contains(3L));
    assertThat(xPlus1.getValuesSatisfying(xPlus1.value(3L), 100), contains(3L));
    assertThat(xPlus1.getValuesSatisfying(x.value(3L), 100), contains(4L));
    assertThat(x.getValuesSatisfying(xPlus1.value(3L), 100), contains(2L));
  }
}
