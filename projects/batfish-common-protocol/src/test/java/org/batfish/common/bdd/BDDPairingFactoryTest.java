package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDUtils.bddFactory;
import static org.batfish.common.bdd.BDDUtils.bitvector;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link BDDPairingFactory}. */
public final class BDDPairingFactoryTest {
  private BDDFactory _factory;

  @Before
  public void setup() {
    _factory = bddFactory(8);
  }

  @Test
  public void testBasic() {
    PrimedBDDInteger primedInteger1 =
        new PrimedBDDInteger(
            _factory, bitvector(_factory, 2, 0, false), bitvector(_factory, 2, 2, false));
    PrimedBDDInteger primedInteger2 =
        new PrimedBDDInteger(
            _factory, bitvector(_factory, 2, 4, false), bitvector(_factory, 2, 6, false));

    BDDInteger x = primedInteger1.getVar();
    BDDInteger xPrime = primedInteger1.getPrimeVar();

    BDDInteger y = primedInteger2.getVar();
    BDDInteger yPrime = primedInteger2.getPrimeVar();

    BDD x0 = x.value(0);
    BDD x1 = x.value(1);
    BDD xPrime0 = xPrime.value(0);
    BDD xPrime1 = xPrime.value(1);
    BDD y1 = y.value(1);
    BDD y2 = y.value(2);
    BDD y3 = y.value(3);
    BDD yPrime1 = yPrime.value(1);
    BDD yPrime2 = yPrime.value(2);
    BDD yPrime3 = yPrime.value(3);

    BDDPairing swapBoth =
        primedInteger1
            .getPairingFactory()
            .composeWith(primedInteger2.getPairingFactory())
            .makeSwapPairing();

    assertEquals(xPrime0.and(yPrime1), x0.and(y1).replace(swapBoth));
    assertEquals(x0.and(y1), xPrime0.and(yPrime1).replace(swapBoth));
    assertEquals(
        x0.and(xPrime1).and(y2).and(yPrime3),
        xPrime0.and(x1).and(yPrime2).and(y3).replace(swapBoth));
  }

  @Test
  public void testEquals() {
    BDD[] dom1 = {_factory.ithVar(0)};
    BDD[] dom2 = {_factory.ithVar(2)};
    BDD[] codom1 = {_factory.ithVar(1)};
    BDD[] codom2 = {_factory.ithVar(3)};
    new EqualsTester()
        .addEqualityGroup(new BDDPairingFactory(dom1, codom1), new BDDPairingFactory(dom1, codom1))
        .addEqualityGroup(new BDDPairingFactory(dom2, codom1))
        .addEqualityGroup(new BDDPairingFactory(dom1, codom2))
        .testEquals();
  }
}
