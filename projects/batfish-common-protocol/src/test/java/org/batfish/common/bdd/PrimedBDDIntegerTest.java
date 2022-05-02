package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDUtils.bddFactory;
import static org.batfish.common.bdd.BDDUtils.bitvector;
import static org.junit.Assert.assertEquals;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.junit.Test;

/** Test for {@link PrimedBDDInteger}. */
public final class PrimedBDDIntegerTest {

  @Test
  public void testBasic() {
    BDDFactory factory = bddFactory(10);
    PrimedBDDInteger primedInteger =
        new PrimedBDDInteger(
            factory, bitvector(factory, 5, 0, false), bitvector(factory, 5, 5, false));

    BDDInteger var = primedInteger.getVar();
    BDDInteger primeVar = primedInteger.getPrimeVar();

    BDD var2 = var.value(2);
    BDD var5 = var.value(5);
    BDD primeVar2 = primeVar.value(2);
    BDD primeVar5 = primeVar.value(5);
    BDDPairing swap = primedInteger.getPairingFactory().getSwapPairing();

    assertEquals(primeVar2, var2.replace(swap));
    assertEquals(var5, primeVar5.replace(swap));
    assertEquals(var2.and(primeVar5), primeVar2.and(var5).replace(swap));
  }
}
