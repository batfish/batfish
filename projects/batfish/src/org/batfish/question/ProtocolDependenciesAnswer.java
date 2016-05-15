package org.batfish.question;

import java.util.Map;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.questions.ProtocolDependenciesQuestion;
import org.batfish.main.Batfish;
import org.batfish.protocoldependency.ProtocolDependencyAnalysis;

public class ProtocolDependenciesAnswer extends Answer {

   public ProtocolDependenciesAnswer(Batfish batfish,
         ProtocolDependenciesQuestion question) {
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      ProtocolDependencyAnalysis analysis = new ProtocolDependencyAnalysis(
            configurations);
      analysis.printDependencies(batfish.getLogger());
      analysis.writeGraphs(batfish.getSettings(), batfish.getLogger());

   }

}
