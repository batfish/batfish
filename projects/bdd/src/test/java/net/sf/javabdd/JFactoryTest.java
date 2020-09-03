package net.sf.javabdd;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import org.junit.Test;

/** Tests of {@link JFactory}. */
public class JFactoryTest {
  private JFactory _factory = (JFactory) JFactory.init(10000, 10000);

  @Test
  public void testRandomSatDoesntSuck() {
    _factory.setVarNum(64);
    HashSet<BitSet> differentAssignments = new HashSet<>();
    BDD one = _factory.one();
    HashFunction someFn = Hashing.goodFastHash(8);
    for (int i = 0; i < 128; ++i) {
      int seed = someFn.hashInt(i).asInt();
      differentAssignments.add(one.randomFullSatOne(seed).minAssignmentBits());
    }
    // Collisions are plausible, but not all the time.
    assertThat(differentAssignments, hasSize(greaterThanOrEqualTo(100)));
  }

  @Test
  public void testAnd() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();
    // Anything and zero is zero
    assertThat(zero.and(zero), equalTo(zero));
    assertThat(zero.and(one), equalTo(zero));
    assertThat(one.and(zero), equalTo(zero));
    assertThat(zero.and(x), equalTo(zero));
    assertThat(x.and(zero), equalTo(zero));

    // Anything and one is anything.
    assertThat(one.and(one), equalTo(one));
    assertThat(x.and(one), equalTo(x));
    assertThat(one.and(x), equalTo(x));

    // And is commutative.
    assertThat(x.and(y), equalTo(y.and(x)));
    assertThat(y.and(notx), equalTo(notx.and(y)));
    assertThat(z.and(y.and(x)), equalTo(x.and(y).and(z)));

    // x and x is x.
    assertThat(x.and(x), equalTo(x));

    // x and not x is zero.
    assertThat(x.and(notx), equalTo(zero));
    assertThat(notx.and(x), equalTo(zero));
  }

  @Test
  public void testXor() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x xor zero is x
    assertThat(zero.xor(zero), equalTo(zero));
    assertThat(zero.xor(x), equalTo(x));
    assertThat(x.xor(zero), equalTo(x));
    assertThat(one.xor(zero), equalTo(one));
    assertThat(zero.xor(one), equalTo(one));

    // x xor one is notx.
    assertThat(one.xor(one), equalTo(zero));
    assertThat(x.xor(one), equalTo(notx));
    assertThat(one.xor(x), equalTo(notx));

    // xor is commutative.
    assertThat(x.xor(y), equalTo(y.xor(x)));
    assertThat(y.xor(notx), equalTo(notx.xor(y)));
    assertThat(z.xor(y.xor(x)), equalTo(x.xor(y).xor(z)));

    // x xor not x is one.
    assertThat(x.xor(notx), equalTo(one));
    assertThat(notx.xor(x), equalTo(one));

    // x xor x is zero.
    assertThat(x.xor(x), equalTo(zero));
  }

  @Test
  public void testOr() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x or zero is x
    assertThat(zero.or(zero), equalTo(zero));
    assertThat(zero.or(x), equalTo(x));
    assertThat(x.or(zero), equalTo(x));
    assertThat(one.or(zero), equalTo(one));
    assertThat(zero.or(one), equalTo(one));

    // x or one is one.
    assertThat(one.or(one), equalTo(one));
    assertThat(x.or(one), equalTo(one));
    assertThat(one.or(x), equalTo(one));

    // or is commutative.
    assertThat(x.or(y), equalTo(y.or(x)));
    assertThat(y.or(notx), equalTo(notx.or(y)));
    assertThat(z.or(y.or(x)), equalTo(x.or(y).or(z)));

    // x or not x is one.
    assertThat(x.or(notx), equalTo(one));
    assertThat(notx.or(x), equalTo(one));

    // x or x is x.
    assertThat(x.or(x), equalTo(x));
  }

  @Test
  public void testNand() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x nand zero is one
    assertThat(zero.nand(zero), equalTo(one));
    assertThat(zero.nand(x), equalTo(one));
    assertThat(x.nand(zero), equalTo(one));
    assertThat(one.nand(zero), equalTo(one));
    assertThat(zero.nand(one), equalTo(one));

    // x nand one is notx.
    assertThat(one.nand(one), equalTo(zero));
    assertThat(x.nand(one), equalTo(notx));
    assertThat(one.nand(x), equalTo(notx));

    // nand is commutative.
    assertThat(x.nand(y), equalTo(y.nand(x)));
    assertThat(y.nand(notx), equalTo(notx.nand(y)));
    assertThat(z.nand(y.nand(x)), equalTo(x.nand(y).nand(z)));

    // x nand not x is one.
    assertThat(x.nand(notx), equalTo(one));
    assertThat(notx.nand(x), equalTo(one));

    // x nand x is notx.
    assertThat(x.nand(x), equalTo(notx));
  }

  @Test
  public void testNor() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x nor zero is notx
    assertThat(zero.nor(zero), equalTo(one));
    assertThat(zero.nor(x), equalTo(notx));
    assertThat(x.nor(zero), equalTo(notx));
    assertThat(one.nor(zero), equalTo(zero));
    assertThat(zero.nor(one), equalTo(zero));

    // x nor one is zero.
    assertThat(one.nor(one), equalTo(zero));
    assertThat(x.nor(one), equalTo(zero));
    assertThat(one.nor(x), equalTo(zero));

    // nor is commutative.
    assertThat(x.nor(y), equalTo(y.nor(x)));
    assertThat(y.nor(notx), equalTo(notx.nor(y)));
    assertThat(z.nor(y.nor(x)), equalTo(x.nor(y).nor(z)));

    // x nor not x is zero.
    assertThat(x.nor(notx), equalTo(zero));
    assertThat(notx.nor(x), equalTo(zero));

    // x nor x is notx.
    assertThat(x.nor(x), equalTo(notx));
  }

  @Test
  public void testImp() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x imp zero is notx
    assertThat(zero.imp(zero), equalTo(one));
    assertThat(x.imp(zero), equalTo(notx));
    assertThat(one.imp(zero), equalTo(zero));

    // zero imp x is one.
    assertThat(zero.imp(zero), equalTo(one));
    assertThat(zero.imp(one), equalTo(one));
    assertThat(zero.imp(x), equalTo(one));

    // x imp one is one.
    assertThat(zero.imp(one), equalTo(one));
    assertThat(one.imp(one), equalTo(one));
    assertThat(x.imp(one), equalTo(one));

    // one imp x is x.
    assertThat(one.imp(zero), equalTo(zero));
    assertThat(one.imp(one), equalTo(one));
    assertThat(one.imp(x), equalTo(x));

    // imp is not commutative.
    assertThat(x.imp(y), not(equalTo(y.imp(x))));
    assertThat(y.imp(notx), not(equalTo(notx.imp(y))));
    assertThat(z.imp(y.imp(x)), not(equalTo(x.imp(y).imp(z))));

    // imp can be rewritten as or
    assertThat(x.imp(y), equalTo(notx.or(y)));

    // x imp not x is not x.
    assertThat(x.imp(notx), equalTo(notx));
    assertThat(notx.imp(x), equalTo(x));

    // x imp x is one.
    assertThat(x.imp(x), equalTo(one));
  }

  @Test
  public void testBiimp() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x biimp zero is not x
    assertThat(zero.biimp(zero), equalTo(one));
    assertThat(zero.biimp(x), equalTo(notx));
    assertThat(x.biimp(zero), equalTo(notx));
    assertThat(one.biimp(zero), equalTo(zero));
    assertThat(zero.biimp(one), equalTo(zero));

    // x biimp one is x.
    assertThat(one.biimp(one), equalTo(one));
    assertThat(x.biimp(one), equalTo(x));
    assertThat(one.biimp(x), equalTo(x));

    // biimp is commutative.
    assertThat(x.biimp(y), equalTo(y.biimp(x)));
    assertThat(y.biimp(notx), equalTo(notx.biimp(y)));
    assertThat(z.biimp(y.biimp(x)), equalTo(x.biimp(y).biimp(z)));

    // x biimp not x is zero.
    assertThat(x.biimp(notx), equalTo(zero));
    assertThat(notx.biimp(x), equalTo(zero));

    // x biimp x is one.
    assertThat(x.biimp(x), equalTo(one));
  }

  @Test
  public void testDiff() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x diff zero is x
    assertThat(zero.diff(zero), equalTo(zero));
    assertThat(x.diff(zero), equalTo(x));
    assertThat(one.diff(zero), equalTo(one));

    // zero diff x is zero.
    assertThat(zero.diff(zero), equalTo(zero));
    assertThat(zero.diff(one), equalTo(zero));
    assertThat(zero.diff(x), equalTo(zero));

    // x diff one is zero.
    assertThat(zero.diff(one), equalTo(zero));
    assertThat(one.diff(one), equalTo(zero));
    assertThat(x.diff(one), equalTo(zero));

    // one diff x is not x.
    assertThat(one.diff(zero), equalTo(one));
    assertThat(one.diff(one), equalTo(zero));
    assertThat(one.diff(x), equalTo(notx));

    // diff is not commutative.
    assertThat(x.diff(y), not(equalTo(y.diff(x))));
    assertThat(y.diff(notx), not(equalTo(notx.diff(y))));
    assertThat(z.diff(y.diff(x)), not(equalTo(x.diff(y).diff(z))));

    // diff can be rewritten as and
    assertThat(y.diff(x), equalTo(y.and(notx)));

    // x diff not x is x.
    assertThat(x.diff(notx), equalTo(x));
    assertThat(notx.diff(x), equalTo(notx));

    // x diff x is zero.
    assertThat(x.diff(x), equalTo(zero));
  }

  @Test
  public void testLess() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x less zero is zero
    assertThat(zero.less(zero), equalTo(zero));
    assertThat(x.less(zero), equalTo(zero));
    assertThat(one.less(zero), equalTo(zero));

    // zero less x is x.
    assertThat(zero.less(zero), equalTo(zero));
    assertThat(zero.less(one), equalTo(one));
    assertThat(zero.less(x), equalTo(x));

    // x less one is not x.
    assertThat(zero.less(one), equalTo(one));
    assertThat(one.less(one), equalTo(zero));
    assertThat(x.less(one), equalTo(notx));

    // one less x is zero.
    assertThat(one.less(zero), equalTo(zero));
    assertThat(one.less(one), equalTo(zero));
    assertThat(one.less(x), equalTo(zero));

    // less is not commutative.
    assertThat(x.less(y), not(equalTo(y.less(x))));
    assertThat(y.less(notx), not(equalTo(notx.less(y))));
    assertThat(z.less(y.less(x)), not(equalTo(x.less(y).less(z))));

    // less can be rewritten as and
    assertThat(x.less(y), equalTo(notx.and(y)));

    // x less not x is not x.
    assertThat(x.less(notx), equalTo(notx));
    assertThat(notx.less(x), equalTo(x));

    // x less x is zero.
    assertThat(x.less(x), equalTo(zero));
  }

  @Test
  public void testInvimp() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD z = _factory.ithVar(2);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // x invimp zero is one
    assertThat(zero.invimp(zero), equalTo(one));
    assertThat(x.invimp(zero), equalTo(one));
    assertThat(one.invimp(zero), equalTo(one));

    // zero invimp x is not x.
    assertThat(zero.invimp(zero), equalTo(one));
    assertThat(zero.invimp(one), equalTo(zero));
    assertThat(zero.invimp(x), equalTo(notx));

    // x invimp one is x.
    assertThat(zero.invimp(one), equalTo(zero));
    assertThat(one.invimp(one), equalTo(one));
    assertThat(x.invimp(one), equalTo(x));

    // one invimp x is one.
    assertThat(one.invimp(zero), equalTo(one));
    assertThat(one.invimp(one), equalTo(one));
    assertThat(one.invimp(x), equalTo(one));

    // invimp is not commutative.
    assertThat(x.invimp(y), not(equalTo(y.invimp(x))));
    assertThat(y.invimp(notx), not(equalTo(notx.invimp(y))));
    assertThat(z.invimp(y.invimp(x)), not(equalTo(x.invimp(y).invimp(z))));

    // invimp can be rewritten as or
    assertThat(y.invimp(x), equalTo(y.or(notx)));

    // x invimp not x is x.
    assertThat(x.invimp(notx), equalTo(x));
    assertThat(notx.invimp(x), equalTo(notx));

    // x invimp x is one.
    assertThat(x.invimp(x), equalTo(one));
  }

  @Test
  public void testIte() {
    _factory.setVarNum(10);
    BDD x = _factory.ithVar(0);
    BDD notx = _factory.nithVar(0);
    BDD y = _factory.ithVar(1);
    BDD zero = _factory.zero();
    BDD one = _factory.one();

    // then/else same
    assertThat(x.ite(zero, zero), equalTo(zero));
    assertThat(x.ite(one, one), equalTo(one));
    assertThat(x.ite(x, x), equalTo(x));

    // true/false guard
    assertThat(one.ite(x, y), equalTo(x));
    assertThat(zero.ite(x, y), equalTo(y));

    // true/false in then
    assertThat(x.ite(one, y), equalTo(x.or(y)));
    assertThat(x.ite(zero, y), equalTo(notx.and(y)));

    // true/false in else
    assertThat(x.ite(y, one), equalTo(notx.or(y)));
    assertThat(x.ite(y, zero), equalTo(x.and(y)));
  }

  @Test
  public void testDedupSorted() {
    int[] a1 = {1, 1, 2, 2, 3, 3};
    int[] a2 = {1, 2, 3};
    assertTrue(Arrays.equals(JFactory.dedupSorted(a1), a2));

    // these tests use pointer equality intentionally. no copying if no dupes
    assertEquals(JFactory.dedupSorted(a2), a2);
    int[] a3 = {};
    assertEquals(JFactory.dedupSorted(a3), a3);
    int[] a4 = {1};
    assertEquals(JFactory.dedupSorted(a4), a4);
  }

  @Test
  public void testProject() {
    _factory.setVarNum(10);
    BDD one = _factory.one();
    BDD zero = _factory.zero();

    // vars used for our test constraint
    BDD v2 = _factory.ithVar(2);
    BDD v4 = _factory.ithVar(4);
    BDD v6 = _factory.ithVar(6);
    BDD ite = v2.ite(v4, v6);

    // projecting onto one of the variables in the constraint
    assertEquals(one, ite.project(v2));
    assertEquals(one, ite.project(v4));
    assertEquals(one, ite.project(v6));

    // projecting onto combinations of the variables in the constraint
    assertEquals(v2.imp(v4), ite.project(v2.and(v4)));
    assertEquals(v2.not().imp(v6), ite.project(v2.and(v6)));
    assertEquals(v4.or(v6), ite.project(v4.and(v6)));

    // projecting onto all the variables in the constraint
    assertEquals(ite, ite.project(v2.and(v4).and(v6)));

    // projecting any satisfiable BDD to the empty set (i.e. zero or one) returns one
    assertEquals(one, ite.project(one));
    assertEquals(one, ite.project(zero));

    // projecting the zero BDD to the empty set (i.e. zero or one) returns zero
    assertEquals(zero, zero.project(one));
    assertEquals(zero, zero.project(zero));

    // projecting onto a variable not in the constraint
    assertEquals(one, ite.project(_factory.ithVar(1)));
    assertEquals(one, ite.project(_factory.ithVar(3)));
    assertEquals(one, ite.project(_factory.ithVar(5)));
    assertEquals(one, ite.project(_factory.ithVar(7)));
    assertEquals(one, ite.project(_factory.ithVar(9))); // last var
  }
}
