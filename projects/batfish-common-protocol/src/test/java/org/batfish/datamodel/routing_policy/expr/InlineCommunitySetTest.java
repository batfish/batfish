package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.batfish.representation.cisco.InlineCommunitySet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link InlineCommunitySet}. */
@RunWith(JUnit4.class)
public class InlineCommunitySetTest {
  @Test
  public void testCommunities() {
    InlineCommunitySet set = new InlineCommunitySet(ImmutableList.of(1L, 2L, 3L));

    assertThat(set.asLiteralCommunities(null), equalTo(ImmutableSet.of(2L, 3L, 1L)));
  }

  @Test
  public void testMatchSingleCommunity() {
    InlineCommunitySet set = new InlineCommunitySet(ImmutableList.of(1L, 2L, 3L));

    assertTrue(set.matchAnyCommunity(null, Sets.newHashSet(2L)));
    assertTrue(set.matchAnyCommunity(null, Sets.newHashSet(1L, 2L, 3L)));
    assertTrue(set.matchAnyCommunity(null, Sets.newHashSet(4L, 1L)));

    assertFalse(set.matchAnyCommunity(null, Sets.newHashSet(4L)));
  }
}
