package org.batfish.symbolic.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.symbolic.smt.VerificationResult;

public class SmtDeterminismAnswerElement implements AnswerElement {

  private static final String PROP_FLOW = "flow";

  private static final String PROP_FORWARDING_CASE1 = "forwardingCase1";

  private static final String PROP_FORWARDING_CASE2 = "forwardingCase2";

  private Flow _flow;

  private SortedSet<String> _forwardingCase1;

  private SortedSet<String> _forwardingCase2;

  private VerificationResult _result;

  @JsonCreator
  private SmtDeterminismAnswerElement() {}

  public SmtDeterminismAnswerElement(
      VerificationResult result,
      @Nullable Flow flow,
      @Nullable SortedSet<String> case1,
      @Nullable SortedSet<String> case2) {
    _result = result;
    _flow = flow;
    _forwardingCase1 = case1;
    _forwardingCase2 = case2;
  }

  @JsonProperty(PROP_FLOW)
  public Flow getFlow() {
    return _flow;
  }

  @JsonProperty(PROP_FORWARDING_CASE1)
  public SortedSet<String> getForwardingCase1() {
    return _forwardingCase1;
  }

  @JsonProperty(PROP_FORWARDING_CASE2)
  public SortedSet<String> getForwardingCase2() {
    return _forwardingCase2;
  }

  @JsonIgnore
  public VerificationResult getResult() {
    return _result;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    if (_result.isVerified()) {
      sb.append("Verified\n");
    } else {
      sb.append(_flow).append("\n\n");
      sb.append("Delta forwarding case 1:\n");
      for (String s : _forwardingCase1) {
        sb.append("   ").append(s).append("\n");
      }
      sb.append("\nDelta forwarding case 2:\n");
      for (String s : _forwardingCase2) {
        sb.append("   ").append(s).append("\n");
      }
    }
    return sb.toString();
  }

  @JsonProperty(PROP_FLOW)
  public void setFlow(Flow flow) {
    _flow = flow;
  }

  @JsonProperty(PROP_FORWARDING_CASE1)
  public void setForwardingCase1(SortedSet<String> forwardingCase1) {
    _forwardingCase1 = forwardingCase1;
  }

  @JsonProperty(PROP_FORWARDING_CASE2)
  public void setForwardingCase2(SortedSet<String> forwardingCase2) {
    _forwardingCase2 = forwardingCase2;
  }
}
