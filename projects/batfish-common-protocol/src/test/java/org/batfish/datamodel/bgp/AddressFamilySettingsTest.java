package org.batfish.datamodel.bgp;

import static org.batfish.datamodel.bgp.AddressFamilySettings.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.AddressFamilySettings.Builder;
import org.junit.Test;

/** Tests of {@link AddressFamilySettings} */
public class AddressFamilySettingsTest {

  @Test
  public void testEquals() {
    Builder builder = builder();
    AddressFamilySettings afs = builder.build();
    new EqualsTester()
        .addEqualityGroup(afs, afs, builder.build())
        .addEqualityGroup(builder.setAdditionalPathsReceive(true).build())
        .addEqualityGroup(builder.setAdditionalPathsSelectAll(true).build())
        .addEqualityGroup(builder.setAdditionalPathsSend(true).build())
        .addEqualityGroup(builder.setAdvertiseExternal(true).build())
        .addEqualityGroup(builder.setAdvertiseInactive(true).build())
        .addEqualityGroup(builder.setAllowLocalAsIn(true).build())
        .addEqualityGroup(builder.setAllowRemoteAsOut(true).build())
        .addEqualityGroup(builder.setSendCommunity(true).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    AddressFamilySettings afs =
        builder()
            .setAdditionalPathsReceive(true)
            .setAdditionalPathsSelectAll(true)
            .setAdditionalPathsSend(true)
            .setAdvertiseExternal(true)
            .setAdvertiseInactive(true)
            .setAllowLocalAsIn(true)
            .setAllowRemoteAsOut(true)
            .setSendCommunity(true)
            .build();
    assertThat(SerializationUtils.clone(afs), equalTo(afs));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    AddressFamilySettings afs =
        builder()
            .setAdditionalPathsReceive(true)
            .setAdditionalPathsSelectAll(true)
            .setAdditionalPathsSend(true)
            .setAdvertiseExternal(true)
            .setAdvertiseInactive(true)
            .setAllowLocalAsIn(true)
            .setAllowRemoteAsOut(true)
            .setSendCommunity(true)
            .build();
    assertThat(BatfishObjectMapper.clone(afs, AddressFamilySettings.class), equalTo(afs));
  }
}
