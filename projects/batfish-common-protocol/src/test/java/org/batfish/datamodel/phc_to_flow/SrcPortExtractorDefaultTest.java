package org.batfish.datamodel.phc_to_flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.junit.Test;

/** Tests for {@link SrcPortExtractorDefault} */
public class SrcPortExtractorDefaultTest {
  @Test
  public void testGetValue_inferFromPhc() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder().setSrcPorts(IntegerSpace.PORTS).build();
    assertThat(SrcPortExtractorDefault.INSTANCE.getValue(phc, null), equalTo(0));
  }

  @Test
  public void testGetValue_noPhc() {
    PacketHeaderConstraints phc = PacketHeaderConstraints.unconstrained();
    assertThat(
        SrcPortExtractorDefault.INSTANCE.getValue(phc, null),
        equalTo(NamedPort.EPHEMERAL_LOWEST.number()));
  }
}
