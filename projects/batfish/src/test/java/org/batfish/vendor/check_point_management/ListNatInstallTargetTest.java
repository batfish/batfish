package org.batfish.vendor.check_point_management;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link ListNatInstallTarget}. */
public final class ListNatInstallTargetTest {

  @Test
  public void testJavaSerialization() {
    ListNatInstallTarget obj = new ListNatInstallTarget(ImmutableList.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ListNatInstallTarget obj = new ListNatInstallTarget(ImmutableList.of());
    new EqualsTester()
        .addEqualityGroup(obj, new ListNatInstallTarget(ImmutableList.of()))
        .addEqualityGroup(new ListNatInstallTarget(ImmutableList.of(Uid.of("1"))))
        .testEquals();
  }
}
