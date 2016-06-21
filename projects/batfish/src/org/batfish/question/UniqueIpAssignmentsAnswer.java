package org.batfish.question;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.JsonDiff;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.JsonDiffAnswerElement;
import org.batfish.datamodel.answers.UniqueIpAssignmentsAnswerElement;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.UniqueIpAssignmentsQuestion;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.EnvironmentSettings;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UniqueIpAssignmentsAnswer extends Answer {

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
         UniqueIpAssignmentsAnswerElement before = initAnswerElement(batfish
               .getBaseEnvSettings());
         UniqueIpAssignmentsAnswerElement after = initAnswerElement(batfish
               .getDiffEnvSettings());
         ObjectMapper mapper = new BatfishObjectMapper();
         try {
            String beforeJsonStr = mapper.writeValueAsString(before);
            String afterJsonStr = mapper.writeValueAsString(after);
            JSONObject beforeJson = new JSONObject(beforeJsonStr);
            JSONObject afterJson = new JSONObject(afterJsonStr);
            JsonDiff diff = new JsonDiff(beforeJson, afterJson);
            addAnswerElement(new JsonDiffAnswerElement(diff));
         }
         catch (JsonProcessingException | JSONException e) {
            throw new BatfishException(
                  "Could not convert diff element to json string", e);
         }
      }
      else {
         UniqueIpAssignmentsAnswerElement answerElement = initAnswerElement(batfish
               .getEnvSettings());
         addAnswerElement(answerElement);
      }

   }

   private UniqueIpAssignmentsAnswerElement initAnswerElement(
         EnvironmentSettings envSettings) {
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
