package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
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
  public void testEquals() {
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
                    .setRemoteIp(Ip.ZERO)
                    .setRemoteAs(3)
                    .setLocalIp(Ip.ZERO)
                    .setLocalAs(4)
                    .build())
            .build();
    assertThat(LocalAs.instance().evaluate(e), equalTo(4L));
  }
}
