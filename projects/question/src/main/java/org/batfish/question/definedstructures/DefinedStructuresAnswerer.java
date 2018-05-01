package org.batfish.question.definedstructures;

import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

public class DefinedStructuresAnswerer extends Answerer {

  public DefinedStructuresAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    DefinedStructuresQuestion question = (DefinedStructuresQuestion) _question;

    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(_batfish);

    DefinedStructuresAnswerElement answer =
        new DefinedStructuresAnswerElement(DefinedStructuresAnswerElement.createMetadata(question));

    // do the logic here

    return answer;
  }
}
