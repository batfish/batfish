package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Test of {@link LiteralIp}. */
public final class LiteralIpTest {

  @Test
  public void testJavaSerialization() {
    IpExpr obj = LiteralIp.of(Ip.ZERO);
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    IpExpr obj = LiteralIp.of(Ip.ZERO);
    assertEquals(obj, BatfishObjectMapper.clone(obj, IpExpr.class));
  }

  @Test
  public void testEquals() {
    IpExpr obj = LiteralIp.of(Ip.ZERO);
    new EqualsTester()
        .addEqualityGroup(obj, LiteralIp.of(Ip.ZERO))
        .addEqualityGroup(LiteralIp.of(Ip.MAX))
        .testEquals();
  }

  @Test
  public void testEvaluate() {
    LiteralIp obj = LiteralIp.of(Ip.ZERO);
    Configuration c = Configuration.builder().setHostname("h").build();
    Environment env = Environment.builder(c).build();

    assertThat(obj.evaluate(env), equalTo(Ip.ZERO));
  }
}
