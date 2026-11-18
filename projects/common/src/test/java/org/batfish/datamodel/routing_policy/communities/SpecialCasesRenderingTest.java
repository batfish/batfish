package org.batfish.datamodel.routing_policy.communities;

import static org.batfish.datamodel.bgp.community.StandardCommunity.NO_EXPORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link SpecialCasesRendering}. */
final class SpecialCasesRenderingTest {

  @Test
  void testJavaSerialization() {
    CommunityRendering obj =
        SpecialCasesRendering.of(
            ColonSeparatedRendering.instance(), ImmutableMap.of(NO_EXPORT, "foo"));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testJacksonSerialization() {
    CommunityRendering obj =
        SpecialCasesRendering.of(
            ColonSeparatedRendering.instance(), ImmutableMap.of(NO_EXPORT, "foo"));
    assertThat(BatfishObjectMapper.clone(obj, CommunityRendering.class), equalTo(obj));
  }

  @Test
  void testEquals() {
    CommunityRendering obj =
        SpecialCasesRendering.of(ColonSeparatedRendering.instance(), ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(
            obj, SpecialCasesRendering.of(ColonSeparatedRendering.instance(), ImmutableMap.of()))
        .addEqualityGroup(
            SpecialCasesRendering.of(IntegerValueRendering.instance(), ImmutableMap.of()))
        .addEqualityGroup(
            SpecialCasesRendering.of(
                ColonSeparatedRendering.instance(), ImmutableMap.of(NO_EXPORT, "foo")))
        .testEquals();
  }
}
