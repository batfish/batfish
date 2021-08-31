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

/** Test of {@link AccessRule}. */
public final class AccessRuleTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    {
      String input =
          "{"
              + "\"GARBAGE\":\"0\","
              + "\"type\":\"access-rule\","
              + "\"action\":\"1\","
              + "\"comments\":\"foo\","
              + "\"content\":[\"2\"],"
              + "\"content-direction\":\"any\","
              + "\"content-negate\":false,"
              + "\"destination\":[\"3\"],"
              + "\"destination-negate\":true,"
              + "\"enabled\":true,"
              + "\"install-on\":[\"4\"],"
              + "\"name\":\"bar\","
              + "\"rule-number\":5,"
              + "\"service\":[\"6\"],"
              + "\"service-negate\":false,"
              + "\"source\":[\"7\"],"
              + "\"source-negate\":false,"
              + "\"uid\":\"8\","
              + "\"vpn\":[\"9\"]"
              + "}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, AccessRule.class),
          equalTo(
              AccessRule.testBuilder()
                  .setAction(Uid.of("1"))
                  .setComments("foo")
                  .setContent(ImmutableList.of(Uid.of("2")))
                  .setContentDirection("any")
                  .setDestination(ImmutableList.of(Uid.of("3")))
                  .setDestinationNegate(true)
                  .setInstallOn(ImmutableList.of(Uid.of("4")))
                  .setName("bar")
                  .setRuleNumber(5)
                  .setService(ImmutableList.of(Uid.of("6")))
                  .setSource(ImmutableList.of(Uid.of("7")))
                  .setUid(Uid.of("8"))
                  .setVpn(ImmutableList.of(Uid.of("9")))
                  .build()));
    }
  }

  @Test
  public void testJavaSerialization() {
    AccessRule obj =
        AccessRule.testBuilder()
            .setAction(Uid.of("1"))
            .setComments("foo")
            .setContent(ImmutableList.of(Uid.of("2")))
            .setContentDirection("any")
            .setDestination(ImmutableList.of(Uid.of("3")))
            .setDestinationNegate(true)
            .setInstallOn(ImmutableList.of(Uid.of("4")))
            .setName("bar")
            .setRuleNumber(5)
            .setService(ImmutableList.of(Uid.of("6")))
            .setSource(ImmutableList.of(Uid.of("7")))
            .setUid(Uid.of("8"))
            .setVpn(ImmutableList.of(Uid.of("9")))
            .build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    AccessRule.Builder builder =
        AccessRule.testBuilder()
            .setAction(Uid.of("1"))
            .setComments("foo")
            .setContent(ImmutableList.of(Uid.of("2")))
            .setContentDirection("any")
            .setDestination(ImmutableList.of(Uid.of("3")))
            .setDestinationNegate(true)
            .setInstallOn(ImmutableList.of(Uid.of("4")))
            .setName("bar")
            .setRuleNumber(5)
            .setService(ImmutableList.of(Uid.of("6")))
            .setSource(ImmutableList.of(Uid.of("7")))
            .setUid(Uid.of("8"))
            .setVpn(ImmutableList.of(Uid.of("9")));
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAction(Uid.of("10")).build())
        .addEqualityGroup(builder.setComments("foo0").build())
        .addEqualityGroup(builder.setContent(ImmutableList.of(Uid.of("20"))).build())
        .addEqualityGroup(builder.setContentDirection("any0").build())
        .addEqualityGroup(builder.setContentNegate(true).build())
        .addEqualityGroup(builder.setDestination(ImmutableList.of(Uid.of("30"))).build())
        .addEqualityGroup(builder.setDestinationNegate(false).build())
        .addEqualityGroup(builder.setEnabled(false).build())
        .addEqualityGroup(builder.setInstallOn(ImmutableList.of(Uid.of("40"))).build())
        .addEqualityGroup(builder.setName("bar0").build())
        .addEqualityGroup(builder.setRuleNumber(50).build())
        .addEqualityGroup(builder.setService(ImmutableList.of(Uid.of("60"))).build())
        .addEqualityGroup(builder.setServiceNegate(true).build())
        .addEqualityGroup(builder.setSource(ImmutableList.of(Uid.of("70"))).build())
        .addEqualityGroup(builder.setSourceNegate(true).build())
        .addEqualityGroup(builder.setUid(Uid.of("80")).build())
        .addEqualityGroup(builder.setVpn(ImmutableList.of(Uid.of("90"))).build())
        .testEquals();
  }
}
