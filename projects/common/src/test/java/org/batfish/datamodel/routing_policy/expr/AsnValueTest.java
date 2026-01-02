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
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.junit.Test;

/** Tests of {@link AsnValue}. */
public class AsnValueTest {
  @Test
  public void testToString() {
    assertThat(AsnValue.of(RemoteAs.instance()).toString(), equalTo("AsnValue{asExpr=RemoteAs}"));
  }

  @Test
  public void testEquals() {
    AsnValue tester = AsnValue.of(RemoteAs.instance());
    new EqualsTester()
        .addEqualityGroup(
            tester,
            AsnValue.of(RemoteAs.instance()),
            SerializationUtils.clone(tester),
            BatfishObjectMapper.clone(tester, LongExpr.class))
        .addEqualityGroup(AsnValue.of(LocalAs.instance()))
        .addEqualityGroup(3L)
        .testEquals();
  }

  @Test
  public void testAsnValue() {
    Configuration c =
        new NetworkFactory()
            .configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Environment e =
        Environment.builder(c)
            .setDirection(Direction.IN)
            .setBgpSessionProperties(
                BgpSessionProperties.builder()
                    .setRemoteIp(Ip.ZERO)
                    .setRemoteAs(3)
                    .setLocalIp(Ip.ZERO)
                    .setLocalAs(4)
                    .build())
            .build();
    assertThat(AsnValue.of(LocalAs.instance()).evaluate(e), equalTo(4L));
    assertThat(AsnValue.of(RemoteAs.instance()).evaluate(e), equalTo(3L));
  }
}
