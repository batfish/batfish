package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link IspFilter} */
public class IspFilterTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))),
            new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("1.1.1.1"))))
        .addEqualityGroup(
            new IspFilter(ImmutableList.of(1234L), ImmutableList.of(Ip.parse("2.2.2.2"))))
        .addEqualityGroup(
            new IspFilter(ImmutableList.of(5678L), ImmutableList.of(Ip.parse("1.1.1.1"))))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    IspFilter ispFilter =
        new IspFilter(ImmutableList.of(5678L), ImmutableList.of(Ip.parse("1.1.1.1")));

    assertThat(BatfishObjectMapper.clone(ispFilter, IspFilter.class), equalTo(ispFilter));
  }
}
