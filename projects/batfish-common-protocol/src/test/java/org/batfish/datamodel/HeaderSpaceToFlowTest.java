package org.batfish.datamodel;

import static org.batfish.datamodel.PacketHeaderConstraintsUtil.DEFAULT_PACKET_LENGTH;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.batfish.common.bdd.BDDFlowConstraintGenerator.FlowPreference;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.IpSpaceReference;
import org.batfish.datamodel.Flow.Builder;
import org.junit.Test;

/** Tests for {@link HeaderSpaceToFlow} */
public class HeaderSpaceToFlowTest {

  @Test
  public void testGetRepresentativeFlow_withoutIpSpaces() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(ImmutableMap.of(), FlowPreference.APPLICATION);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder().setSrcIps(Ip.parse("1.2.3.4").toIpSpace()).build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getSrcIp(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(DEFAULT_PACKET_LENGTH));
  }

  @Test
  public void testGetRepresentativeFlow_withPacketLength() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(
            ImmutableMap.of("ipSpace", Ip.parse("1.2.3.4").toIpSpace()),
            FlowPreference.APPLICATION);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder()
                .setPacketLengths(ImmutableList.of(SubRange.singleton(20)))
                .setSrcIps(new IpSpaceReference("ipSpace"))
                .build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getSrcIp(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(20));
  }

  @Test
  public void testGetRepresentativeFlow_withIpSpaces() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(
            ImmutableMap.of("ipSpace", Ip.parse("1.2.3.4").toIpSpace()),
            FlowPreference.APPLICATION);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder().setSrcIps(new IpSpaceReference("ipSpace")).build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getSrcIp(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(DEFAULT_PACKET_LENGTH));
  }

  @Test
  public void testGetRepresentativeFlow_testFilterPrefAllFields() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(ImmutableMap.of(), FlowPreference.TESTFILTER);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder()
                .setDstIps(Ip.parse("1.1.1.1").toIpSpace())
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(SubRange.singleton(1))
                .setSrcPorts(SubRange.singleton(2))
                .build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getDstIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(flowBuilder.get().getIpProtocol(), equalTo(IpProtocol.UDP));
    assertThat(flowBuilder.get().getDstPort(), equalTo(1));
    assertThat(flowBuilder.get().getSrcPort(), equalTo(2));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(DEFAULT_PACKET_LENGTH));
  }

  @Test
  public void testGetRepresentativeFlow_testFilterPrefNoDstIp() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(ImmutableMap.of(), FlowPreference.TESTFILTER);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(SubRange.singleton(1))
                .setSrcPorts(SubRange.singleton(2))
                .build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getDstIp(), equalTo(Ip.parse("8.8.8.8")));
    assertThat(flowBuilder.get().getIpProtocol(), equalTo(IpProtocol.UDP));
    assertThat(flowBuilder.get().getDstPort(), equalTo(1));
    assertThat(flowBuilder.get().getSrcPort(), equalTo(2));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(DEFAULT_PACKET_LENGTH));
  }

  @Test
  public void testGetRepresentativeFlow_testFilterPrefNoIpProtocol() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(ImmutableMap.of(), FlowPreference.TESTFILTER);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder()
                .setDstIps(Ip.parse("1.1.1.1").toIpSpace())
                .setDstPorts(SubRange.singleton(1))
                .setSrcPorts(SubRange.singleton(2))
                .build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getDstIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(flowBuilder.get().getIpProtocol(), equalTo(IpProtocol.TCP));
    assertThat(flowBuilder.get().getDstPort(), equalTo(1));
    assertThat(flowBuilder.get().getSrcPort(), equalTo(2));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(DEFAULT_PACKET_LENGTH));
  }

  @Test
  public void testGetRepresentativeFlow_testFilterPrefNoDstPort() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(ImmutableMap.of(), FlowPreference.TESTFILTER);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder()
                .setDstIps(Ip.parse("1.1.1.1").toIpSpace())
                .setIpProtocols(IpProtocol.UDP)
                .setSrcPorts(SubRange.singleton(2))
                .build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getDstIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(flowBuilder.get().getIpProtocol(), equalTo(IpProtocol.UDP));
    assertThat(flowBuilder.get().getDstPort(), equalTo(NamedPort.HTTP.number()));
    assertThat(flowBuilder.get().getSrcPort(), equalTo(2));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(DEFAULT_PACKET_LENGTH));
  }

  @Test
  public void testGetRepresentativeFlow_testFilterPrefNoSrcPort() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(ImmutableMap.of(), FlowPreference.TESTFILTER);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder()
                .setDstIps(Ip.parse("1.1.1.1").toIpSpace())
                .setIpProtocols(IpProtocol.UDP)
                .setDstPorts(SubRange.singleton(1))
                .build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getDstIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(flowBuilder.get().getIpProtocol(), equalTo(IpProtocol.UDP));
    assertThat(flowBuilder.get().getDstPort(), equalTo(1));
    assertThat(flowBuilder.get().getSrcPort(), equalTo(NamedPort.EPHEMERAL_LOWEST.number()));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(DEFAULT_PACKET_LENGTH));
  }

  @Test
  public void testGetRepresentativeFlow_testFilterPrefICMP() {
    HeaderSpaceToFlow headerSpaceToFlow =
        new HeaderSpaceToFlow(ImmutableMap.of(), FlowPreference.TESTFILTER);
    Optional<Builder> flowBuilder =
        headerSpaceToFlow.getRepresentativeFlow(
            HeaderSpace.builder()
                .setDstIps(Ip.parse("1.1.1.1").toIpSpace())
                .setIpProtocols(IpProtocol.ICMP)
                .build());

    assertTrue(flowBuilder.isPresent());
    assertThat(flowBuilder.get().getDstIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(flowBuilder.get().getIpProtocol(), equalTo(IpProtocol.ICMP));
    assertThat(flowBuilder.get().getPacketLength(), equalTo(DEFAULT_PACKET_LENGTH));
  }
}
