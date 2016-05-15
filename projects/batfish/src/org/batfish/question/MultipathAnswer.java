package org.batfish.question;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.collections.NodeSet;
import org.batfish.datamodel.questions.MultipathQuestion;
import org.batfish.main.Batfish;
import org.batfish.z3.MultipathInconsistencyQuerySynthesizer;
import org.batfish.z3.NodJob;
import org.batfish.z3.Synthesizer;

public class MultipathAnswer extends Answer {

   public MultipathAnswer(Batfish batfish, MultipathQuestion question) {
      batfish.checkDataPlaneQuestionDependencies();
      String tag = batfish.getFlowTag();
      Map<String, Configuration> configurations = batfish.loadConfigurations();
      File dataPlanePath = new File(batfish.getEnvSettings().getDataPlanePath());
      Set<Flow> flows = null;
      Synthesizer dataPlaneSynthesizer = batfish.synthesizeDataPlane(
            configurations, dataPlanePath);
      List<NodJob> jobs = new ArrayList<NodJob>();
      for (String node : configurations.keySet()) {
         MultipathInconsistencyQuerySynthesizer query = new MultipathInconsistencyQuerySynthesizer(
               node);
         NodeSet nodes = new NodeSet();
         nodes.add(node);
         NodJob job = new NodJob(dataPlaneSynthesizer, query, nodes, tag);
         jobs.add(job);
      }

      flows = batfish.computeNodOutput(jobs);
      Map<String, StringBuilder> trafficFactBins = new LinkedHashMap<String, StringBuilder>();
      Batfish.initTrafficFactBins(trafficFactBins);
      StringBuilder wSetFlowOriginate = trafficFactBins.get("SetFlowOriginate");
      for (Flow flow : flows) {
         wSetFlowOriginate.append(flow.toLBLine());
      }
      batfish.dumpTrafficFacts(trafficFactBins);

   }

}
