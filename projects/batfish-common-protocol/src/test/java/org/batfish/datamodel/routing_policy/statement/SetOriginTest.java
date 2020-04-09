package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.junit.Test;

/** Tests of {@link SetOrigin} */
public class SetOriginTest {
  @Test
  public void testEquals() {
    SetOrigin so = new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, 1L));
    new EqualsTester()
        .addEqualityGroup(so, so, new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, 1L)))
        .addEqualityGroup(new SetOrigin(new LiteralOrigin(OriginType.IGP, 1L)))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    SetOrigin so = new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, 1L));
    assertThat(SerializationUtils.clone(so), equalTo(so));
  }

  @Test
  public void testJsonSerialization() {
    SetOrigin so = new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, 1L));
    assertThat(BatfishObjectMapper.clone(so, SetOrigin.class), equalTo(so));
  }

  @Test
  public void testExecuteNonBgpRoute() {
    SetOrigin so = new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, 1L));
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_NX)
            .setHostname("c")
            .build();
    // Do not crash
    assertThat(so.execute(Environment.builder(c).build()), notNullValue());
  }
}
