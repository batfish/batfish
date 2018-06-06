package org.batfish.question.prefixtracer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.questions.NodesSpecifier;
import org.junit.Test;

public class PrefixTracerQuestionTest {
  @Test
  public void testConstructorHasDefaultNodeSpecifier() {
    PrefixTracerQuestion ptq = new PrefixTracerQuestion();
    assertThat(ptq.getNodeRegex(), equalTo(NodesSpecifier.ALL));
  }
}
