package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.RouteDistinguisher.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link RouteDistinguisher} */
public class RouteDistinguisherTest {
  @Test
  public void testEquals() {
    RouteDistinguisher rd = RouteDistinguisher.from(0L, 1);
    new EqualsTester()
        .addEqualityGroup(rd, rd, RouteDistinguisher.from(0L, 1))
        .addEqualityGroup(RouteDistinguisher.from(0, 1L))
        .addEqualityGroup(RouteDistinguisher.from(Ip.ZERO, 1))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(RouteDistinguisher.from(0, 0L)),
        equalTo(RouteDistinguisher.from(0, 0L)));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    assertThat(
        BatfishObjectMapper.clone(RouteDistinguisher.from(0, 0L), RouteDistinguisher.class),
        equalTo(RouteDistinguisher.from(0, 0L)));

    // Note that there is some type ambiguity and (0L, 0) will be converted into (0, 0L)
    assertThat(
        BatfishObjectMapper.clone(RouteDistinguisher.from(0L, 0), RouteDistinguisher.class),
        equalTo(RouteDistinguisher.from(0, 0L)));

    assertThat(
        BatfishObjectMapper.clone(RouteDistinguisher.from(1L << 17, 2), RouteDistinguisher.class),
        equalTo(RouteDistinguisher.from(1L << 17, 2)));
    assertThat(
        BatfishObjectMapper.clone(
            RouteDistinguisher.from(Ip.parse("8.8.8.8"), 999), RouteDistinguisher.class),
        equalTo(RouteDistinguisher.from(Ip.parse("8.8.8.8"), 999)));
  }

  @Test
  public void testParsing() {
    assertThat(parse("1:1").getValue(), equalTo(4294967297L));
    assertThat(parse("131072:1").getValue(), equalTo(8589934593L));
    assertThat(parse("0.0.0.0:1").getValue(), equalTo(1L));
  }
}
