package org.batfish.answerer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.answers.AclLinesAnswerElement;
import org.batfish.datamodel.answers.AclLinesAnswerElement.AclReachabilityEntry;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.AclReachabilityQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings;
import org.batfish.main.Settings.TestrigSettings;
import org.batfish.z3.AclLine;
import org.batfish.z3.AclReachabilityQuerySynthesizer;
import org.batfish.z3.EarliestMoreGeneralReachableLineQuerySynthesizer;
import org.batfish.z3.NodFirstUnsatJob;
import org.batfish.z3.NodSatJob;
import org.batfish.z3.Synthesizer;

public class AclReachabilityAnswerer extends Answerer {

   public AclReachabilityAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {
      Settings settings = _batfish.getSettings();
      AclReachabilityQuestion question = (AclReachabilityQuestion) _question;

      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getNodeRegex() + "\"", e);
      }

      Pattern aclNameRegex;
      try {
         aclNameRegex = Pattern.compile(question.getAclNameRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + question.getAclNameRegex() + "\"", e);
      }

      _batfish.checkConfigurations();
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);
      List<NodSatJob<AclLine>> jobs = new ArrayList<NodSatJob<AclLine>>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         Configuration c = e.getValue();
         for (Entry<String, IpAccessList> e2 : c.getIpAccessLists().entrySet()) {
            String aclName = e2.getKey();
            if (!aclNameRegex.matcher(aclName).matches()) {
               continue;
            }
            // skip juniper srx inbound filters, as they can't really contain
            // operator error
            if (aclName.contains("~ZONE_INTERFACE_FILTER~")
                  || aclName.contains("~INBOUND_ZONE_FILTER~")) {
               continue;
            }
            IpAccessList acl = e2.getValue();
            int numLines = acl.getLines().size();
            if (numLines == 0) {
               _logger.redflag("RED_FLAG: Acl \"" + hostname + ":" + aclName
                     + "\" contains no lines\n");
               continue;
            }
            AclReachabilityQuerySynthesizer query = new AclReachabilityQuerySynthesizer(
                  hostname, aclName, numLines);
            Synthesizer aclSynthesizer = synthesizeAcls(Collections
                  .singletonMap(hostname, c));
            NodSatJob<AclLine> job = new NodSatJob<AclLine>(settings,
                  aclSynthesizer, query);
            jobs.add(job);
         }
      }
      Map<AclLine, Boolean> output = new TreeMap<AclLine, Boolean>();
      _batfish.computeNodSatOutput(jobs, output);

      // rearrange output for next step
      Map<String, Map<String, List<AclLine>>> arrangedAclLines = new TreeMap<String, Map<String, List<AclLine>>>();
      for (Entry<AclLine, Boolean> e : output.entrySet()) {
         AclLine line = e.getKey();
         String hostname = line.getHostname();
         Map<String, List<AclLine>> byAclName = arrangedAclLines.get(hostname);
         if (byAclName == null) {
            byAclName = new TreeMap<String, List<AclLine>>();
            arrangedAclLines.put(hostname, byAclName);
         }
         String aclName = line.getAclName();
         List<AclLine> aclLines = byAclName.get(aclName);
         if (aclLines == null) {
            aclLines = new ArrayList<AclLine>();
            byAclName.put(aclName, aclLines);
         }
         aclLines.add(line);
      }

      // now get earliest more general lines
      List<NodFirstUnsatJob<AclLine, Integer>> step2Jobs = new ArrayList<NodFirstUnsatJob<AclLine, Integer>>();
      for (Entry<String, Map<String, List<AclLine>>> e : arrangedAclLines
            .entrySet()) {
         String hostname = e.getKey();
         Configuration c = configurations.get(hostname);
         Synthesizer aclSynthesizer = synthesizeAcls(Collections.singletonMap(
               hostname, c));
         Map<String, List<AclLine>> byAclName = e.getValue();
         for (Entry<String, List<AclLine>> e2 : byAclName.entrySet()) {
            String aclName = e2.getKey();
            IpAccessList ipAccessList = c.getIpAccessLists().get(aclName);
            List<AclLine> lines = e2.getValue();
            for (int i = 0; i < lines.size(); i++) {
               AclLine line = lines.get(i);
               boolean reachable = output.get(line);
               if (!reachable) {
                  List<AclLine> toCheck = new ArrayList<AclLine>();
                  for (int j = 0; j < i; j++) {
                     AclLine earlierLine = lines.get(j);
                     boolean earlierIsReachable = output.get(earlierLine);
                     if (earlierIsReachable) {
                        toCheck.add(earlierLine);
                     }
                  }
                  EarliestMoreGeneralReachableLineQuerySynthesizer query = new EarliestMoreGeneralReachableLineQuerySynthesizer(
                        line, toCheck, ipAccessList);
                  NodFirstUnsatJob<AclLine, Integer> job = new NodFirstUnsatJob<AclLine, Integer>(
                        settings, aclSynthesizer, query);
                  step2Jobs.add(job);
               }
            }
         }
      }
      Map<AclLine, Integer> step2Output = new TreeMap<AclLine, Integer>();
      _batfish.computeNodFirstUnsatOutput(step2Jobs, step2Output);
      for (AclLine line : output.keySet()) {
         Integer earliestMoreGeneralReachableLine = step2Output.get(line);
         line.setEarliestMoreGeneralReachableLine(earliestMoreGeneralReachableLine);
      }

      Set<Pair<String, String>> aclsWithUnreachableLines = new TreeSet<Pair<String, String>>();
      Set<Pair<String, String>> allAcls = new TreeSet<Pair<String, String>>();
      int numUnreachableLines = 0;
      int numLines = output.entrySet().size();
      for (Entry<AclLine, Boolean> e : output.entrySet()) {
         AclLine aclLine = e.getKey();
         boolean sat = e.getValue();
         String hostname = aclLine.getHostname();
         String aclName = aclLine.getAclName();
         Pair<String, String> qualifiedAclName = new Pair<String, String>(
               hostname, aclName);
         allAcls.add(qualifiedAclName);
         if (!sat) {
            numUnreachableLines++;
            aclsWithUnreachableLines.add(qualifiedAclName);
         }
      }
      AclLinesAnswerElement answerElement = new AclLinesAnswerElement();
      for (Entry<AclLine, Boolean> e : output.entrySet()) {
         AclLine aclLine = e.getKey();
         int index = aclLine.getLine();
         boolean sat = e.getValue();
         String hostname = aclLine.getHostname();
         String aclName = aclLine.getAclName();
         Pair<String, String> qualifiedAclName = new Pair<String, String>(
               hostname, aclName);
         IpAccessList ipAccessList = configurations.get(hostname)
               .getIpAccessLists().get(aclName);
         IpAccessListLine ipAccessListLine = ipAccessList.getLines().get(index);
         AclReachabilityEntry line = new AclReachabilityEntry(index,
               ipAccessListLine.getName());
         if (aclsWithUnreachableLines.contains(qualifiedAclName)) {
            if (sat) {
               _logger.debugf("%s:%s:%d:'%s' is REACHABLE\n", hostname,
                     aclName, line.getIndex(), line.getName());
               answerElement.addReachableLine(hostname, ipAccessList, line);
            }
            else {
               _logger.debugf("%s:%s:%d:'%s' is UNREACHABLE\n\t%s\n", hostname,
                     aclName, line.getIndex(), line.getName(),
                     ipAccessListLine.toString());
               Integer earliestMoreGeneralLineIndex = aclLine
                     .getEarliestMoreGeneralReachableLine();
               if (earliestMoreGeneralLineIndex != null) {
                  IpAccessListLine earliestMoreGeneralLine = ipAccessList
                        .getLines().get(earliestMoreGeneralLineIndex);
                  line.setEarliestMoreGeneralLineIndex(earliestMoreGeneralLineIndex);
                  line.setEarliestMoreGeneralLineName(earliestMoreGeneralLine
                        .getName());
               }
               answerElement.addUnreachableLine(hostname, ipAccessList, line);
               aclsWithUnreachableLines.add(qualifiedAclName);
            }
         }
         else {
            answerElement.addReachableLine(hostname, ipAccessList, line);
         }
      }
      for (Pair<String, String> qualfiedAcl : aclsWithUnreachableLines) {
         String hostname = qualfiedAcl.getFirst();
         String aclName = qualfiedAcl.getSecond();
         _logger.debugf("%s:%s has at least 1 unreachable line\n", hostname,
               aclName);
      }
      int numAclsWithUnreachableLines = aclsWithUnreachableLines.size();
      int numAcls = allAcls.size();
      double percentUnreachableAcls = 100d * numAclsWithUnreachableLines
            / numAcls;
      double percentUnreachableLines = 100d * numUnreachableLines / numLines;
      _logger.debugf("SUMMARY:\n");
      _logger.debugf("\t%d/%d (%.1f%%) acls have unreachable lines\n",
            numAclsWithUnreachableLines, numAcls, percentUnreachableAcls);
      _logger.debugf("\t%d/%d (%.1f%%) acl lines are unreachable\n",
            numUnreachableLines, numLines, percentUnreachableLines);

      return answerElement;
   }

   private Synthesizer synthesizeAcls(Map<String, Configuration> configurations) {
      _logger.info("\n*** GENERATING Z3 LOGIC ***\n");
      _batfish.resetTimer();

      _logger.info("Synthesizing Z3 ACL logic...");
      Synthesizer s = new Synthesizer(configurations, _batfish.getSettings()
            .getSimplify());

      List<String> warnings = s.getWarnings();
      int numWarnings = warnings.size();
      if (numWarnings == 0) {
         _logger.info("OK\n");
      }
      else {
         for (String warning : warnings) {
            _logger.warn(warning);
         }
      }
      _batfish.printElapsedTime();
      return s;
   }

}
