package org.batfish.datamodel.phc_to_flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.Location;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Test;

/** Tests for {@link SrcIpExtractorDefault} */
public class SrcIpExtractorDefaultTest {
  private static Location LOC = new InterfaceLocation("host1", "interface1");
  private static IpFieldExtractorContext HELPER =
      new IpFieldExtractorContext(
          IpSpaceAssignment.builder().assign(LOC, Ip.parse("1.2.3.4").toIpSpace()).build(),
          MockSpecifierContext.builder().build());

  private static SrcIpExtractorDefault SRC_IP_EXTRACTOR = new SrcIpExtractorDefault(HELPER);

  @Test
  public void testGetValue_inferFromPhc() {
    PacketHeaderConstraints phc = PacketHeaderConstraints.builder().setSrcIp("2.3.4.5").build();
    assertThat(SRC_IP_EXTRACTOR.getValue(phc, LOC), equalTo(Ip.parse("2.3.4.5")));
  }

  @Test
  public void testGetValue_inferFromLocation() {
    PacketHeaderConstraints phc = PacketHeaderConstraints.unconstrained();
    assertThat(SRC_IP_EXTRACTOR.getValue(phc, LOC), equalTo(Ip.parse("1.2.3.4")));
  }

  @Test
  public void testGetValue_default() {
    PacketHeaderConstraints phc = PacketHeaderConstraints.unconstrained();
    assertThat(SRC_IP_EXTRACTOR.getValue(phc, null), equalTo(Ip.parse("8.8.8.8")));
  }
}
