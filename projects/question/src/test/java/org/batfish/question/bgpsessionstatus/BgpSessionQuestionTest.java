package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer.SessionStatus;
import org.junit.Test;

public class BgpSessionQuestionTest {
  @Test
  public void testDefaultParameters() {
    BgpSessionStatusQuestion question = new BgpSessionStatusQuestion();

    assertThat(question.getDataPlane(), equalTo(true));
    assertThat(question.getNodes(), equalTo(NodesSpecifier.ALL));
    assertThat(question.getRemoteNodes(), equalTo(NodesSpecifier.ALL));
    assertThat(question.getName(), equalTo("bgpSessionStatusNew"));
  }

  @Test
  public void testDefaultMatchesNullStatus() {
    BgpSessionStatusQuestion question = new BgpSessionStatusQuestion();
    assertThat(question.matchesStatus((ConfiguredSessionStatus) null), equalTo(true));
    assertThat(question.matchesStatus((SessionStatus) null), equalTo(true));
  }

  @Test
  public void testStatusFilterDoesNotMatchNullStatus() {
    BgpSessionStatusQuestion question =
        new BgpSessionStatusQuestion(null, null, "UNIQUE_MATCH", null);
    assertThat(question.matchesStatus((ConfiguredSessionStatus) null), equalTo(false));
    assertThat(question.matchesStatus((SessionStatus) null), equalTo(false));
  }
}
