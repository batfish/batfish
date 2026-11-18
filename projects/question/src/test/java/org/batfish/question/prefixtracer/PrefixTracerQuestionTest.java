package org.batfish.question.prefixtracer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

/** Tests for {@link PrefixTracerQuestion} */
class PrefixTracerQuestionTest {
  @Test
  void testConstructorHasDefaultNodeSpecifier() {
    PrefixTracerQuestion ptq = new PrefixTracerQuestion();
    assertThat(ptq.getNodes(), nullValue());
  }

  @Test
  void testConstructorDefaultHasNullPrefix() {
    PrefixTracerQuestion ptq = new PrefixTracerQuestion();
    assertThat(ptq.getPrefix(), nullValue());
  }

  @Test
  void testRequiredDataplane() {
    PrefixTracerQuestion ptq = new PrefixTracerQuestion();
    assertThat(ptq.getDataPlane(), equalTo(true));
  }
}
