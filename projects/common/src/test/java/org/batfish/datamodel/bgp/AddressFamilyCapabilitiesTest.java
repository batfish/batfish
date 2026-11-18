package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.AddressFamilyCapabilities.builder;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.ALWAYS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities.Builder;
import org.junit.jupiter.api.Test;

/** Tests of {@link AddressFamilyCapabilities} */
class AddressFamilyCapabilitiesTest {

  @Test
  void testEquals() {
    Builder builder = builder();
    AddressFamilyCapabilities afs = builder.build();
    new EqualsTester()
        .addEqualityGroup(afs, afs, builder.build())
        .addEqualityGroup(builder.setAdditionalPathsReceive(true).build())
        .addEqualityGroup(builder.setAdditionalPathsSelectAll(true).build())
        .addEqualityGroup(builder.setAdditionalPathsSend(true).build())
        .addEqualityGroup(builder.setAdvertiseExternal(true).build())
        .addEqualityGroup(builder.setAdvertiseInactive(true).build())
        .addEqualityGroup(builder.setAllowLocalAsIn(true).build())
        .addEqualityGroup(builder.setAllowRemoteAsOut(ALWAYS).build())
        .addEqualityGroup(builder.setSendCommunity(true).build())
        .addEqualityGroup(builder.setSendExtendedCommunity(true).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    AddressFamilyCapabilities afs =
        builder()
            .setAdditionalPathsReceive(true)
            .setAdditionalPathsSelectAll(true)
            .setAdditionalPathsSend(true)
            .setAdvertiseExternal(true)
            .setAdvertiseInactive(true)
            .setAllowLocalAsIn(true)
            .setAllowRemoteAsOut(ALWAYS)
            .setSendCommunity(true)
            .setSendExtendedCommunity(true)
            .build();
    assertThat(SerializationUtils.clone(afs), equalTo(afs));
  }

  @Test
  void testJsonSerialization() {
    AddressFamilyCapabilities afs =
        builder()
            .setAdditionalPathsReceive(true)
            .setAdditionalPathsSelectAll(true)
            .setAdditionalPathsSend(true)
            .setAdvertiseExternal(true)
            .setAdvertiseInactive(true)
            .setAllowLocalAsIn(true)
            .setAllowRemoteAsOut(ALWAYS)
            .setSendCommunity(true)
            .setSendExtendedCommunity(true)
            .build();
    assertThat(BatfishObjectMapper.clone(afs, AddressFamilyCapabilities.class), equalTo(afs));
  }
}
