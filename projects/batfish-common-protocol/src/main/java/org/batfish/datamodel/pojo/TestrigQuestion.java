package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;

/**
 * The {@link TestrigQuestion TestrigQuestion} is an Object representation of the question used for
 * Batfish service.
 *
 * <p>{@link TestrigQuestion TestrigQuestion} contains a list of {@link Question questions} that
 * need to be analyzed, along with the name of the question.
 */
@JsonInclude(Include.NON_NULL)
public class TestrigQuestion {

  private static final String PROP_NAME = "name";
  private static final String PROP_QUESTION = "question";

  private String _name;
  private @Nullable Question _question;

  public static TestrigQuestion of(@JsonProperty(PROP_NAME) String name) {
    return of(name, null);
  }

  @JsonCreator
  public static TestrigQuestion of(
      @JsonProperty(PROP_NAME) String name,
      @Nullable @JsonProperty(PROP_QUESTION) Question question) {
    return new TestrigQuestion(name, question);
  }

  private TestrigQuestion(String name, @Nullable Question question) {
    this._name = name;
    this._question = question;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @Nullable
  @JsonProperty(PROP_QUESTION)
  public Question getQuestion() {
    return _question;
  }

  @JsonProperty(PROP_NAME)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(PROP_QUESTION)
  public void setQuestion(Question question) {
    _question = question;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(TestrigQuestion.class)
        .add(PROP_NAME, _name)
        .add(PROP_QUESTION, _question)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TestrigQuestion)) {
      return false;
    }
    TestrigQuestion other = (TestrigQuestion) o;
    return Objects.equals(_name, other._name) && Objects.equals(_question, other._question);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _question);
  }
}
