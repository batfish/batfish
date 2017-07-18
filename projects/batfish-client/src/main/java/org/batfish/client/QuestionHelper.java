package org.batfish.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.questions.IQuestion;
import org.batfish.datamodel.questions.IReachabilityQuestion;
import org.batfish.datamodel.questions.ITracerouteQuestion;
import org.batfish.datamodel.questions.Question;

public class QuestionHelper {

   public enum MacroType {
      CHECKPROTECTION("checkprotection"),
      CHECKREACHABILITY("checkreachability"),
      TRACEROUTE("traceroute");

      private final static Map<String, MacroType> _map = buildMap();

      private static Map<String, MacroType> buildMap() {
         Map<String, MacroType> map = new HashMap<>();
         for (MacroType value : MacroType.values()) {
            String name = value._name;
            map.put(name, value);
         }
         return Collections.unmodifiableMap(map);
      }

      @JsonCreator
      public static MacroType fromName(String name) {
         MacroType instance = _map.get(name.toLowerCase());
         if (instance == null) {
            throw new BatfishException(
                  "Not a valid MacroType: \"" + name + "\"");
         }
         return instance;
      }

      private final String _name;

      private MacroType(String name) {
         _name = name;
      }

      public String macroTypeName() {
         return _name;
      }
   }

   public static final String MACRO_PREFIX = "#";

   public static String getParametersString(Map<String, String> parameters)
         throws Exception {
      String retString = "{\n";

      for (String paramKey : parameters.keySet()) {
         retString += String.format("\"%s\" : %s,\n", paramKey,
               parameters.get(paramKey));
      }

      retString += "}\n";

      return retString;
   }

   public static Question getQuestion(
         String questionTypeStr,
         Map<String, Supplier<Question>> questions) {
      Supplier<Question> supplier = questions.get(questionTypeStr);
      if (supplier == null) {
         throw new BatfishException("No question found of type: " + questionTypeStr + ". Did you include the questions plugins directory in your JVM arguments?");
      }
      Question question = supplier.get();
      return question;
   }

   public static String getQuestionString(
         String questionTypeStr,
         Map<String, Supplier<Question>> questions, boolean full) {
      Question question = getQuestion(questionTypeStr, questions);
      if (full) {
         return question.toFullJsonString();
      }
      else {
         return question.toJsonString();
      }
   }

   public static IQuestion getReachabilityQuestion(
         String dstIp,
         String protocolStr, String ingressNodeRegex, ForwardingAction action,
         Map<String, Supplier<Question>> questions) {

      if (!questions.containsKey(IReachabilityQuestion.NAME)) {
         throw new BatfishException("Reachability question not found. Did you include the questions plugins directory in your JVM arguments?");
      }

      IReachabilityQuestion question = (IReachabilityQuestion) questions
            .get(IReachabilityQuestion.NAME).get();

      question.setDstIps(new TreeSet<>(
            Collections.singleton(new IpWildcard(new Ip(dstIp)))));

      boolean inverted = false;

      if (protocolStr.startsWith("!")) {
         inverted = true;
         protocolStr = protocolStr.substring(1);
      }
      SortedSet<Protocol> protocols = new TreeSet<>(
            Collections.singleton(Protocol.fromString(protocolStr)));
      if (inverted) {
         question.setNotDstProtocols(protocols);
      }
      else {
         question.setDstProtocols(protocols);
      }

      if (ingressNodeRegex != null) {
         question.setIngressNodeRegex(ingressNodeRegex);
      }

      SortedSet<ForwardingAction> actionSet = new TreeSet<>();
      actionSet.add(action);
      question.setActions(actionSet);

      return question;
   }

   public static String resolveMacro(
         String macroName, String paramsLine,
         Map<String, Supplier<Question>> questions) {
      String macro = macroName.replace(MACRO_PREFIX, "");
      MacroType macroType = MacroType.fromName(macro);

      switch (macroType) {
      case CHECKPROTECTION: {
         String[] words = paramsLine.split(" ");
         if (words.length < 2 || words.length > 3) {
            throw new BatfishException(
                  "Incorrect usage for noreachability macro. "
                        +
                        "Should be:\n #checkreachability <dstip> <protocol> [<ingressNodeRegex>]");
         }
         String dstIp = words[0];
         String protocol = words[1];
         String ingressNodeRegex = (words.length == 3) ? words[2] : null;

         return getReachabilityQuestion(dstIp, protocol, ingressNodeRegex,
               ForwardingAction.ACCEPT, questions).toJsonString();
      }
      case CHECKREACHABILITY: {
         String[] words = paramsLine.split(" ");
         if (words.length < 2 || words.length > 3) {
            throw new BatfishException(
                  "Incorrect usage for noreachability macro. "
                        +
                        "Should be:\n #checkreachability <dstip> <protocol> [<ingressNodeRegex>]");
         }
         String dstIp = words[0];
         String protocol = words[1];
         String ingressNodeRegex = (words.length == 3) ? words[2] : null;

         return getReachabilityQuestion(dstIp, protocol, ingressNodeRegex,
               ForwardingAction.DROP, questions).toJsonString();
      }
      case TRACEROUTE: {
         String[] words = paramsLine.split(" ");
         if (words.length < 2 || words.length > 3) {
            throw new BatfishException("Incorrect usage for traceroute macro. "
                  + "Should be:\n #traceroute <srcNode> <dstip> [<protocol>]");
         }
         ITracerouteQuestion question = (ITracerouteQuestion) questions
               .get(ITracerouteQuestion.NAME).get();
         String srcNode = words[0];
         String dstIp = words[1];

         question.setIngressNode(srcNode);
         question.setDstIp(new Ip(dstIp));

         if (words.length == 3) {
            String protocolStr = words[2];
            Protocol protocol = Protocol.fromString(protocolStr);
            question.setDstProtocol(protocol);
         }
         // else {
         // question.setIpProtocol(IpProtocol.ICMP);
         // }
         return question.toJsonString();
      }
      default:
         throw new BatfishException("Unknown macrotype: " + macroType);
      }
   }
}
