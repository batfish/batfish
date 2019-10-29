package org.batfish.datamodel.phc_to_flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Range;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.junit.Test;

/** Tests for {@link IcmpCodeExtractorDefault} */
public class IcmpCodeExtractorDefaultTest {
  @Test
  public void testGetValue_inferFromPhc() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder()
            .setIcmpCodes(IntegerSpace.of(Range.closed(10, 20)))
            .build();
    assertThat(IcmpCodeExtractorDefault.ZERO.getValue(phc, null), equalTo(10));
  }

  @Test
  public void testGetValue_noPhc() {
    PacketHeaderConstraints phc = PacketHeaderConstraints.unconstrained();
    assertThat(IcmpCodeExtractorDefault.ZERO.getValue(phc, null), equalTo(0));
  }
}
