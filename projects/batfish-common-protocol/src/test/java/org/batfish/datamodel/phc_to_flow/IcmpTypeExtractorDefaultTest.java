package org.batfish.datamodel.phc_to_flow;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import com.google.common.collect.Range;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.junit.Test;

/** Tests for {@link IcmpTypeExtractorDefault} */
public class IcmpTypeExtractorDefaultTest {
  @Test
  public void testGetValue_inferFromPhc() {
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder()
            .setIcmpTypes(IntegerSpace.of(Range.closed(10, 20)))
            .build();
    assertThat(IcmpTypeExtractorDefault.REQUEST.getValue(phc, null), equalTo(10));
  }

  @Test
  public void testGetValue_noPhc() {
    PacketHeaderConstraints phc = PacketHeaderConstraints.unconstrained();
    assertThat(IcmpTypeExtractorDefault.REQUEST.getValue(phc, null), equalTo(8));
  }
}
