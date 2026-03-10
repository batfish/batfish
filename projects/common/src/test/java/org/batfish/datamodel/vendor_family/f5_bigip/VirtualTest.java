package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link Virtual}. */
public final class VirtualTest {

  private Virtual.Builder _builder;

  @Before
  public void setup() {
    _builder =
        Virtual.builder()
            .setDescription("d")
            .setDestination("d")
            .setDestinationPort(1)
            .setDisabled(true)
            .setIpForward(true)
            .setIpProtocol(IpProtocol.AHP)
            .setMask(Ip.ZERO)
            .setMask6(Ip6.ZERO)
            .setName("n")
            .setPool("p")
            .setReject(true)
            .setSource(Prefix.ZERO)
            .setSource6(Prefix6.ZERO)
            .setSourceAddressTranslationPool("s")
            .setTranslateAddress(true)
            .setTranslatePort(true)
            .addVlan("v")
            .setVlansEnabled(true);
  }

  @Test
  public void testEquals() {
    Virtual obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setDescription(null).build())
        .addEqualityGroup(_builder.setDestination(null).build())
        .addEqualityGroup(_builder.setDestinationPort(null).build())
        .addEqualityGroup(_builder.setDisabled(null).build())
        .addEqualityGroup(_builder.setIpForward(false).build())
        .addEqualityGroup(_builder.setIpProtocol(null).build())
        .addEqualityGroup(_builder.setMask(null).build())
        .addEqualityGroup(_builder.setMask6(null).build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setPool(null).build())
        .addEqualityGroup(_builder.setReject(false).build())
        .addEqualityGroup(_builder.setSource(null).build())
        .addEqualityGroup(_builder.setSource6(null).build())
        .addEqualityGroup(_builder.setSourceAddressTranslationPool(null).build())
        .addEqualityGroup(_builder.setTranslateAddress(null).build())
        .addEqualityGroup(_builder.setTranslatePort(null).build())
        .addEqualityGroup(_builder.setVlans(ImmutableSet.of()).build())
        .addEqualityGroup(_builder.setVlansEnabled(false).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Virtual obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
