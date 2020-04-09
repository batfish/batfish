package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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

/** Tests of {@link RemoteAs}. */
public class RemoteAsTest {

  @Test
  public void testToString() {
    assertThat(RemoteAs.instance().toString(), equalTo("RemoteAs"));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            RemoteAs.instance(),
            RemoteAs.instance(),
            SerializationUtils.clone(RemoteAs.instance()),
            BatfishObjectMapper.clone(RemoteAs.instance(), AsExpr.class))
        .addEqualityGroup(3L)
        .testEquals();
  }

  @Test
  public void testRemoteAs() {
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
    assertThat(RemoteAs.instance().evaluate(e), equalTo(4L));
  }
}
