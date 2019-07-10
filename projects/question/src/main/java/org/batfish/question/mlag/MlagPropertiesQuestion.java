package org.batfish.question.mlag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** MLAG properties question */
@ParametersAreNonnullByDefault
public final class MlagPropertiesQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_MLAG_IDS = "mlagIds";

  static final String MATCH_ALL = ".*";

  private final @Nullable String _nodeSpecInput;
  private final @Nullable String _mlagIdSpecInput;

  MlagPropertiesQuestion() {
    this(null, MATCH_ALL);
  }

  public MlagPropertiesQuestion(@Nullable String nodeSpecInput, @Nullable String mlagIdSpecInput) {
    _nodeSpecInput = nodeSpecInput;
    _mlagIdSpecInput = mlagIdSpecInput;
  }

  @JsonCreator
  private static MlagPropertiesQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodeSpecInput,
      @Nullable @JsonProperty(PROP_MLAG_IDS) String mlagIdSpecInput) {
    return new MlagPropertiesQuestion(nodeSpecInput, mlagIdSpecInput);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "mlagProperties";
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodeSpecInput() {
    return _nodeSpecInput;
  }

  @JsonProperty(PROP_MLAG_IDS)
  @Nullable
  public String getMlagIdSpecInput() {
    return _mlagIdSpecInput;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MlagPropertiesQuestion)) {
      return false;
    }
    MlagPropertiesQuestion that = (MlagPropertiesQuestion) o;
    return Objects.equals(_nodeSpecInput, that._nodeSpecInput)
        && Objects.equals(_mlagIdSpecInput, that._mlagIdSpecInput);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeSpecInput, _mlagIdSpecInput);
  }
}
