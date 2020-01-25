package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.junit.Test;

/** Test for {@link FwFromDestinationPort} */
public class FwFromDestinationPortTest {

  @Test
  public void testToHeaderspace() {
    FwFromDestinationPort from = new FwFromDestinationPort(new SubRange(1, 2));
    assertEquals(
        from.toHeaderspace(), HeaderSpace.builder().setDstPorts(new SubRange(1, 2)).build());
  }
}
