package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import org.batfish.common.BatfishException;
import org.batfish.common.ErrorDetails;
import org.batfish.common.Warnings;

public abstract class InitStepAnswerElement extends AnswerElement {

  protected static final String PROP_ERRORS = "errors";
  protected static final String PROP_ERROR_DETAILS = "errorDetails";
  protected static final String PROP_WARNINGS = "warnings";

  @JsonProperty(PROP_ERRORS)
  public abstract SortedMap<String, BatfishException.BatfishStackTrace> getErrors();

  @JsonProperty(PROP_ERROR_DETAILS)
  public abstract SortedMap<String, ErrorDetails> getErrorDetails();

  @JsonProperty(PROP_WARNINGS)
  public abstract SortedMap<String, Warnings> getWarnings();

  @JsonProperty(PROP_ERRORS)
  public abstract void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors);

  @JsonProperty(PROP_WARNINGS)
  public abstract void setWarnings(SortedMap<String, Warnings> warnings);
}
