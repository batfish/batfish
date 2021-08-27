package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link Package}. */
public final class PackageTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    {
      String input =
          "{"
              + "\"GARBAGE\":0,"
              + "\"type\":\"package\","
              + "\"uid\":\"0\","
              + "\"name\":\"foo\","
              + "\"domain\":{"
              + "\"domain-type\":\"domain\","
              + "\"uid\":\"1\","
              + "\"name\":\"bar\""
              + "}," // domain
              + "\"installation-targets\":\"all\","
              + "\"access\":true,"
              + "\"nat-policy\":true"
              + "}"; // Package

      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Package.class),
          equalTo(
              new Package(
                  new Domain("bar", Uid.of("1")),
                  AllInstallationTargets.instance(),
                  "foo",
                  true,
                  true,
                  Uid.of("0"))));
    }
    {
      String input =
          "{"
              + "\"GARBAGE\":0,"
              + "\"type\":\"package\","
              + "\"uid\":\"1\","
              + "\"name\":\"foo\","
              + "\"domain\":{"
              + "\"domain-type\":\"domain\","
              + "\"uid\":\"2\","
              + "\"name\":\"bar\""
              + "}," // domain
              + "\"installation-targets\":["
              + "{" // object: simple-gateway
              + "\"type\":\"simple-gateway\","
              + "\"uid\":\"0\","
              + "\"name\":\"foo\""
              + "}" // object: simple-gateway
              + "]," // installation-targets
              + "\"access\":true,"
              + "\"nat-policy\":true"
              + "}"; // Package

      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Package.class),
          equalTo(
              new Package(
                  new Domain("bar", Uid.of("2")),
                  new ListInstallationTargets(
                      ImmutableList.of(new PackageInstallationTarget("foo", Uid.of("0")))),
                  "foo",
                  true,
                  true,
                  Uid.of("1"))));
    }
  }

  @Test
  public void testJavaSerialization() {
    Package obj =
        new Package(
            new Domain("bar", Uid.of("1")),
            AllInstallationTargets.instance(),
            "foo",
            true,
            true,
            Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    Package obj =
        new Package(
            new Domain("bar", Uid.of("1")),
            AllInstallationTargets.instance(),
            "foo",
            true,
            true,
            Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(
            obj,
            new Package(
                new Domain("bar", Uid.of("1")),
                AllInstallationTargets.instance(),
                "foo",
                true,
                true,
                Uid.of("0")))
        .addEqualityGroup(
            new Package(
                new Domain("bar", Uid.of("2")),
                AllInstallationTargets.instance(),
                "foo",
                true,
                true,
                Uid.of("0")))
        .addEqualityGroup(
            new Package(
                new Domain("bar", Uid.of("1")),
                new ListInstallationTargets(
                    ImmutableList.of(new PackageInstallationTarget("foo", Uid.of("0")))),
                "foo",
                true,
                true,
                Uid.of("0")))
        .addEqualityGroup(
            new Package(
                new Domain("bar", Uid.of("1")),
                AllInstallationTargets.instance(),
                "bar",
                true,
                true,
                Uid.of("0")))
        .addEqualityGroup(
            new Package(
                new Domain("bar", Uid.of("1")),
                AllInstallationTargets.instance(),
                "foo",
                false,
                true,
                Uid.of("0")))
        .addEqualityGroup(
            new Package(
                new Domain("bar", Uid.of("1")),
                AllInstallationTargets.instance(),
                "foo",
                true,
                false,
                Uid.of("0")))
        .addEqualityGroup(
            new Package(
                new Domain("bar", Uid.of("1")),
                AllInstallationTargets.instance(),
                "foo",
                true,
                true,
                Uid.of("1")))
        .testEquals();
  }
}
