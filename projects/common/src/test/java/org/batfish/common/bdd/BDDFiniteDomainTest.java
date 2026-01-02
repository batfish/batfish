package org.batfish.common.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
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
    ImmutableBDDInteger var = _pkt.allocateBDDInteger("", 1);
    _exception.expect(IllegalArgumentException.class);
    new BDDFiniteDomain<>(var, ImmutableSet.of(1, 2, 3));
  }

  @Test
  public void testIsValidConstraint() {
    // 1 bit variable
    ImmutableBDDInteger var = _pkt.allocateBDDInteger("", 1);

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
    assertThat(fd.getValueFromAssignment(fd.getConstraintForValue(1)), equalTo(1));
    assertThat(fd.getValueFromAssignment(fd.getConstraintForValue(2)), equalTo(2));
    assertThat(fd.getValueFromAssignment(fd.getConstraintForValue(3)), equalTo(3));
  }

  @Test
  public void testDomainsWithSharedVariable_preferBeforePacketVars() {
    Map<String, BDDFiniteDomain<String>> domains =
        BDDFiniteDomain.domainsWithSharedVariable(
            _pkt, "name", ImmutableMap.of("n1", ImmutableSet.of("v1", "v2")), true);
    assertThat(
        domains.values().iterator().next().getConstraintForValue("v1").var(),
        lessThan(_pkt.getDstIp().value(0).var()));
  }
}
