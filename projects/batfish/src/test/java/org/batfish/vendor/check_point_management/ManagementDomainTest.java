package org.batfish.vendor.check_point_management;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link ManagementDomain}. */
public final class ManagementDomainTest {

  @Test
  public void testJavaSerialization() {
    ManagementDomain obj =
        new ManagementDomain(new Domain("a", Uid.of("1")), ImmutableMap.of(), ImmutableMap.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ManagementDomain obj =
        new ManagementDomain(new Domain("a", Uid.of("1")), ImmutableMap.of(), ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(
            obj,
            new ManagementDomain(
                new Domain("a", Uid.of("1")), ImmutableMap.of(), ImmutableMap.of()))
        .addEqualityGroup(
            new ManagementDomain(
                new Domain("b", Uid.of("1")), ImmutableMap.of(), ImmutableMap.of()))
        .addEqualityGroup(
            new ManagementDomain(
                new Domain("a", Uid.of("1")),
                ImmutableMap.of(
                    Uid.of("1"),
                    new SimpleGateway(
                        Ip.ZERO,
                        "b",
                        ImmutableList.of(),
                        GatewayOrServerPolicy.empty(),
                        Uid.of("2"))),
                ImmutableMap.of()))
        .addEqualityGroup(
            new ManagementDomain(
                new Domain("a", Uid.of("1")),
                ImmutableMap.of(),
                ImmutableMap.of(
                    Uid.of("1"),
                    new ManagementPackage(
                        ImmutableList.of(),
                        null,
                        new Package(
                            new Domain("a", Uid.of("1")),
                            AllInstallationTargets.instance(),
                            "b",
                            false,
                            false,
                            Uid.of("1"))))))
        .testEquals();
  }
}
