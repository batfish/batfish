package org.batfish.answerer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NeighborType;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.NeighborsAnswerElement;
import org.batfish.datamodel.collections.IpEdge;
import org.batfish.datamodel.questions.NeighborsQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class NeighborsAnswerer extends Answerer {

   private boolean _remoteBgpNeighborsInitialized;

   public NeighborsAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      NeighborsQuestion question = (NeighborsQuestion) _question;
      Pattern node1Regex;
      Pattern node2Regex;

      try {
         node1Regex = Pattern.compile(question.getNode1Regex());
         node2Regex = Pattern.compile(question.getNode2Regex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               String.format(
                     "One of the supplied regexes (%s  OR  %s) is not a valid java regex.",
                     question.getNode1Regex(), question.getNode2Regex()), e);
      }

      NeighborsAnswerElement answerElement = new NeighborsAnswerElement();

      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);

//      for (NeighborType nType : question.getNeighborTypes()) {
//         switch (nType) {
//         case EBGP:
//            answerElement.initEbgpNeighbors();
//            initRemoteBgpNeighbors(_batfish, configurations);
//            break;
//         case IBGP:
//            answerElement.initIbgpNeighbors();
//            initRemoteBgpNeighbors(_batfish, configurations);
//            break;
//         case LAN:
//            answerElement.initLanNeighbors();
//            break;
//         default:
//            throw new BatfishException("Unsupported NeighborType: "
//                  + nType.toString());
//
//         }
//      }

      if (question.getNeighborTypes().contains(NeighborType.EBGP)) {
         answerElement.initEbgpNeighbors();
         initRemoteBgpNeighbors(_batfish, configurations);
         for (Configuration c : configurations.values()) {
            String hostname = c.getHostname();
            BgpProcess proc = c.getBgpProcess();
            if (proc != null) {
               for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
                  BgpNeighbor remoteBgpNeighbor = bgpNeighbor
                        .getRemoteBgpNeighbor();
                  if (remoteBgpNeighbor != null) {
                     boolean ebgp = !bgpNeighbor.getRemoteAs().equals(
                           bgpNeighbor.getLocalAs());
                     if (ebgp) {
                        Configuration remoteHost = remoteBgpNeighbor.getOwner();
                        String remoteHostname = remoteHost.getHostname();
                        Ip localIp = bgpNeighbor.getLocalIp();
                        Ip remoteIp = remoteBgpNeighbor.getLocalIp();
                        answerElement.getEbgpNeighbors().add(
                              new IpEdge(hostname, localIp, remoteHostname,
                                    remoteIp));
                     }
                  }
               }
            }
         }
      }

      if (question.getNeighborTypes().contains(NeighborType.IBGP)) {
         answerElement.initIbgpNeighbors();
         initRemoteBgpNeighbors(_batfish, configurations);
         for (Configuration c : configurations.values()) {
            String hostname = c.getHostname();
            BgpProcess proc = c.getBgpProcess();
            if (proc != null) {
               for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
                  BgpNeighbor remoteBgpNeighbor = bgpNeighbor
                        .getRemoteBgpNeighbor();
                  if (remoteBgpNeighbor != null) {
                     boolean ibgp = bgpNeighbor.getRemoteAs().equals(
                           bgpNeighbor.getLocalAs());
                     if (ibgp) {
                        Configuration remoteHost = remoteBgpNeighbor.getOwner();
                        String remoteHostname = remoteHost.getHostname();
                        Ip localIp = bgpNeighbor.getLocalIp();
                        Ip remoteIp = remoteBgpNeighbor.getLocalIp();
                        answerElement.getIbgpNeighbors().add(
                              new IpEdge(hostname, localIp, remoteHostname,
                                    remoteIp));
                     }
                  }
               }
            }
         }
      }

      if (question.getNeighborTypes().isEmpty()
            || question.getNeighborTypes().contains(NeighborType.LAN)) {
         answerElement.initLanNeighbors();
         Topology topology = _batfish.computeTopology(configurations,
               testrigSettings);

         for (Edge edge : topology.getEdges()) {
            Matcher node1Matcher = node1Regex.matcher(edge.getNode1());
            Matcher node2Matcher = node2Regex.matcher(edge.getNode2());
            if (node1Matcher.matches() && node2Matcher.matches()) {
               answerElement.addLanEdge(edge);
            }
         }
      }

      return answerElement;
   }

   private void initRemoteBgpNeighbors(Batfish batfish,
         Map<String, Configuration> configurations) {
      if (!_remoteBgpNeighborsInitialized) {
         batfish.initRemoteBgpNeighbors(configurations);
         _remoteBgpNeighborsInitialized = true;
      }
   }
}
