package org.batfish.datamodel.answers;

import java.util.SortedMap;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;

public abstract class InitStepAnswerElement extends AnswerElement {

  public abstract SortedMap<String, BatfishException.BatfishStackTrace> getErrors();

  public abstract SortedMap<String, Warnings> getWarnings();

  public abstract void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors);

  public abstract void setWarnings(SortedMap<String, Warnings> warnings);
}
