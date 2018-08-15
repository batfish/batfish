package org.batfish.question.routes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.junit.Test;

/** Tests of {@link RoutesQuestion} */
public class RoutesQuestionTest {
  @Test
  public void testDefaultParams() {
    RoutesQuestion question = new RoutesQuestion();

    assertThat(question.getDataPlane(), equalTo(true));
    assertThat(question.getName(), equalTo("routes2"));

    assertThat(question.getNodeRegex(), equalTo(NodesSpecifier.ALL));
    assertThat(question.getVrfRegex(), equalTo(".*"));
    assertThat(question.getProtocol(), equalTo(RibProtocol.MAIN));
  }
}
