package org.batfish.vendor;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test for {@link VendorStructure}. */
public final class VendorStructureTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new VendorStructure("f1", "t1", "n1"), new VendorStructure("f1", "t1", "n1"))
        .addEqualityGroup(new VendorStructure("f2", "t1", "n1"))
        .addEqualityGroup(new VendorStructure("f1", "t2", "n1"))
        .addEqualityGroup(new VendorStructure("f1", "t1", "n2"))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    VendorStructure vendorStructure = new VendorStructure("f", "t", "n");
    assertEquals(
        vendorStructure, BatfishObjectMapper.clone(vendorStructure, VendorStructure.class));
  }
}
