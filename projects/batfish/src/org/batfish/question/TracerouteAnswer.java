package org.batfish.question;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowBuilder;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.TracerouteQuestion;
import org.batfish.main.Batfish;

public class TracerouteAnswer extends Answer {

   public TracerouteAnswer(Batfish batfish, TracerouteQuestion question) {
      batfish.checkDataPlaneQuestionDependencies();
      Set<FlowBuilder> flowBuilders = question.getFlowBuilders();
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      String tag = batfish.getFlowTag();
      for (FlowBuilder flowBuilder : flowBuilders) {
         flowBuilder.setTag(tag);
         Flow flow = flowBuilder.build();
         wSetFlowOriginate.append(flow.toLBLine());
      }
      batfish.dumpTrafficFacts(trafficFactBins);
      batfish.nlsTraffic();
      AnswerElement answerElement = batfish.getHistory();
      addAnswerElement(answerElement);
   }

}
