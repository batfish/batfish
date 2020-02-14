package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.ip.Ip;
import org.batfish.datamodel.packet_policy.FibLookupOverrideLookupIp.Builder;
import org.junit.Test;

/** Tests of {@link FibLookupOverrideLookupIp} */
public class FibLookupOverrideLookupIpTest {
  @Test
  public void testEquals() {
    Builder builder =
        FibLookupOverrideLookupIp.builder()
            .setIps(ImmutableList.of(Ip.ZERO))
            .setVrfExpr(IngressInterfaceVrf.instance())
            .setRequireConnected(true)
            .setDefaultAction(Drop.instance());
    FibLookupOverrideLookupIp fl = builder.build();

    new EqualsTester()
        .addEqualityGroup(fl, fl, builder.build())
        .addEqualityGroup(builder.setIps(ImmutableList.of(Ip.MAX)).build())
        .addEqualityGroup(builder.setVrfExpr(new LiteralVrfName("avrf")).build())
        .addEqualityGroup(
            builder.setDefaultAction(new FibLookup(IngressInterfaceVrf.instance())).build())
        .addEqualityGroup(builder.setRequireConnected(false).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    FibLookupOverrideLookupIp fl =
        FibLookupOverrideLookupIp.builder()
            .setIps(ImmutableList.of(Ip.ZERO))
            .setVrfExpr(IngressInterfaceVrf.instance())
            .setRequireConnected(true)
            .setDefaultAction(Drop.instance())
            .build();
    assertThat(SerializationUtils.clone(fl), equalTo(fl));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    FibLookupOverrideLookupIp fl =
        FibLookupOverrideLookupIp.builder()
            .setIps(ImmutableList.of(Ip.ZERO))
            .setVrfExpr(IngressInterfaceVrf.instance())
            .setRequireConnected(true)
            .setDefaultAction(Drop.instance())
            .build();
    assertThat(BatfishObjectMapper.clone(fl, FibLookupOverrideLookupIp.class), equalTo(fl));
  }
}
