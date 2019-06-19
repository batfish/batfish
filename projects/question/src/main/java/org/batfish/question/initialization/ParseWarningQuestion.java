package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** A question that returns a table with the parse warnings for each file. */
@ParametersAreNonnullByDefault
public final class ParseWarningQuestion extends Question {

  private static final boolean DEFAULT_AGGREGATE_DUPLICATES = false;
  private static final String PROP_AGGREGATE_DUPLICATES = "aggregateDuplicates";

  private final boolean _aggregateDuplicates;

  @JsonCreator
  private static ParseWarningQuestion create(
      @JsonProperty(PROP_AGGREGATE_DUPLICATES) Boolean aggregate) {
    return new ParseWarningQuestion(firstNonNull(aggregate, DEFAULT_AGGREGATE_DUPLICATES));
  }

  // package-private constructor
  ParseWarningQuestion() {
    this(DEFAULT_AGGREGATE_DUPLICATES);
  }

  public ParseWarningQuestion(boolean aggregateDuplicates) {
    _aggregateDuplicates = aggregateDuplicates;
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
