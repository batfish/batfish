package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.Ip6SpaceMatchers.containsIp6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class EmptyIp6SpaceTest {

  @Test
  public void testSingletonBehavior() {
    assertThat(EmptyIp6Space.INSTANCE, sameInstance(EmptyIp6Space.INSTANCE));
  }

  @Test
  public void testInterning() {
    assertThat(
        BatfishObjectMapper.clone(EmptyIp6Space.INSTANCE, Ip6Space.class),
        sameInstance(EmptyIp6Space.INSTANCE));
    assertThat(
        SerializationUtils.clone(EmptyIp6Space.INSTANCE), sameInstance(EmptyIp6Space.INSTANCE));
  }

  @Test
  public void testComplement() {
    assertThat(EmptyIp6Space.INSTANCE.complement(), equalTo(UniverseIp6Space.INSTANCE));
  }

  @Test
  public void testContainsIp() {
    // EmptyIp6Space should not contain any IP
    assertThat(EmptyIp6Space.INSTANCE, not(containsIp6(Ip6.ZERO)));
    assertThat(EmptyIp6Space.INSTANCE, not(containsIp6(Ip6.MAX)));
    assertThat(EmptyIp6Space.INSTANCE, not(containsIp6(Ip6.parse("2001:db8::1"))));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EmptyIp6Space.INSTANCE, EmptyIp6Space.INSTANCE)
        .addEqualityGroup(UniverseIp6Space.INSTANCE)
        .testEquals();
  }
}
