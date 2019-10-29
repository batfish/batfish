package org.batfish.datamodel.phc_to_flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.junit.Test;

/** Tests for {@link IpProtocolExtractorDefaultTcp} */
public class IpProtocolExtractorDefaultTcpTest {
  @Test
  public void testGetValue_inferFromPhc() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder().setIpProtocols(ImmutableSet.of(IpProtocol.UDP)).build();
    assertThat(IpProtocolExtractorDefaultTcp.INSTANCE.getValue(phc, null), equalTo(IpProtocol.UDP));
  }

  @Test
  public void testGetValue_multipleProtocol() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder()
            .setIpProtocols(ImmutableSet.of(IpProtocol.UDP, IpProtocol.TCP))
            .build();
    assertThat(IpProtocolExtractorDefaultTcp.INSTANCE.getValue(phc, null), equalTo(IpProtocol.UDP));
  }

  @Test
  public void testGetValue_noPhc() {
    PacketHeaderConstraints phc = PacketHeaderConstraints.unconstrained();
    assertThat(IpProtocolExtractorDefaultTcp.INSTANCE.getValue(phc, null), equalTo(IpProtocol.TCP));
  }
}
