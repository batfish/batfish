package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunitySet}. */
public final class CommunitySetTest {

  private static final CommunitySet SET = CommunitySet.of();

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(SET, CommunitySet.class), equalTo(SET));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(SET), equalTo(SET));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(SET, SET, CommunitySet.empty())
        .addEqualityGroup(CommunitySet.of(StandardCommunity.of(1L)))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testHasExtendedCommunities() {
    assertFalse(CommunitySet.empty().hasExtendedCommunities());
    assertFalse(CommunitySet.of(StandardCommunity.of(1L)).hasExtendedCommunities());
    assertTrue(CommunitySet.of(ExtendedCommunity.parse("1:1:1")).hasExtendedCommunities());
    assertTrue(
        CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.parse("1:1:1"))
            .hasExtendedCommunities());
    // Consistent after the extended-community subset has been materialized and cached.
    CommunitySet withExtended =
        CommunitySet.of(StandardCommunity.of(1L), ExtendedCommunity.parse("1:1:1"));
    assertThat(withExtended.getExtendedCommunities().size(), equalTo(1));
    assertTrue(withExtended.hasExtendedCommunities());
    CommunitySet withoutExtended = CommunitySet.of(StandardCommunity.of(1L));
    assertThat(withoutExtended.getExtendedCommunities().size(), equalTo(0));
    assertFalse(withoutExtended.hasExtendedCommunities());
  }

  @Test
  public void testToString() {
    assertNotNull(CommunitySet.empty().toString());
    assertThat(
        CommunitySet.of(StandardCommunity.of(1L)).toString(),
        allOf(notNullValue(), not(equalTo(CommunitySet.empty().toString()))));
  }
}
