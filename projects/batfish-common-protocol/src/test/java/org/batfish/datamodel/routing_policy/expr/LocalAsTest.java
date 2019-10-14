package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link LocalAs}. */
public class LocalAsTest {

  @Test
  public void testToString() {
    assertThat(LocalAs.instance().toString(), equalTo("LocalAs"));
  }

  @Test
  public void testEquals() throws IOException {
    new EqualsTester()
        .addEqualityGroup(
            LocalAs.instance(),
            LocalAs.instance(),
            SerializationUtils.clone(LocalAs.instance()),
            BatfishObjectMapper.clone(LocalAs.instance(), AsExpr.class))
        .addEqualityGroup(3L)
        .testEquals();
  }

  @Test
  public void testLocalAs() {
    Configuration c =
        new NetworkFactory()
            .configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Environment e =
        Environment.builder(c)
            .setBgpSessionProperties(
                BgpSessionProperties.builder()
                    .setHeadIp(Ip.ZERO)
                    .setHeadAs(3)
                    .setTailIp(Ip.ZERO)
                    .setTailAs(4)
                    .build())
            .build();
    assertThat(LocalAs.instance().evaluate(e), equalTo(3L));
  }
}
