package org.batfish.datamodel;

import static org.batfish.datamodel.RouteFilterLine.PERMIT_ALL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link RouteFilterLine} */
public class RouteFilterLineTest {
  @Test
  public void testEquals() {
    RouteFilterLine rfl =
        new RouteFilterLine(LineAction.PERMIT, IpWildcard.parse("1.1.1.1"), new SubRange(30, 32));
    new EqualsTester()
        .addEqualityGroup(
            rfl,
            rfl,
            new RouteFilterLine(
                LineAction.PERMIT, IpWildcard.parse("1.1.1.1"), new SubRange(30, 32)))
        .addEqualityGroup(
            new RouteFilterLine(LineAction.DENY, IpWildcard.parse("1.1.1.1"), new SubRange(30, 32)))
        .addEqualityGroup(
            new RouteFilterLine(
                LineAction.PERMIT, IpWildcard.parse("2.2.2.2"), new SubRange(30, 32)))
        .addEqualityGroup(
            new RouteFilterLine(
                LineAction.PERMIT, IpWildcard.parse("2.2.2.2"), new SubRange(29, 32)))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    RouteFilterLine rfl =
        new RouteFilterLine(LineAction.PERMIT, IpWildcard.parse("1.1.1.1"), new SubRange(30, 32));
    assertThat(SerializationUtils.clone(rfl), equalTo(rfl));
  }

  @Test
  public void testJsonSerialization() {
    RouteFilterLine rfl =
        new RouteFilterLine(LineAction.PERMIT, IpWildcard.parse("1.1.1.1"), new SubRange(30, 32));
    assertThat(BatfishObjectMapper.clone(rfl, RouteFilterLine.class), equalTo(rfl));
  }

  @Test
  public void testPermitAll() {
    RouteFilterList rfl = new RouteFilterList("name", ImmutableList.of(PERMIT_ALL));
    for (int prefixLen = 0; prefixLen <= Prefix.MAX_PREFIX_LENGTH; prefixLen++) {
      assertTrue(rfl.permits(Prefix.create(Ip.parse("1.1.1.1"), prefixLen)));
    }
  }
}
