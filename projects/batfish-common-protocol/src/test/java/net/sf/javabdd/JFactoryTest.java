package net.sf.javabdd;

import static org.junit.Assert.assertEquals;

import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.ImmutableBDDInteger;
import org.junit.Ignore;
import org.junit.Test;

/** Tests for {link JFactory}. */
public class JFactoryTest {
  @Test
  public void testMultiOr() {
    JFactory factory = (JFactory) JFactory.init(100000, 10000);
    factory.setVarNum(8);

    BDDInteger var = ImmutableBDDInteger.makeFromIndex(factory, 8, 0);
    BDD bdd1 = var.leq(8);
    BDD bdd2 = var.geq(128);
    BDD bdd3 = var.value(64);
    BDD multiOr = factory.orAll(bdd1, bdd2, bdd3);
    BDD or = bdd1.or(bdd2).or(bdd3);
    assertEquals(multiOr, or);
  }

  @Ignore
  @Test
  public void benchMultiOr() {
    int bits = 20;

    JFactory factory = (JFactory) JFactory.init(100000, 10000);
    factory.setVarNum(bits);
    BDDInteger var = ImmutableBDDInteger.makeFromIndex(factory, bits, 0);

    BDD[] bdds = new BDD[1 << bits];
    for (int i = 0; i < bdds.length; i++) {
      bdds[i] = var.value(i);
    }

    long tMultiOr = System.currentTimeMillis();
    BDD resMultiOr = factory.orAll(bdds);
    assert resMultiOr.isOne();
    tMultiOr = System.currentTimeMillis() - tMultiOr;
    System.out.println(String.format("MultOr: %dms", tMultiOr));

    factory = (JFactory) JFactory.init(100000, 10000);
    factory.setVarNum(bits);
    var = ImmutableBDDInteger.makeFromIndex(factory, bits, 0);

    for (int i = 0; i < bdds.length; i++) {
      bdds[i] = var.value(i);
    }

    long tOr = System.currentTimeMillis();
    BDD result = factory.zero();
    for (BDD bdd : bdds) {
      result = result.or(bdd);
    }
    tOr = System.currentTimeMillis() - tOr;
    System.out.println(String.format("Or: %dms", tOr));
  }
}
