package org.batfish.datamodel.phc_to_flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.ip.Ip;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.Location;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Test;

/** Tests for {@link PacketHeaderConstraintToFlowConverter} */
public class PacketHeaderConstraintToFlowConverterTest {
  private static Location LOC = new InterfaceLocation("host1", "interface1");
  private static IpFieldExtractorContext HELPER =
      new IpFieldExtractorContext(
          IpSpaceAssignment.builder().assign(LOC, Ip.parse("1.2.3.4").toIpSpace()).build(),
          MockSpecifierContext.builder().build());

  private static SrcIpExtractorDefault SRC_IP_EXTRACTOR = new SrcIpExtractorDefault(HELPER);
  private static DstIpExtractorDefault DST_IP_EXTRACTOR = new DstIpExtractorDefault(HELPER);

  @Test
  public void testToFlow() {
    PacketHeaderConstraintToFlowConverter converter =
        PacketHeaderConstraintToFlowConverter.builder()
            .setSrcIpExtractor(SRC_IP_EXTRACTOR)
            .setDstIpExtractor(DST_IP_EXTRACTOR)
            .build();

    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder().setSrcIp("2.3.4.5").setDstIp("1.2.3.4").build();
    Flow flow = converter.toFlow(phc, LOC).setIngressNode("host1").setIngressVrf("vrf1").build();
    Flow expectedFlow =
        Flow.builder()
            .setSrcIp(Ip.parse("2.3.4.5"))
            .setDstIp(Ip.parse("1.2.3.4"))
            .setIngressNode("host1")
            .setIngressVrf("vrf1")
            .build();
    assertThat(flow, equalTo(expectedFlow));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToFlow_noSrcIpExtractor() {
    PacketHeaderConstraintToFlowConverter.builder().setDstIpExtractor(DST_IP_EXTRACTOR).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToFlow_noDstIpExtractor() {
    PacketHeaderConstraintToFlowConverter.builder().setSrcIpExtractor(SRC_IP_EXTRACTOR).build();
  }
}
