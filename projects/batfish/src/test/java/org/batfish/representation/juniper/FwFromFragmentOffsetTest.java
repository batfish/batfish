package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.junit.Test;

/** Class for {@link FwFromFragmentOffset} */
public class FwFromFragmentOffsetTest {

  @Test
  public void testToHeaderspace_notExcept() {
    FwFromFragmentOffset from = new FwFromFragmentOffset(new SubRange(1, 2), false);
    assertEquals(
        from.toHeaderspace(),
        HeaderSpace.builder().setFragmentOffsets(ImmutableSet.of(new SubRange(1, 2))).build());
  }

  @Test
  public void testToHeaderspace_except() {
    FwFromFragmentOffset from = new FwFromFragmentOffset(new SubRange(1, 2), true);
    assertEquals(
        from.toHeaderspace(),
        HeaderSpace.builder().setNotFragmentOffsets(ImmutableSet.of(new SubRange(1, 2))).build());
  }
}
