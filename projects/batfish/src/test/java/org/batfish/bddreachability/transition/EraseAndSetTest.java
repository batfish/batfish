package org.batfish.bddreachability.transition;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link EraseAndSet}. */
public class EraseAndSetTest {
  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _one = _pkt.getFactory().one();
  private final BDD _zero = _pkt.getFactory().zero();

  private BDD dstIp(String ip) {
    return _pkt.getDstIp().value(Ip.parse(ip).asLong());
  }

  private BDD srcIp(String ip) {
    return _pkt.getSrcIp().value(Ip.parse(ip).asLong());
  }

  @Test
  public void testEraseAndSet() {
    BDD setBdd = dstIp("1.1.1.1");
    BDD vars = Arrays.stream(_pkt.getDstIp().getBitvec()).reduce(_pkt.getFactory().one(), BDD::and);
    Transition transition = new EraseAndSet(vars, setBdd);

    assertThat(transition.transitForward(_one), equalTo(setBdd));
    assertThat(transition.transitBackward(_one), equalTo(_one));

    assertThat(transition.transitForward(setBdd), equalTo(setBdd));
    assertThat(transition.transitBackward(setBdd), equalTo(_one));

    assertThat(transition.transitForward(setBdd.not()), equalTo(setBdd));
    assertThat(transition.transitBackward(setBdd.not()), equalTo(_zero));

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
