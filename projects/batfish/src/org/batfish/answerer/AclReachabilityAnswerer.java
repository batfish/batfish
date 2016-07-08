package org.batfish.answerer;

import java.util.ArrayList;
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
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.AclReachabilityQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings;
import org.batfish.main.Settings.TestrigSettings;
import org.batfish.z3.AclLine;
import org.batfish.z3.AclReachabilityQuerySynthesizer;
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
      Synthesizer aclSynthesizer = synthesizeAcls(configurations);
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
            NodSatJob<AclLine> job = new NodSatJob<AclLine>(settings,
                  aclSynthesizer, query);
            jobs.add(job);
         }
      }
      Map<AclLine, Boolean> output = new TreeMap<AclLine, Boolean>();
      _batfish.computeNodSatOutput(jobs, output);
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
         boolean sat = e.getValue();
         String hostname = aclLine.getHostname();
         String aclName = aclLine.getAclName();
         Pair<String, String> qualifiedAclName = new Pair<String, String>(
               hostname, aclName);
         IpAccessList ipAccessList = configurations.get(hostname)
               .getIpAccessLists().get(aclName);
         int line = aclLine.getLine();
         if (aclsWithUnreachableLines.contains(qualifiedAclName)) {
            if (sat) {
               _logger.outputf("%s:%s:%d is REACHABLE\n", hostname, aclName,
                     line);
               answerElement.addReachableLine(hostname, ipAccessList, line);
            }
            else {
               Configuration c = configurations.get(aclLine.getHostname());
               IpAccessList acl = c.getIpAccessLists()
                     .get(aclLine.getAclName());
               IpAccessListLine ipAccessListLine = acl.getLines().get(line);
               _logger.outputf("%s:%s:%d is UNREACHABLE\n\t%s\n", hostname,
                     aclName, line, ipAccessListLine.toString());
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
         _logger.outputf("%s:%s has at least 1 unreachable line\n", hostname,
               aclName);
      }
      int numAclsWithUnreachableLines = aclsWithUnreachableLines.size();
      int numAcls = allAcls.size();
      double percentUnreachableAcls = 100d * numAclsWithUnreachableLines
            / numAcls;
      double percentUnreachableLines = 100d * numUnreachableLines / numLines;
      _logger.outputf("SUMMARY:\n");
      _logger.outputf("\t%d/%d (%.1f%%) acls have unreachable lines\n",
            numAclsWithUnreachableLines, numAcls, percentUnreachableAcls);
      _logger.outputf("\t%d/%d (%.1f%%) acl lines are unreachable\n",
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
