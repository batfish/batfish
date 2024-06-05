package org.batfish.common.autocomplete;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.Location;
import org.junit.Test;

public final class LocationCompletionMetadataTest {

  @Test
  public void testEquals() {
    Location loc = new InterfaceLinkLocation("node", "iface");
    new EqualsTester()
        .addEqualityGroup(
            new LocationCompletionMetadata(loc, true, true),
            new LocationCompletionMetadata(new InterfaceLinkLocation("node", "iface"), true, true))
        .addEqualityGroup(new LocationCompletionMetadata(loc, false, true))
        .addEqualityGroup(new LocationCompletionMetadata(loc, true, false))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    LocationCompletionMetadata metadata =
        new LocationCompletionMetadata(new InterfaceLinkLocation("node", "iface"), true);
    LocationCompletionMetadata clone =
        BatfishObjectMapper.clone(metadata, LocationCompletionMetadata.class);
    assertEquals(metadata, clone);
  }

  @Test
  public void testJavaSerialization() {
    LocationCompletionMetadata metadata =
        new LocationCompletionMetadata(new InterfaceLinkLocation("node", "iface"), true);
    assertThat(SerializationUtils.clone(metadata), equalTo(metadata));
  }
}
