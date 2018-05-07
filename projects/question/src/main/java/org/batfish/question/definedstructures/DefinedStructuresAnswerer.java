package org.batfish.question.definedstructures;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.questions.Question;

public class DefinedStructuresAnswerer extends Answerer {

  public DefinedStructuresAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    DefinedStructuresQuestion question = (DefinedStructuresQuestion) _question;
    Multiset<DefinedStructureRow> structures = rawAnswer(question);
    DefinedStructuresAnswerElement answer =
        new DefinedStructuresAnswerElement(DefinedStructuresAnswerElement.createMetadata(question));
    answer.postProcessAnswer(question, structures);
    return answer;
  }

  private Multiset<DefinedStructureRow> rawAnswer(DefinedStructuresQuestion question) {
    Multiset<DefinedStructureRow> structures = HashMultiset.create();
    Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(_batfish);

    ConvertConfigurationAnswerElement ccae =
        _batfish.loadConvertConfigurationAnswerElementOrReparse();

    ccae.getDefinedStructures()
        .forEach(
            (nodeName, byStructType) -> {
              if (!includeNodes.contains(nodeName)) {
                return;
              }
              byStructType.forEach(
                  (structType, byStructName) -> {
                    byStructName.forEach(
                        (structName, info) -> {
                          DefinedStructureRow row =
                              new DefinedStructureRow(
                                  nodeName,
                                  structType,
                                  structName,
                                  info.getNumReferrers(),
                                  info.getDefinitionLines());
                          structures.add(row);
                        });
                  });
            });

    return structures;
  }
}
