package org.batfish.question.mlag;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** MLAG properties question */
@ParametersAreNonnullByDefault
public final class MlagPropertiesQuestion extends Question {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_ID_REGEX = "idRegex";

  static final String MATCH_ALL = ".*";

  private final @Nullable String _nodeSpecInput;
  private final @Nonnull String _mlagIdRegex;

  MlagPropertiesQuestion() {
    this(null, MATCH_ALL);
  }

  public MlagPropertiesQuestion(@Nullable String nodeSpecInput, String mlagIdRegex) {
    _nodeSpecInput = nodeSpecInput;
    _mlagIdRegex = mlagIdRegex;
  }

  @JsonCreator
  private static MlagPropertiesQuestion create(
      @Nullable @JsonProperty(PROP_NODES) String nodeSpecInput,
      @Nullable @JsonProperty(PROP_ID_REGEX) String mlagIdRegex) {
    return new MlagPropertiesQuestion(nodeSpecInput, firstNonNull(mlagIdRegex, MATCH_ALL));
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

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(
        _nodeSpecInput, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_ID_REGEX)
  @Nonnull
  public String getMlagIdRegex() {
    return _mlagIdRegex;
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
        && _mlagIdRegex.equals(that._mlagIdRegex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeSpecInput, _mlagIdRegex);
  }
}
