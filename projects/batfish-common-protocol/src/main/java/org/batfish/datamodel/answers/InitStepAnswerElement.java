package org.batfish.datamodel.answers;

import java.util.SortedMap;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;

public interface InitStepAnswerElement extends AnswerElement {

  SortedMap<String, BatfishException.BatfishStackTrace> getErrors();

  SortedMap<String, Warnings> getWarnings();

  void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors);

  void setWarnings(SortedMap<String, Warnings> warnings);
}
