package org.batfish.storage;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class StoredObjectMetadataTest {

  @Test
  public void testEquals() {
    String key = "some key";
    long size = 15;
    StoredObjectMetadata metadata1 = new StoredObjectMetadata(key, size);
    StoredObjectMetadata metadata2 = new StoredObjectMetadata("some other key", size);
    StoredObjectMetadata metadata3 = new StoredObjectMetadata(key, 16);

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            metadata1, metadata1, new StoredObjectMetadata(metadata1.getKey(), metadata1.getSize()))
        .addEqualityGroup(metadata2)
        .addEqualityGroup(metadata3)
        .addEqualityGroup(metadata1.toString())
        .addEqualityGroup(metadata2.toString())
        .addEqualityGroup(metadata3.toString())
        .testEquals();
  }
}
