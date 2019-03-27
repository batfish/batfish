package org.batfish.bddreachability.transition;

import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.or;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link Transitions}. */
public class TransitionsTest {
  private BDDPacket _bddPacket = new BDDPacket();

  Transition setSrcIp(Ip value) {
    return Transitions.constraint(_bddPacket.getSrcIp().value(value.asLong()));
  }

  // Any composition containing ZERO is zero
  @Test
  public void composeWithZero() {
    assertThat(compose(ZERO), is(ZERO));
    assertThat(compose(IDENTITY, ZERO, IDENTITY), is(ZERO));
    assertThat(compose(ZERO, ZERO), is(ZERO));
    assertThat(compose(ZERO, setSrcIp(Ip.parse("1.2.3.4"))), is(ZERO));
    assertThat(compose(setSrcIp(Ip.parse("1.2.3.4")), ZERO), is(ZERO));
  }

  // Composing with IDENTITY is a no-op.
  @Test
  public void composeWithIdentity() {
    assertThat(compose(IDENTITY), is(IDENTITY));
    assertThat(compose(IDENTITY, IDENTITY), is(IDENTITY));
    assertThat(
        compose(IDENTITY, setSrcIp(Ip.parse("1.2.3.4"))), equalTo(setSrcIp(Ip.parse("1.2.3.4"))));
    assertThat(
        compose(setSrcIp(Ip.parse("1.2.3.4")), IDENTITY), equalTo(setSrcIp(Ip.parse("1.2.3.4"))));
  }

  // Composing with Compose flattens
  @Test
  public void composeWithCompose() {
    Transition t1 = setSrcIp(Ip.parse("1.2.3.4"));
    Transition t2 = setSrcIp(Ip.parse("1.2.3.5"));
    Transition t3 = setSrcIp(Ip.parse("1.2.3.6"));
    Transition t4 = setSrcIp(Ip.parse("1.2.3.7"));
    Transition c1 = compose(t1, t2);
    Transition c2 = compose(t3, t4);
    // Sanity check data prep
    assertThat(c1, instanceOf(Composite.class));
    assertThat(c2, instanceOf(Composite.class));

    assertThat(compose(c1, c2), is(compose(t1, t2, t3, t4)));
  }

  // Or with ZERO is a no-op
  @Test
  public void orWithZero() {
    assertThat(or(ZERO), is(ZERO));
    assertThat(or(IDENTITY, ZERO), is(IDENTITY));
    assertThat(or(ZERO, ZERO), is(ZERO));
    assertThat(or(ZERO, setSrcIp(Ip.parse("1.2.3.4"))), is(setSrcIp(Ip.parse("1.2.3.4"))));
    assertThat(or(setSrcIp(Ip.parse("1.2.3.4")), ZERO), is(setSrcIp(Ip.parse("1.2.3.4"))));
  }

  // Or with Or flattens and uniquifies
  @Test
  public void orWithOr() {
    Transition t1 = setSrcIp(Ip.parse("1.2.3.4"));
    Transition t2 = setSrcIp(Ip.parse("1.2.3.5"));
    Transition o1 = or(t1, IDENTITY);
    Transition o2 = or(IDENTITY, t2);
    // Sanity check data prep
    assertThat(o1, instanceOf(Or.class));
    assertThat(o2, instanceOf(Or.class));

    assertThat(or(o1, o2), is(or(t1, IDENTITY, t2)));
  }
}
