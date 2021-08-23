package org.batfish.vendor.check_point_management;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link ListInstallationTargets}. */
public final class ListInstallationTargetsTest {

  @Test
  public void testJavaSerialization() {
    ListInstallationTargets obj =
        new ListInstallationTargets(
            ImmutableList.of(new PackageInstallationTarget("foo", Uid.of("0"))));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ListInstallationTargets obj = new ListInstallationTargets(ImmutableList.of());
    new EqualsTester()
        .addEqualityGroup(obj, new ListInstallationTargets(ImmutableList.of()))
        .addEqualityGroup(
            new ListInstallationTargets(
                ImmutableList.of(new PackageInstallationTarget("foo", Uid.of("0")))))
        .testEquals();
  }
}
