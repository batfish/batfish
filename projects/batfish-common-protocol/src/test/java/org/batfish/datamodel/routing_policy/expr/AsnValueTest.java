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

/** Tests of {@link AsnValue}. */
public class AsnValueTest {
  @Test
  public void testToString() {
    assertThat(AsnValue.of(RemoteAs.instance()).toString(), equalTo("AsnValue{asExpr=RemoteAs}"));
  }

  @Test
  public void testEquals() throws IOException {
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
            .setBgpSessionProperties(
                BgpSessionProperties.builder()
                    .setHeadIp(Ip.ZERO)
                    .setHeadAs(3)
                    .setTailIp(Ip.ZERO)
                    .setTailAs(4)
                    .build())
            .build();
    assertThat(AsnValue.of(LocalAs.instance()).evaluate(e), equalTo(3L));
    assertThat(AsnValue.of(RemoteAs.instance()).evaluate(e), equalTo(4L));
  }
}
