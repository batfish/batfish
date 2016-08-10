package org.batfish.answerer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.OspfLoopbacksAnswerElement;
import org.batfish.datamodel.questions.OspfLoopbacksQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class OspfLoopbacksAnswerer extends Answerer {

   public OspfLoopbacksAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {

      OspfLoopbacksQuestion question = (OspfLoopbacksQuestion) _question;

      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      OspfLoopbacksAnswerElement answerElement = new OspfLoopbacksAnswerElement();

      _batfish.checkConfigurations(testrigSettings);
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);

      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         Configuration c = e.getValue();
         for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
            String interfaceName = e2.getKey();
            Interface iface = e2.getValue();
            if (iface.isLoopback(c.getConfigurationFormat())) {
               if (iface.getOspfEnabled()) {
                  // ospf is running either passively or actively
                  answerElement.add(answerElement.getRunning(), hostname,
                        interfaceName);
                  if (iface.getOspfPassive()) {
                     answerElement.add(answerElement.getPassive(), hostname,
                           interfaceName);
                  }
                  else {
                     answerElement.add(answerElement.getActive(), hostname,
                           interfaceName);
                  }
               }
               else {
                  // check if exported as external ospf route
                  boolean exported = false;
                  OspfProcess proc = c.getOspfProcess();
                  if (proc != null) {
                     String exportPolicyName = proc.getExportPolicy();
                     if (exportPolicyName != null) {
                        RoutingPolicy exportPolicy = c.getRoutingPolicies()
                              .get(exportPolicyName);
                        if (exportPolicy != null) {
                           for (Prefix prefix : iface.getAllPrefixes()) {
                              Route route = new Route(hostname, prefix, null,
                                    null, interfaceName, 0, 0,
                                    RoutingProtocol.CONNECTED, -1);
                              if (exportPolicy.permits(route)) {
                                 exported = true;
                              }
                           }
                        }
                     }
                  }

                  if (exported) {
                     answerElement.add(answerElement.getExported(), hostname,
                           interfaceName);
                  }
                  else {
                     // not exported, so should be inactive
                     answerElement.add(answerElement.getInactive(), hostname,
                           interfaceName);
                  }
               }
            }
         }
      }

      return answerElement;
   }

}
