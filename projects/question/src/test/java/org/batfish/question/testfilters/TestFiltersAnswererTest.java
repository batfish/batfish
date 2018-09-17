package org.batfish.question.testfilters;

import static org.batfish.question.testfilters.TestFiltersAnswerer.DEFAULT_DST_PORT;
import static org.batfish.question.testfilters.TestFiltersAnswerer.DEFAULT_IP_PROTOCOL;
import static org.batfish.question.testfilters.TestFiltersAnswerer.DEFAULT_SRC_PORT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.batfish.datamodel.Flow;
import org.junit.Test;

public class TestFiltersAnswererTest {

  /**
   * Check that if a simple string is the specifier input, that maps to the expected
   * FilterSpecifier. (That this happens is being assumed by ReachFilterAnswerer, which it ideally
   * shouldn't but in the meanwhile this test helps.)
   */
  @Test
  public void testApplyDefaults() {
    TestFiltersQuestion question = new TestFiltersQuestion(null, "acl");

    Flow.Builder flowBuilder = new Flow.Builder();
    TestFiltersAnswerer.applyDefaults(flowBuilder, question);

    assertThat(flowBuilder.getIpProtocol(), equalTo(DEFAULT_IP_PROTOCOL));
    assertThat(flowBuilder.getDstPort(), equalTo(DEFAULT_DST_PORT));
    assertThat(flowBuilder.getSrcPort(), equalTo(DEFAULT_SRC_PORT));
  }
}
