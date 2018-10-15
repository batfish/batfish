package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.SessionStatus;
import org.junit.Test;

public class BgpSessionStatusQuestionTest {
  @Test
  public void testDefaultParameters() {
    BgpSessionStatusQuestion question = new BgpSessionStatusQuestion();

    assertThat(question.getDataPlane(), equalTo(true));
    assertThat(question.getNodes(), equalTo(NodesSpecifier.ALL));
    assertThat(question.getRemoteNodes(), equalTo(NodesSpecifier.ALL));
    assertThat(question.getName(), equalTo("bgpSessionStatusNew"));
  }

  @Test
  public void testDoesNotMatchNullStatus() {
    BgpSessionStatusQuestion question = new BgpSessionStatusQuestion();
    assertThat(question.matchesStatus((ConfiguredSessionStatus) null), equalTo(false));
    assertThat(question.matchesStatus((SessionStatus) null), equalTo(false));
  }
}
