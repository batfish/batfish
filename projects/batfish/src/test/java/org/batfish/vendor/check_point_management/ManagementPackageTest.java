package org.batfish.vendor.check_point_management;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link ManagementPackage}. */
final class ManagementPackageTest {

  @Test
  void testJavaSerialization() {
    ManagementPackage obj =
        new ManagementPackage(
            ImmutableList.of(),
            null,
            new Package(
                new Domain("a", Uid.of("1")),
                AllInstallationTargets.instance(),
                "b",
                false,
                false,
                Uid.of("1")));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testEquals() {
    ManagementPackage obj =
        new ManagementPackage(
            ImmutableList.of(),
            null,
            new Package(
                new Domain("a", Uid.of("1")),
                AllInstallationTargets.instance(),
                "b",
                false,
                false,
                Uid.of("1")));
    new EqualsTester()
        .addEqualityGroup(
            obj,
            new ManagementPackage(
                ImmutableList.of(),
                null,
                new Package(
                    new Domain("a", Uid.of("1")),
                    AllInstallationTargets.instance(),
                    "b",
                    false,
                    false,
                    Uid.of("1"))))
        .addEqualityGroup(
            new ManagementPackage(
                ImmutableList.of(
                    new AccessLayer(ImmutableMap.of(), ImmutableList.of(), Uid.of("1"), "foo")),
                null,
                new Package(
                    new Domain("a", Uid.of("1")),
                    AllInstallationTargets.instance(),
                    "b",
                    false,
                    false,
                    Uid.of("1"))))
        .addEqualityGroup(
            new ManagementPackage(
                ImmutableList.of(),
                new NatRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("1")),
                new Package(
                    new Domain("a", Uid.of("1")),
                    AllInstallationTargets.instance(),
                    "b",
                    false,
                    false,
                    Uid.of("1"))))
        .addEqualityGroup(
            new ManagementPackage(
                ImmutableList.of(),
                null,
                new Package(
                    new Domain("a", Uid.of("1")),
                    AllInstallationTargets.instance(),
                    "c",
                    false,
                    false,
                    Uid.of("1"))))
        .testEquals();
  }
}
