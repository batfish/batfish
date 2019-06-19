package org.batfish.question.routes;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.junit.Test;

/** Tests of {@link RoutesQuestion} */
public class RoutesQuestionTest {
  @Test
  public void testDefaultParams() {
    RoutesQuestion question = new RoutesQuestion();

    assertThat(question.getDataPlane(), equalTo(true));
    assertThat(question.getName(), equalTo("routes"));

    assertThat(question.getNodeSpecifier(), equalTo(AllNodesNodeSpecifier.INSTANCE));
    assertThat(question.getVrfs(), equalTo(".*"));
    assertThat(question.getRib(), equalTo(RibProtocol.MAIN));
  }
}
