package org.batfish.minesweeper.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.junit.Test;

public class BDDPrimeImplicantsTest {
  @Test
  public void testPrimeImplicants() {
    BDDFactory factory = JFactory.init(10000, 1000);
    factory.setVarNum(5);
    BDD x = factory.ithVar(0);
    BDD y = factory.ithVar(1);
    BDD z = factory.ithVar(2);
    BDD u = factory.ithVar(3);
    BDD v = factory.ithVar(4);

    assertThat(allPIs(x.or(y)), equalTo(ImmutableList.of(y, x)));

    assertThat(
        allPIs(x.or(y).and(x.not().or(z))),
        equalTo(ImmutableList.of(x.and(z), x.not().and(y), y.and(z))));

    assertThat(
        allPIs(x.and(y).or(z.and(u)).or(v)), equalTo(ImmutableList.of(v, z.and(u), x.and(y))));
  }

  private static List<BDD> allPIs(BDD bdd) {
    return ImmutableList.copyOf(new BDDPrimeImplicants(bdd));
  }
}
