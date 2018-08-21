package org.batfish.question.ipsecsessionstatus;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ipsecsessionstatus.IpsecSessionInfo.IpsecSessionStatus;

/** Return status of all IPSec sessions in the network */
public class IpsecSessionStatusQuestion extends Question {

  private static final String PROP_INITIATOR_REGEX = "initiatorRegex";

  private static final String PROP_RESPONDER_REGEX = "responderRegex";

  private static final String PROP_STATUS_REGEX = "statusRegex";

  private static final String QUESTION_NAME = "ipsecsessionstatus";

  @Nonnull private NodesSpecifier _initiatorRegex;

  @Nonnull private NodesSpecifier _responderRegex;

  @Nonnull private Pattern _statusRegex;

  @JsonCreator
  public IpsecSessionStatusQuestion(
      @Nullable @JsonProperty(PROP_INITIATOR_REGEX) NodesSpecifier initiatorRegex,
      @Nullable @JsonProperty(PROP_RESPONDER_REGEX) NodesSpecifier responderRegex,
      @Nullable @JsonProperty(PROP_STATUS_REGEX) String statusRegex) {
    _initiatorRegex = firstNonNull(initiatorRegex, NodesSpecifier.ALL);
    _responderRegex = firstNonNull(responderRegex, NodesSpecifier.ALL);
    _statusRegex =
        Strings.isNullOrEmpty(statusRegex)
            ? Pattern.compile(".*")
            : Pattern.compile(statusRegex.toUpperCase());
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

  @JsonProperty(PROP_STATUS_REGEX)
  public String getStatusRegex() {
    return _statusRegex.toString();
  }

  boolean matchesStatus(@Nullable IpsecSessionStatus status) {
    return status != null && _statusRegex.matcher(status.toString()).matches();
  }
}
