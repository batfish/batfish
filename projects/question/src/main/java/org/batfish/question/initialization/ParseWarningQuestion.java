package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;

/** A question that returns a table with the parse warnings for each file. */
public final class ParseWarningQuestion extends Question {

  private static final boolean DEFAULT_AGGREGATE_DUPLICATES = false;

  private static final String PROP_AGGREGATE_DUPLICATES = "aggregateDuplicates";

  private final boolean _aggregateDuplicates;

  // package-private constructor
  ParseWarningQuestion() {
    this(null);
  }

  @JsonCreator
  public ParseWarningQuestion(
      @Nullable @JsonProperty(PROP_AGGREGATE_DUPLICATES) Boolean aggregate) {
    _aggregateDuplicates = firstNonNull(aggregate, DEFAULT_AGGREGATE_DUPLICATES);
  }

  public boolean getAggregateDuplicates() {
    return _aggregateDuplicates;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "parseWarning";
  }
}
