package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.questions.NodesSpecifier;
import org.junit.Test;

public class BgpSessionCompatibilityQuestionTest {
  @Test
  public void testDefaultParameters() {
    BgpSessionCompatibilityQuestion question = new BgpSessionCompatibilityQuestion();

    assertThat(question.getDataPlane(), equalTo(false));
    assertThat(question.getNodes(), equalTo(NodesSpecifier.ALL));
    assertThat(question.getRemoteNodes(), equalTo(NodesSpecifier.ALL));
    assertThat(question.getName(), equalTo("bgpSessionCompatibility"));
  }
}
