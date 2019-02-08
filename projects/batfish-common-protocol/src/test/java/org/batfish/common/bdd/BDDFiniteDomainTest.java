package org.batfish.common.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link BDDFiniteDomain}. */
public final class BDDFiniteDomainTest {
  @Rule public ExpectedException _exception = ExpectedException.none();

  private BDDPacket _pkt;

  @Before
  public void setup() {
    _pkt = new BDDPacket();
  }

  @Test
  public void testEmptyDomain() {
    // create a new variable
    assertTrue(new BDDFiniteDomain<>(_pkt, "foo", ImmutableSet.of()).isEmpty());
    // use dst IP
    assertTrue(new BDDFiniteDomain<>(_pkt.getDstIp(), ImmutableSet.of()).isEmpty());
  }

  @Test
  public void testTooLarge() {
    BDDInteger var = _pkt.allocateBDDInteger("", 1, false);
    _exception.expect(IllegalArgumentException.class);
    new BDDFiniteDomain<>(var, ImmutableSet.of(1, 2, 3));
  }

  @Test
  public void testIsValidConstraint() {
    // 1 bit variable
    BDDInteger var = _pkt.allocateBDDInteger("", 1, false);

    // 1 value
    assertThat(
        new BDDFiniteDomain<>(var, ImmutableSet.of(1)).getIsValidConstraint(),
        equalTo(var.getFactory().one()));

    // 2 values
    assertThat(
        new BDDFiniteDomain<>(var, ImmutableSet.of(1, 2)).getIsValidConstraint(),
        equalTo(var.getFactory().one()));
  }

  @Test
  public void testGetValueFromAssignment() {
    BDDFiniteDomain<Integer> fd = new BDDFiniteDomain<>(_pkt, "", ImmutableSet.of(1, 2, 3));
    assertThat(fd.getValueFromAssignment(fd.getConstraintForValue(1)), equalTo(Optional.of(1)));
    assertThat(fd.getValueFromAssignment(fd.getConstraintForValue(2)), equalTo(Optional.of(2)));
    assertThat(fd.getValueFromAssignment(fd.getConstraintForValue(3)), equalTo(Optional.of(3)));
  }
}
