package org.batfish.answerer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowBuilder;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.TracerouteQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class TracerouteAnswerer extends Answerer {

   public TracerouteAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      TracerouteQuestion question  = (TracerouteQuestion) _question;
      _batfish.checkDataPlaneQuestionDependencies(testrigSettings);
      Set<FlowBuilder> flowBuilders = question.getFlowBuilders();
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      String tag = _batfish.getFlowTag(testrigSettings);
      for (FlowBuilder flowBuilder : flowBuilders) {
         flowBuilder.setTag(tag);
         Flow flow = flowBuilder.build();
         wSetFlowOriginate.append(flow.toLBLine());
      }
      _batfish.dumpTrafficFacts(trafficFactBins);
      _batfish.nlsTraffic(testrigSettings);
      AnswerElement answerElement = _batfish.getHistory(testrigSettings);
      return answerElement;
   }

}
