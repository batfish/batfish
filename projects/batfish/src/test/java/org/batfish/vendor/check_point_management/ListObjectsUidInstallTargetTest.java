package org.batfish.vendor.check_point_management;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link ListObjectsUidInstallTarget}. */
public final class ListObjectsUidInstallTargetTest {

  @Test
  public void testJavaSerialization() {
    ListObjectsUidInstallTarget obj = new ListObjectsUidInstallTarget(ImmutableList.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ListObjectsUidInstallTarget obj = new ListObjectsUidInstallTarget(ImmutableList.of());
    new EqualsTester()
        .addEqualityGroup(obj, new ListObjectsUidInstallTarget(ImmutableList.of()))
        .addEqualityGroup(new ListObjectsUidInstallTarget(ImmutableList.of(Uid.of("1"))))
        .testEquals();
  }
}
