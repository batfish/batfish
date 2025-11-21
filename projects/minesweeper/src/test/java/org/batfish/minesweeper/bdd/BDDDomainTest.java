package org.batfish.minesweeper.bdd;

import static org.batfish.common.bdd.BDDMatchers.isOne;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.JFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.MutableBDDInteger;
import org.junit.Test;

public class BDDDomainTest {
  @Test
  public void testEquals() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);
    BDDDomain<String> base = new BDDDomain<>(factory, ImmutableList.of("a", "b"), 0);

    new EqualsTester()
        .addEqualityGroup(
            base,
            new BDDDomain<>(factory, ImmutableList.of("a", "b"), 0),
            // Documentation test that values are ignored.
            new BDDDomain<>(factory, ImmutableList.of("b", "c"), 0),
            new BDDDomain<>(base))
        .addEqualityGroup(new BDDDomain<>(factory, ImmutableList.of("a", "b", "c"), 0))
        .addEqualityGroup(new BDDDomain<>(factory, ImmutableList.of("a", "b"), 1))
        .addEqualityGroup(new BDDDomain<>(factory.nithVar(4), base))
        .testEquals();
  }

  @Test
  public void testIsValidConstraint() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);

    // TODO: do domains of size 0 or 1 make sense? They have no underlying bits, so
    // they cannot represent a constraint on the input.
    BDDDomain<String> zero = new BDDDomain<>(factory, ImmutableList.of(), 0);
    assertThat(zero.getIsValidConstraint(), isOne());

    BDDDomain<String> one = new BDDDomain<>(factory, ImmutableList.of("a"), 0);
    assertThat(one.getIsValidConstraint(), isOne());
    assertThat(one.getIsValidConstraint(), equalTo(one.value("a")));

    BDDDomain<String> two = new BDDDomain<>(factory, ImmutableList.of("a", "b"), 0);
    assertThat(two.getIsValidConstraint(), isOne());

    BDDDomain<String> three = new BDDDomain<>(factory, ImmutableList.of("a", "b", "c"), 0);
    assertThat(three.getIsValidConstraint(), not(isOne()));
    assertThat(three.getIsValidConstraint().or(factory.ithVar(0).and(factory.ithVar(1))), isOne());
  }

  @Test
  public void testValue() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);
    BDDDomain<String> two = new BDDDomain<>(factory, ImmutableList.of("a", "b"), 0);
    assertThat(two.getIsValidConstraint(), isOne());

    assertThat(two.value("a"), equalTo(factory.nithVar(0)));
    assertThat(two.value("b"), equalTo(factory.ithVar(0)));

    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> two.value("c"));
    assertThat(thrown, hasMessage(equalTo("c is not in the domain [a, b]")));
  }

  @Test
  public void testSetValue() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);
    BDDDomain<String> two = new BDDDomain<>(factory, ImmutableList.of("a", "b"), 0);
    BDD inputA = two.value("a");
    BDD inputB = two.value("b");
    MutableBDDInteger integer = two.getInteger();

    // setValue("a") means that the output will be "a" for all inputs.
    two.setValue("a");
    assertThat(integer.satAssignmentToInt(factory.one()), equalTo(0));
    assertThat(two.satAssignmentToValue(inputA), equalTo("a"));
    assertThat(two.satAssignmentToValue(inputB), equalTo("a"));
  }

  @Test
  public void testSupport() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(5);

    BDDDomain<String> two = new BDDDomain<>(factory, ImmutableList.of("a", "b"), 0);
    BDD s1 = two.support();
    assertThat(s1, equalTo(factory.andAll(two.getInteger().support())));

    two.setValue("a");
    BDD s2 = two.support();
    assertThat(s2, equalTo(factory.one()));
  }

  @Test
  public void testAugmentPairing() {
    BDDFactory factory = BDDPacket.defaultFactory(JFactory::init);
    factory.setVarNum(10);

    // x represents an unknown element of the set {"a", "b", "c"}
    BDDDomain<String> x = new BDDDomain<>(factory, ImmutableList.of("a", "b", "c"), 0);
    // valueA represents the value "a"
    BDDDomain<String> valueA = new BDDDomain<>(factory, ImmutableList.of("a", "b", "c"), 0);
    valueA.setValue("a");

    // map x to valueA
    BDDPairing pairing1 = factory.makePair();
    valueA.augmentPairing(x, pairing1);

    // map x's underlying BDD integer to valueA's underlying BDD integer
    BDDPairing pairing2 = factory.makePair();
    valueA.getInteger().augmentPairing(x.getInteger(), pairing2);

    // check that these mappings are equivalent by comparing the results of applying the two
    // mappings to the BDD variables that constitute x (i.e., x's support)
    assertEquals(x.support().veccompose(pairing1), x.support().veccompose(pairing2));
  }
}
