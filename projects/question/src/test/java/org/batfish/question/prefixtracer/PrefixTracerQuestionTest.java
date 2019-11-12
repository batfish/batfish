package org.batfish.question.prefixtracer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests for {@link PrefixTracerQuestion} */
public class PrefixTracerQuestionTest {
  @Test
  public void testConstructorHasDefaultNodeSpecifier() {
    PrefixTracerQuestion ptq = new PrefixTracerQuestion();
    assertThat(ptq.getNodes(), nullValue());
  }

  @Test
  public void testConstructorDefaultHasNullPrefix() {
    PrefixTracerQuestion ptq = new PrefixTracerQuestion();
    assertThat(ptq.getPrefix(), nullValue());
  }

  @Test
  public void testRequiredDataplane() {
    PrefixTracerQuestion ptq = new PrefixTracerQuestion();
    assertThat(ptq.getDataPlane(), equalTo(true));
  }
}
