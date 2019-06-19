package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.specifier.AllNodesNodeSpecifier;
import org.junit.Test;

public class BgpSessionCompatibilityQuestionTest {
  @Test
  public void testDefaultParameters() {
    BgpSessionCompatibilityQuestion question = new BgpSessionCompatibilityQuestion();

    assertThat(question.getDataPlane(), equalTo(false));
    assertThat(question.getNodeSpecifier(), equalTo(AllNodesNodeSpecifier.INSTANCE));
    assertThat(question.getRemoteNodeSpecifier(), equalTo(AllNodesNodeSpecifier.INSTANCE));
    assertThat(question.getName(), equalTo("bgpSessionCompatibility"));
  }
}
