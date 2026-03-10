package org.batfish.common.util.isp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

public class NoTrafficFilteringPolicyTest {
  @Test
  public void testSetsNoFilters() {
    NoTrafficFilteringPolicy policy = NoTrafficFilteringPolicy.create();
    assertThat(policy.filterTrafficFromInternet(), nullValue());
    assertThat(policy.filterTrafficToInternet(), nullValue());
    assertThat(policy.filterTrafficFromNetwork(), nullValue());
    assertThat(policy.filterTrafficToNetwork(), nullValue());
  }
}
