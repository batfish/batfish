package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
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
    SetOrigin so = new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE));
    new EqualsTester()
        .addEqualityGroup(so, so, new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE)))
        .addEqualityGroup(new SetOrigin(new LiteralOrigin(OriginType.IGP)))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    SetOrigin so = new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE));
    assertThat(SerializationUtils.clone(so), equalTo(so));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    SetOrigin so = new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE));
    assertThat(BatfishObjectMapper.clone(so, SetOrigin.class), equalTo(so));
  }

  @Test
  public void testExecuteNonBgpRoute() {
    SetOrigin so = new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE));
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
