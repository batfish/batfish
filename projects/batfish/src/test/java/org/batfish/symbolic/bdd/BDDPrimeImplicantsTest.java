package org.batfish.symbolic.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.LinkedList;
import java.util.List;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.junit.Test;

public class BDDPrimeImplicantsTest {
  @Test
  public void testPrimeImplicants() {
    BDDFactory factory = JFactory.init(10000, 1000);
    factory.setVarNum(3);
    BDD x = factory.ithVar(0);
    BDD y = factory.ithVar(1);
    BDD z = factory.ithVar(2);

    BDD bdd1 = x.or(y);
    List<BDD> pis1 = allPIs(bdd1);
    List<BDD> oracle1 = new LinkedList<>();
    oracle1.add(x);
    oracle1.add(y);
    assertThat(pis1, equalTo(oracle1));

    BDD bdd2 = x.or(y).and(x.not().or(z));
    List<BDD> pis2 = allPIs(bdd2);
    List<BDD> oracle2 = new LinkedList<>();
    oracle2.add(x.and(z));
    oracle2.add(x.not().and(y));
    oracle2.add(y.and(z));
    assertThat(pis2, equalTo(oracle2));
  }

  private static List<BDD> allPIs(BDD bdd) {
    BDDPrimeImplicants bddPI = new BDDPrimeImplicants(bdd);
    List<BDD> pis = new LinkedList<>();
    for (BDD pi : bddPI) {
      pis.add(pi);
    }
    return pis;
  }
}
