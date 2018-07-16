package org.batfish.question.ipsecpeers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/** Return status of all IPSec peers in the network */
public class IpsecPeersQuestion extends Question {

  private static final String PROP_INITIATOR_REGEX = "initiatorRegex";

  private static final String PROP_RESPONDER_REGEX = "responderRegex";

  private static final String QUESTION_NAME = "ipsecpeers";

  private NodesSpecifier _initiatorRegex;

  private NodesSpecifier _responderRegex;

  @JsonCreator
  public IpsecPeersQuestion(
      @JsonProperty(PROP_INITIATOR_REGEX) NodesSpecifier initiatorRegex,
      @JsonProperty(PROP_RESPONDER_REGEX) NodesSpecifier responderRegex) {
    _initiatorRegex = initiatorRegex == null ? NodesSpecifier.ALL : initiatorRegex;
    _responderRegex = responderRegex == null ? NodesSpecifier.ALL : responderRegex;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return QUESTION_NAME;
  }

  @JsonProperty(PROP_INITIATOR_REGEX)
  public NodesSpecifier getInitiatorRegex() {
    return _initiatorRegex;
  }

  @JsonProperty(PROP_RESPONDER_REGEX)
  public NodesSpecifier getResponderRegex() {
    return _responderRegex;
  }
}
