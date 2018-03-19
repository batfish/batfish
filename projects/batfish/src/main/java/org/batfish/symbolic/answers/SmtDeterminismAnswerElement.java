package org.batfish.symbolic.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.AnswerElement;

public class SmtDeterminismAnswerElement extends AnswerElement {

  private static final String PROP_FLOW = "flow";

  private static final String PROP_FORWARDING_CASE1 = "forwardingCase1";

  private static final String PROP_FORWARDING_CASE2 = "forwardingCase2";

  private Flow _flow;

  private SortedSet<String> _forwardingCase1;

  private SortedSet<String> _forwardingCase2;

  @JsonCreator
  public SmtDeterminismAnswerElement(
      @JsonProperty(PROP_FLOW) @Nullable Flow flow,
      @JsonProperty(PROP_FORWARDING_CASE1) @Nullable SortedSet<String> case1,
      @JsonProperty(PROP_FORWARDING_CASE2) @Nullable SortedSet<String> case2) {
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

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    if (_forwardingCase1 == null) {
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
}
