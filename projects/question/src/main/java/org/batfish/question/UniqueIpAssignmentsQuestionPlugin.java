package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.Question;

public class UniqueIpAssignmentsQuestionPlugin extends QuestionPlugin {

  public static class UniqueIpAssignmentsAnswerElement implements AnswerElement {

    private SortedMap<Ip, SortedSet<NodeInterfacePair>> _allIps;

    private SortedMap<Ip, SortedSet<NodeInterfacePair>> _enabledIps;

    public UniqueIpAssignmentsAnswerElement() {
      _allIps = new TreeMap<>();
      _enabledIps = new TreeMap<>();
    }

    public void add(
        SortedMap<Ip, SortedSet<NodeInterfacePair>> map,
        Ip ip,
        String hostname,
        String interfaceName) {
      SortedSet<NodeInterfacePair> interfaces = map.computeIfAbsent(ip, k -> new TreeSet<>());
      interfaces.add(new NodeInterfacePair(hostname, interfaceName));
    }

    public SortedMap<Ip, SortedSet<NodeInterfacePair>> getAllIps() {
      return _allIps;
    }

    public SortedMap<Ip, SortedSet<NodeInterfacePair>> getEnabledIps() {
      return _enabledIps;
    }

    private Object ipsToString(
        String indent, String header, SortedMap<Ip, SortedSet<NodeInterfacePair>> ips) {
      StringBuilder sb = new StringBuilder(indent + header + "\n");
      for (Ip ip : ips.keySet()) {
        sb.append(indent + indent + ip + "\n");
        for (NodeInterfacePair nip : ips.get(ip)) {
          sb.append(indent + indent + indent + nip + "\n");
        }
      }
      return sb.toString();
    }

    @Override
    public String prettyPrint() {
      StringBuilder sb = new StringBuilder("Results for unique IP assignment check\n");
      if (_allIps != null) {
        sb.append(ipsToString("  ", "All IPs", _allIps));
      }
      if (_enabledIps != null) {
        sb.append(ipsToString("  ", "Enabled IPs", _enabledIps));
      }
      return sb.toString();
    }

    public void setAllIps(SortedMap<Ip, SortedSet<NodeInterfacePair>> allIps) {
      _allIps = allIps;
    }

    public void setEnabledIps(SortedMap<Ip, SortedSet<NodeInterfacePair>> enabledIps) {
      _enabledIps = enabledIps;
    }
  }

  public static class UniqueIpAssignmentsAnswerer extends Answerer {

    // private final Batfish _batfish;
    // private final UniqueIpAssignmentsQuestion _question;
    //
    // public UniqueIpAssignmentsReplier(Batfish batfish,
    // UniqueIpAssignmentsQuestion question) {
    // _batfish = batfish;
    // _question = question;
    //
    // if (question.getDifferential()) {
    // _batfish.checkEnvironmentExists(_batfish.getBaseTestrigSettings());
    // _batfish.checkEnvironmentExists(_batfish.getDeltaTestrigSettings());
    // UniqueIpAssignmentsAnswerElement before = initAnswerElement(batfish
    // .getBaseTestrigSettings());
    // UniqueIpAssignmentsAnswerElement after = initAnswerElement(batfish
    // .getDeltaTestrigSettings());
    // ObjectMapper mapper = new BatfishObjectMapper();
    // try {
    // String beforeJsonStr = mapper.writeValueAsString(before);
    // String afterJsonStr = mapper.writeValueAsString(after);
    // JSONObject beforeJson = new JSONObject(beforeJsonStr);
    // JSONObject afterJson = new JSONObject(afterJsonStr);
    // JsonDiff diff = new JsonDiff(beforeJson, afterJson);
    // addAnswerElement(new JsonDiffAnswerElement(diff));
    // }
    // catch (JsonProcessingException | JSONException e) {
    // throw new BatfishException(
    // "Could not convert diff element to json string", e);
    // }
    // }
    // else {
    // UniqueIpAssignmentsAnswerElement answerElement =
    // initAnswerElement(batfish
    // .getTestrigSettings());
    // addAnswerElement(answerElement);
    // }
    //
    // }

    public UniqueIpAssignmentsAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {

      UniqueIpAssignmentsQuestion question = (UniqueIpAssignmentsQuestion) _question;

      Pattern nodeRegex;
      try {
        nodeRegex = Pattern.compile(question.getNodeRegex());
      } catch (PatternSyntaxException e) {
        throw new BatfishException(
            "Supplied regex for nodes is not a valid java regex: \""
                + question.getNodeRegex()
                + "\"",
            e);
      }
      UniqueIpAssignmentsAnswerElement answerElement = new UniqueIpAssignmentsAnswerElement();
      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      MultiSet<Ip> allIps = new TreeMultiSet<>();
      MultiSet<Ip> enabledIps = new TreeMultiSet<>();
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
              answerElement.add(answerElement.getAllIps(), ip, hostname, interfaceName);
            }
            if (iface.getActive()) {
              if (enabledIps.count(ip) != 1) {
                answerElement.add(answerElement.getEnabledIps(), ip, hostname, interfaceName);
              }
            }
          }
        }
      }
      return answerElement;
    }
  }

  // <question_page_comment>

  /**
   * Lists IP addresses that are assigned to multiple interfaces.
   *
   * <p>Except in cases of anycast, an IP address should be assigned to only one interface. This
   * question produces the list of IP addresses for which this condition does not hold.
   *
   * @type UniqueIpAssignments multifile
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @param verbose Details coming
   * @example bf_answer("UniqueIpAssignments", nodeRegex='as2.*') Answers the question only for
   *     nodes whose names start with 'as2'.
   */
  public static class UniqueIpAssignmentsQuestion extends Question {

    private static final String NODE_REGEX_VAR = "nodeRegex";

    private static final String VERBOSE_VAR = "verbose";

    private String _nodeRegex;

    private boolean _verbose;

    public UniqueIpAssignmentsQuestion() {
      _nodeRegex = ".*";
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "uniqueipassignments";
    }

    @JsonProperty(NODE_REGEX_VAR)
    public String getNodeRegex() {
      return _nodeRegex;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @JsonProperty(VERBOSE_VAR)
    public boolean getVerbose() {
      return _verbose;
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format(
              "uniqueipassignments %snodeRegex=\"%s\" | verbose=%s",
              prettyPrintBase(), _nodeRegex, _verbose);
      return retString;
    }

    @JsonProperty(NODE_REGEX_VAR)
    public void setNodeRegex(String nodeRegex) {
      _nodeRegex = nodeRegex;
    }

    @JsonProperty(VERBOSE_VAR)
    public void setVerbose(boolean verbose) {
      _verbose = verbose;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new UniqueIpAssignmentsAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new UniqueIpAssignmentsQuestion();
  }
}
