package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link Packages}. */
public final class PackagesTest {
  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"packages\":["
            + "{"
            + "\"type\":\"package\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"domain\":{"
            + "\"domain-type\":\"domain\","
            + "\"uid\":\"1\","
            + "\"name\":\"bar\""
            + "}," // domain
            + "\"installation-targets\":\"all\","
            + "\"nat-policy\":true"
            + "}" // package
            + "]" // packages
            + "}"; // Packages
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Packages.class),
        equalTo(
            new Packages(
                ImmutableMap.of(
                    Uid.of("0"),
                    new Package(
                        new Domain("bar", Uid.of("1")),
                        AllInstallationTargets.instance(),
                        "foo",
                        true,
                        Uid.of("0"))))));
  }

  @Test
  public void testJavaSerialization() {
    Packages obj =
        new Packages(
            ImmutableMap.of(
                Uid.of("0"),
                new Package(
                    new Domain("bar", Uid.of("1")),
                    AllInstallationTargets.instance(),
                    "foo",
                    true,
                    Uid.of("0"))));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    Packages obj = new Packages(ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(obj, new Packages(ImmutableMap.of()))
        .addEqualityGroup(
            new Packages(
                ImmutableMap.of(
                    Uid.of("0"),
                    new Package(
                        new Domain("bar", Uid.of("1")),
                        AllInstallationTargets.instance(),
                        "foo",
                        true,
                        Uid.of("0")))))
        .testEquals();
  }
}
