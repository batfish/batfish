package org.batfish.vendor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test for {@link VendorStructureId}. */
final class VendorStructureIdTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new VendorStructureId("f1", "t1", "n1"), new VendorStructureId("f1", "t1", "n1"))
        .addEqualityGroup(new VendorStructureId("f2", "t1", "n1"))
        .addEqualityGroup(new VendorStructureId("f1", "t2", "n1"))
        .addEqualityGroup(new VendorStructureId("f1", "t1", "n2"))
        .testEquals();
  }

  @Test
  void testJsonSerialization() {
    VendorStructureId vendorStructureId = new VendorStructureId("f", "t", "n");
    assertEquals(
        vendorStructureId, BatfishObjectMapper.clone(vendorStructureId, VendorStructureId.class));
  }
}
