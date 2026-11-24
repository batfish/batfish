package org.batfish.datamodel.phc_to_flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.junit.jupiter.api.Test;

/** Tests for {@link IpProtocolExtractorDefaultTcp} */
class IpProtocolExtractorDefaultTcpTest {
  @Test
  void testGetValue_inferFromPhc() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder().setIpProtocols(ImmutableSet.of(IpProtocol.UDP)).build();
    assertThat(IpProtocolExtractorDefaultTcp.INSTANCE.getValue(phc, null), equalTo(IpProtocol.UDP));
  }

  @Test
  void testGetValue_multipleProtocol() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder()
            .setIpProtocols(ImmutableSet.of(IpProtocol.UDP, IpProtocol.TCP))
            .build();
    assertThat(IpProtocolExtractorDefaultTcp.INSTANCE.getValue(phc, null), equalTo(IpProtocol.UDP));
  }

  @Test
  void testGetValue_noPhc() {
    PacketHeaderConstraints phc = PacketHeaderConstraints.unconstrained();
    assertThat(IpProtocolExtractorDefaultTcp.INSTANCE.getValue(phc, null), equalTo(IpProtocol.TCP));
  }
}
