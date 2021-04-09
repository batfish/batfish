package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class IpSpaceMetadataTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new IpSpaceMetadata("a", "b", null), new IpSpaceMetadata("a", "b", null))
        .addEqualityGroup(new IpSpaceMetadata("b", "b", null))
        .addEqualityGroup(new IpSpaceMetadata("b", "c", null))
        .addEqualityGroup(new IpSpaceMetadata("b", "c", new VendorStructureId("f", "t", "n")))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    IpSpaceMetadata orig = new IpSpaceMetadata("b", "c", new VendorStructureId("f", "t", "n"));
    assertEquals(orig, BatfishObjectMapper.clone(orig, IpSpaceMetadata.class));
    assertEquals(orig, SerializationUtils.clone(orig));
  }
}
