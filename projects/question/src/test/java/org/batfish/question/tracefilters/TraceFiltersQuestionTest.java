package org.batfish.question.tracefilters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.specifier.ShorthandFilterSpecifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TraceFiltersQuestionTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  /**
   * Check that if a simple string is the specifier input, that maps to the expected
   * FilterSpecifier. (That this happens is being assumed by ReachFilterAnswerer, which it ideally
   * shouldn't but in the meanwhile this test helps.)
   */
  @Test
  public void testDefaultSpecifierInput() {
    TraceFiltersQuestion question = new TraceFiltersQuestion(null, "acl");

    assertThat(
        question.getFilterSpecifier(),
        equalTo(new ShorthandFilterSpecifier(new FiltersSpecifier("acl"))));
  }

  @Test
  public void testDeserializationDefaultValues() throws IOException {
    String serialized =
        String.format("{\"class\":\"%s\"}", TraceFiltersQuestion.class.getCanonicalName());
    TraceFiltersQuestion q =
        BatfishObjectMapper.mapper().readValue(serialized, TraceFiltersQuestion.class);

    assertThat(q.getFilterSpecifier(), notNullValue());
    assertThat(q.getNodeRegex(), notNullValue());
  }
}
