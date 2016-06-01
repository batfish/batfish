package org.batfish.question;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.DiffLabel;
import org.batfish.datamodel.answers.UniqueIpAssignmentsAnswerElement;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.UniqueIpAssignmentsQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.EnvironmentSettings;

public class UniqueIpAssignmentsAnswer extends Answer {

   private static void computeAdded(Map<Ip, SortedSet<NodeInterfacePair>> dst,
         Map<Ip, SortedSet<NodeInterfacePair>> before,
         Map<Ip, SortedSet<NodeInterfacePair>> after) {
      for (Entry<Ip, SortedSet<NodeInterfacePair>> eAfter : after.entrySet()) {
         Ip key = eAfter.getKey();
         SortedSet<NodeInterfacePair> sAfter = eAfter.getValue();
         if (!before.containsKey(key)) {
            dst.put(key, sAfter);
         }
         else {
            SortedSet<NodeInterfacePair> sBefore = before.get(key);
            SortedSet<NodeInterfacePair> sDst = new TreeSet<NodeInterfacePair>();
            sDst.addAll(sAfter);
            sDst.removeAll(sBefore);
            if (!sDst.isEmpty()) {
               dst.put(key, sDst);
            }
         }
      }
   }

   private final Batfish _batfish;

   private final UniqueIpAssignmentsQuestion _question;

   public UniqueIpAssignmentsAnswer(Batfish batfish,
         UniqueIpAssignmentsQuestion question) {
      _batfish = batfish;
      _question = question;
      _batfish.checkConfigurations();

      if (question.getDifferential()) {
         _batfish.checkEnvironmentExists(_batfish.getBaseEnvSettings());
         _batfish.checkEnvironmentExists(_batfish.getDiffEnvSettings());
         UniqueIpAssignmentsAnswerElement before = initAnswerElement(
               batfish.getBaseEnvSettings(), DiffLabel.BEFORE);
         UniqueIpAssignmentsAnswerElement after = initAnswerElement(
               batfish.getDiffEnvSettings(), DiffLabel.AFTER);
         UniqueIpAssignmentsAnswerElement deleted = deleted(before, after);
         UniqueIpAssignmentsAnswerElement added = added(before, after);
         if (question.getVerbose()) {
            addAnswerElement(before);
            addAnswerElement(after);
         }
         addAnswerElement(deleted);
         addAnswerElement(added);
      }
      else {
         UniqueIpAssignmentsAnswerElement answerElement = initAnswerElement(
               batfish.getBaseEnvSettings(), null);
         addAnswerElement(answerElement);
      }

   }

   private UniqueIpAssignmentsAnswerElement added(
         UniqueIpAssignmentsAnswerElement before,
         UniqueIpAssignmentsAnswerElement after) {
      UniqueIpAssignmentsAnswerElement answerElement = new UniqueIpAssignmentsAnswerElement();
      answerElement.setDiffLabel(DiffLabel.ADDED);
      computeAdded(answerElement.getAllIps(), before.getAllIps(),
            after.getAllIps());
      return answerElement;
   }

   private UniqueIpAssignmentsAnswerElement deleted(
         UniqueIpAssignmentsAnswerElement before,
         UniqueIpAssignmentsAnswerElement after) {
      UniqueIpAssignmentsAnswerElement answerElement = new UniqueIpAssignmentsAnswerElement();
      answerElement.setDiffLabel(DiffLabel.DELETED);
      computeAdded(answerElement.getAllIps(), after.getAllIps(),
            before.getAllIps());
      return answerElement;
   }

   private UniqueIpAssignmentsAnswerElement initAnswerElement(
         EnvironmentSettings envSettings, DiffLabel diffLabel) {
      Pattern nodeRegex;
      try {
         nodeRegex = Pattern.compile(_question.getNodeRegex());
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for nodes is not a valid java regex: \""
                     + _question.getNodeRegex() + "\"", e);
      }
      UniqueIpAssignmentsAnswerElement answerElement = new UniqueIpAssignmentsAnswerElement();
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(envSettings);
      MultiSet<Ip> allIps = new TreeMultiSet<Ip>();
      MultiSet<Ip> enabledIps = new TreeMultiSet<Ip>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         Configuration c = e.getValue();
         for (Interface iface : c.getInterfaces().values()) {
            for (Prefix prefix : iface.getAllPrefixes()) {
               Ip ip = prefix.getAddress();
               allIps.add(ip);
               if (iface.getActive()) {
                  enabledIps.add(ip);
               }

            }
         }
      }
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         Configuration c = e.getValue();
         for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
            String interfaceName = e2.getKey();
            Interface iface = e2.getValue();
            for (Prefix prefix : iface.getAllPrefixes()) {
               Ip ip = prefix.getAddress();
               if (allIps.count(ip) != 1) {
                  answerElement.add(answerElement.getAllIps(), ip, hostname,
                        interfaceName);
               }
               if (iface.getActive()) {
                  if (enabledIps.count(ip) != 1) {
                     answerElement.add(answerElement.getEnabledIps(), ip,
                           hostname, interfaceName);
                  }
               }

            }
         }
      }
      return answerElement;
   }
}
