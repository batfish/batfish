package org.batfish.question.testfilters;

import static org.batfish.question.testfilters.TestFiltersAnswerer.DEFAULT_DST_PORT;
import static org.batfish.question.testfilters.TestFiltersAnswerer.DEFAULT_IP_PROTOCOL;
import static org.batfish.question.testfilters.TestFiltersAnswerer.DEFAULT_SRC_PORT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Protocol;
import org.junit.Test;

public class TestFiltersAnswererTest {

  /** Test for defaults when nothing is specified */
  @Test
  public void testApplyDefaults() {
    TestFiltersQuestion question = new TestFiltersQuestion(null, "acl");

    Flow.Builder flowBuilder = new Flow.Builder();
    TestFiltersAnswerer.applyDefaults(flowBuilder, question);

    assertThat(flowBuilder.getIpProtocol(), equalTo(DEFAULT_IP_PROTOCOL));
    assertThat(flowBuilder.getDstPort(), equalTo(DEFAULT_DST_PORT));
    assertThat(flowBuilder.getSrcPort(), equalTo(DEFAULT_SRC_PORT));
  }

  /** Test for defaults when dst protocol is specified */
  @Test
  public void testApplyDefaultsDstProtocol() {
    TestFiltersQuestion question = new TestFiltersQuestion(null, "acl");
    question.setDstProtocol(Protocol.DNS);

    Flow.Builder flowBuilder = new Flow.Builder();
    TestFiltersAnswerer.applyDefaults(flowBuilder, question);

    // these two fields are filled in later when dst protocol is specified
    assertThat(flowBuilder.getIpProtocol(), equalTo(IpProtocol.IP));
    assertThat(flowBuilder.getDstPort(), equalTo(0));

    assertThat(flowBuilder.getSrcPort(), equalTo(DEFAULT_SRC_PORT));
  }

  /** Test for defaults when src protocol is specified */
  @Test
  public void testApplyDefaultsSrcProtocol() {
    TestFiltersQuestion question = new TestFiltersQuestion(null, "acl");
    question.setSrcProtocol(Protocol.DNS);

    Flow.Builder flowBuilder = new Flow.Builder();
    TestFiltersAnswerer.applyDefaults(flowBuilder, question);

    // these two fields are filled in later when src protocol is specified
    assertThat(flowBuilder.getIpProtocol(), equalTo(IpProtocol.IP));
    assertThat(flowBuilder.getSrcPort(), equalTo(0));

    assertThat(flowBuilder.getDstPort(), equalTo(DEFAULT_DST_PORT));
  }
}
