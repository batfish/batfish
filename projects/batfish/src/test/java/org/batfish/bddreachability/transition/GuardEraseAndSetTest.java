package org.batfish.bddreachability.transition;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.bddreachability.transition.GuardEraseAndSet.ValueBeforeAndAfter;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPacket;
import org.junit.Test;

public class GuardEraseAndSetTest {
  private static BDDFactory createBddFactory() {
    BDDFactory factory = JFactory.init(1000, 1000);
    factory.setVarNum(10);
    return factory;
  }

  BDDFactory _factory = createBddFactory();

  BDD _one = _factory.one();

  BDD _v0 = _factory.ithVar(0); // leave space for primes!
  BDD _v1 = _factory.ithVar(2);
  BDD _v2 = _factory.ithVar(4);

  BDD _vars = _v0.and(_v1);

  @Test
  public void testBasic() {
    Transition t =
        new GuardEraseAndSet(
            _vars,
            ImmutableList.of(
                new ValueBeforeAndAfter(_v0.and(_v1), _v0.nor(_v1)),
                new ValueBeforeAndAfter(_v0.nor(_v1), _v0.and(_v1))));

    assertEquals(_v0.nor(_v1), t.transitForward(_v0.and(_v1)));
    assertEquals(_v0.nor(_v1), t.transitBackward(_v0.and(_v1)));

    assertEquals(_v0.and(_v1), t.transitForward(_v0.nor(_v1)));
    assertEquals(_v0.and(_v1), t.transitBackward(_v0.nor(_v1)));

    assertEquals(_v0.biimp(_v1), t.transitForward(_one));
    assertEquals(_v0.biimp(_v1), t.transitBackward(_one));

    assertEquals(_v0.nor(_v1), t.transitForward(_v0));
    assertEquals(_v0.and(_v1), t.transitForward(_v0.not()));
  }

  @Test
  public void testExtraVars() {
    // invariant: extra var (_v2 here) constraints must be consistent before and after
    BDD before1 = _v0.and(_v1.and(_v2));
    BDD after1 = _v0.nor(_v1).and(_v2);
    BDD before2 = _v0.nor(_v1).diff(_v2);
    BDD after2 = _v0.and(_v1).diff(_v2);
    Transition t =
        new GuardEraseAndSet(
            _vars,
            ImmutableList.of(
                new ValueBeforeAndAfter(before1, after1),
                new ValueBeforeAndAfter(before2, after2)));

    assertEquals(after1, t.transitForward(before1));
    assertEquals(before2, t.transitBackward(after2));

    assertEquals(after2, t.transitForward(before2));
    assertEquals(before1, t.transitBackward(after1));

    assertEquals(after1.or(after2), t.transitForward(_one));
    assertEquals(before1.or(before2), t.transitBackward(_one));

    assertEquals(after1, t.transitForward(_v2));
    assertEquals(after2, t.transitForward(_v2.not()));

    assertEquals(before1, t.transitBackward(_v2));
    assertEquals(before2, t.transitBackward(_v2.not()));
  }

  @Test
  public void test3() {
    BDDPacket pkt = new BDDPacket();
    BDDInteger srcPort = pkt.getSrcPort();
    BDDInteger dstPort = pkt.getDstPort();

    BDD guard1 = srcPort.value(1);
    BDD set1 = srcPort.value(11);
    BDD guard2 = srcPort.value(2);
    BDD set2 = srcPort.value(12);

    Transition t =
        new GuardEraseAndSet(
            srcPort.getVars(),
            ImmutableList.of(
                new ValueBeforeAndAfter(guard1, set1), new ValueBeforeAndAfter(guard2, set2)));

    BDD before = guard1.and(dstPort.value(1)).or(guard2.and(dstPort.value(2)));
    BDD after = set1.and(dstPort.value(1)).or(set2.and(dstPort.value(2)));

    assertEquals(after, t.transitForward(before));
    assertEquals(before, t.transitBackward(after));
  }
}
