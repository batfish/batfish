package org.batfish.question.filtertable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** {@link Question} to construct the default policies for a new testrig */
@ParametersAreNonnullByDefault
public class FilterTableQuestion extends Question {
  private static final String PROP_COLUMNS = "columns";
  private static final String PROP_FILTER = "filter";
  private static final String PROP_INNER_QUESTION = "innerQuestion";

  @Nullable private Set<String> _columns;

  @Nullable private Filter _filter;

  private Question _innerQuestion;

  // called by the plugin, so mostly a dummy
  protected FilterTableQuestion() {
    _innerQuestion =
        new Question() {
          @Override
          public boolean getDataPlane() {
            return false;
          }

          @Override
          public String getName() {
            return null;
          }
        };
  }

  @JsonCreator
  public FilterTableQuestion(
      @JsonProperty(PROP_INNER_QUESTION) Question innerQuestion,
      @Nullable @JsonProperty(PROP_FILTER) Filter filter,
      @Nullable @JsonProperty(PROP_COLUMNS) Set<String> columns) {
    if (innerQuestion == null) {
      throw new IllegalArgumentException("InnerQuestion not specified for filter table");
    }
    _innerQuestion = innerQuestion;
    _filter = filter;
    _columns = columns;
  }

  public Set<String> getColumns() {
    return _columns;
  }

  @Override
  public boolean getDataPlane() {
    return _innerQuestion.getDataPlane();
  }

  @JsonProperty(PROP_FILTER)
  public Filter getFilter() {
    return _filter;
  }

  @JsonProperty(PROP_INNER_QUESTION)
  public Question getInnerQuestion() {
    return _innerQuestion;
  }

  @Override
  public String getName() {
    return "filterTable";
  }
}
