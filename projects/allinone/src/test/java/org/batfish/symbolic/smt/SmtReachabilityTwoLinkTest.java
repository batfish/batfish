package org.batfish.symbolic.smt;

import static org.batfish.symbolic.smt.matchers.SmtReachabilityAnswerElementMatchers.hasVerificationResult;
import static org.batfish.symbolic.smt.matchers.VerificationResultMatchers.hasFailures;
import static org.batfish.symbolic.smt.matchers.VerificationResultMatchers.hasIsVerified;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;

import java.io.IOException;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.question.SmtReachabilityQuestionPlugin.ReachabilityQuestion;
import org.batfish.symbolic.answers.SmtReachabilityAnswerElement;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SmtReachabilityTwoLinkTest {
  private TwoNodeNetworkWithTwoLinks _network;

  @Before
  public void setup() throws IOException {
    _network = new TwoNodeNetworkWithTwoLinks();
  }

  /**
   * Test that with one failure, both links between the two nodes are down, so no _dstIp is
   * reachable from the source.
   */
  @Test
  public void testOneFailure() {
    final ReachabilityQuestion question = new ReachabilityQuestion();
    question.setIngressNodeRegex(_network._srcNode.getName());
    question.setFinalNodeRegex(_network._dstNode.getName());
    question.setFailures(1);

    final AnswerElement answer = _network._batfish.smtReachability(question);
    assertThat(answer, instanceOf(SmtReachabilityAnswerElement.class));

    final SmtReachabilityAnswerElement smtAnswer = (SmtReachabilityAnswerElement) answer;
    assertThat(
        smtAnswer,
        hasVerificationResult(
            allOf(
                hasIsVerified(false),
                hasFailures(
                    contains(
                        String.format(
                            "link(%s,%s)",
                            _network._srcNode.getName(), _network._dstNode.getName()))))));
  }

  /**
   * Negation of the above test, i.e. verify unreachability under 1 failure.
   *
   * <p>{@link Ignore Ignored} because the test is failing. It's unclear whether the intent is that
   * failures constraint should be included in the negation. But that isn't working either
   * (otherwise, failures=0 should return a {@link VerificationResult} with failures != 0. It does
   * not).
   */
  @Ignore
  @Test
  public void testOneFailure_negate() {
    final ReachabilityQuestion question = new ReachabilityQuestion();
    question.setIngressNodeRegex(_network._srcNode.getName());
    question.setFinalNodeRegex(_network._dstNode.getName());
    question.setFailures(1);
    question.setNegate(true);

    final AnswerElement answer = _network._batfish.smtReachability(question);
    assertThat(answer, instanceOf(SmtReachabilityAnswerElement.class));

    final SmtReachabilityAnswerElement smtAnswer = (SmtReachabilityAnswerElement) answer;
    assertThat(
        smtAnswer,
        hasVerificationResult(
            allOf(
                hasIsVerified(true),
                hasFailures(
                    contains(
                        String.format(
                            "link(%s,%s)",
                            _network._srcNode.getName(), _network._dstNode.getName()))))));
  }

  @Test
  public void testOneFailure_notFailNode1() {
    final ReachabilityQuestion question = new ReachabilityQuestion();
    question.setIngressNodeRegex(_network._srcNode.getName());
    question.setFinalNodeRegex(_network._dstNode.getName());
    question.setFailures(1);

    // Dont allow edges connected to _srcNode to fail
    question.setNotFailNode1Regex(_network._srcNode.getName());
    question.setNotFailNode2Regex(".*");

    final AnswerElement answer = _network._batfish.smtReachability(question);
    assertThat(answer, instanceOf(SmtReachabilityAnswerElement.class));

    final SmtReachabilityAnswerElement smtAnswer = (SmtReachabilityAnswerElement) answer;
    assertThat(smtAnswer, hasVerificationResult(hasIsVerified(true)));
  }

  /** Test that the notFailNode*Regex parameters are not directional. */
  @Test
  public void testOneFailure_notFailNode2() {
    final ReachabilityQuestion question = new ReachabilityQuestion();
    question.setIngressNodeRegex(_network._srcNode.getName());
    question.setFinalNodeRegex(_network._dstNode.getName());
    question.setFailures(1);

    // Dont allow edges connected to _srcNode to fail
    question.setNotFailNode1Regex(".*");
    question.setNotFailNode2Regex(_network._srcNode.getName());

    final AnswerElement answer = _network._batfish.smtReachability(question);
    assertThat(answer, instanceOf(SmtReachabilityAnswerElement.class));

    final SmtReachabilityAnswerElement smtAnswer = (SmtReachabilityAnswerElement) answer;
    assertThat(smtAnswer, hasVerificationResult(hasIsVerified(true)));
  }
}
