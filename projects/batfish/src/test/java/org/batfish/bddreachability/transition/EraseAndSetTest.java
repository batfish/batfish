package org.batfish.bddreachability.transition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link EraseAndSet}. */
public class EraseAndSetTest {
  private static final BDDPacket PKT = new BDDPacket();
  private static final BDD ONE = PKT.getFactory().one();
  private static final BDD ZERO = PKT.getFactory().zero();

  private static BDD dstIp(String ip) {
    return PKT.getDstIp().value(new Ip(ip).asLong());
  }

  private static BDD srcIp(String ip) {
    return PKT.getSrcIp().value(new Ip(ip).asLong());
  }

  @Test
  public void testEraseAndSet() {
    BDD setBdd = dstIp("1.1.1.1");
    BDD vars = Arrays.stream(PKT.getDstIp().getBitvec()).reduce(PKT.getFactory().one(), BDD::and);
    Transition transition = new EraseAndSet(vars, setBdd);

    assertThat(transition.transitForward(ONE), equalTo(setBdd));
    assertThat(transition.transitBackward(ONE), equalTo(ONE));

    assertThat(transition.transitForward(setBdd), equalTo(setBdd));
    assertThat(transition.transitBackward(setBdd), equalTo(ONE));

    assertThat(transition.transitForward(setBdd.not()), equalTo(setBdd));
    assertThat(transition.transitBackward(setBdd.not()), equalTo(ZERO));

    // more interesting example
    BDD bdd = setBdd.ite(srcIp("5.5.5.5"), srcIp("6.6.6.6"));
    /* transitForward: After erasing setBdd, we end up with srcIp("5.5.5.5").or(srcIp("6.6.6.6")).
     * Then we and the setBdd constraint back in.
     */
    assertThat(
        transition.transitForward(bdd), equalTo(setBdd.and(srcIp("5.5.5.5").or(srcIp("6.6.6.6")))));

    /* transitBackward: After applying the setBdd constraint, we have setBdd.and(srcIp("5.5.5.5")).
     * The false branch of the ite is inconsistent with the outputs of the transition. Then we erase
     * the dstIp vars and are left with just srcIp("5.5.5.5").
     */
    assertThat(transition.transitBackward(bdd), equalTo(srcIp("5.5.5.5")));
  }
}
