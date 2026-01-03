package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.Ip6SpaceMatchers.containsIp6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class UniverseIp6SpaceTest {

  @Test
  public void testSingletonBehavior() {
    assertThat(UniverseIp6Space.INSTANCE, sameInstance(UniverseIp6Space.INSTANCE));
  }

  @Test
  public void testInterning() {
    assertThat(
        BatfishObjectMapper.clone(UniverseIp6Space.INSTANCE, Ip6Space.class),
        sameInstance(UniverseIp6Space.INSTANCE));
    assertThat(
        SerializationUtils.clone(UniverseIp6Space.INSTANCE),
        sameInstance(UniverseIp6Space.INSTANCE));
  }

  @Test
  public void testComplement() {
    assertThat(UniverseIp6Space.INSTANCE.complement(), equalTo(EmptyIp6Space.INSTANCE));
  }

  @Test
  public void testContainsIp() {
    // UniverseIp6Space should contain all IPs
    assertThat(UniverseIp6Space.INSTANCE, containsIp6(Ip6.ZERO));
    assertThat(UniverseIp6Space.INSTANCE, containsIp6(Ip6.MAX));
    assertThat(UniverseIp6Space.INSTANCE, containsIp6(Ip6.parse("2001:db8::1")));
    assertThat(UniverseIp6Space.INSTANCE, containsIp6(Ip6.parse("::1")));
    assertThat(UniverseIp6Space.INSTANCE, containsIp6(Ip6.parse("fe80::1")));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(UniverseIp6Space.INSTANCE, UniverseIp6Space.INSTANCE)
        .addEqualityGroup(EmptyIp6Space.INSTANCE)
        .testEquals();
  }
}
