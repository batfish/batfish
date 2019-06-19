package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.specifier.AllNodesNodeSpecifier;
import org.junit.Test;

public class BgpSessionQuestionTest {
  @Test
  public void testDefaultParameters() {
    BgpSessionStatusQuestion question = new BgpSessionStatusQuestion();

    assertThat(question.getDataPlane(), equalTo(true));
    assertThat(question.getNodeSpecifier(), equalTo(AllNodesNodeSpecifier.INSTANCE));
    assertThat(question.getRemoteNodeSpecifier(), equalTo(AllNodesNodeSpecifier.INSTANCE));
    assertThat(question.getName(), equalTo("bgpSessionStatus"));
  }

  @Test
  public void testDefaultMatchesNullStatus() {
    BgpSessionStatusQuestion question = new BgpSessionStatusQuestion();
    assertThat(question.matchesStatus(null), equalTo(true));
  }

  @Test
  public void testStatusFilterDoesNotMatchNullStatus() {
    BgpSessionStatusQuestion question =
        new BgpSessionStatusQuestion(null, null, "UNIQUE_MATCH", null);
    assertThat(question.matchesStatus(null), equalTo(false));
  }
}
