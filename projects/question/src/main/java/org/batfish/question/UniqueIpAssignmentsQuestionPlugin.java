package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.MultiSet;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.TreeMultiSet;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class UniqueIpAssignmentsQuestionPlugin extends QuestionPlugin {

  public static class UniqueIpAssignmentsAnswerElement extends AnswerElement {

    private SortedMap<Ip, SortedSet<NodeInterfacePair>> _duplicateIps;

    public UniqueIpAssignmentsAnswerElement() {
      _duplicateIps = new TreeMap<>();
    }

    public void add(
        SortedMap<Ip, SortedSet<NodeInterfacePair>> map,
        Ip ip,
        String hostname,
        String interfaceName) {
      SortedSet<NodeInterfacePair> interfaces = map.computeIfAbsent(ip, k -> new TreeSet<>());
      interfaces.add(new NodeInterfacePair(hostname, interfaceName));
    }

    public SortedMap<Ip, SortedSet<NodeInterfacePair>> getDuplicateIps() {
      return _duplicateIps;
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
      if (_duplicateIps != null) {
        sb.append(ipsToString("  ", "Duplicate IPs", _duplicateIps));
      }
      return sb.toString();
    }

    public void setDuplicateIps(SortedMap<Ip, SortedSet<NodeInterfacePair>> duplicateIps) {
      _duplicateIps = duplicateIps;
    }
  }

  public static class UniqueIpAssignmentsAnswerer extends Answerer {

    public UniqueIpAssignmentsAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {

      UniqueIpAssignmentsQuestion question = (UniqueIpAssignmentsQuestion) _question;

      UniqueIpAssignmentsAnswerElement answerElement = new UniqueIpAssignmentsAnswerElement();
      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      Set<String> nodes = question.getNodeRegex().getMatchingNodes(configurations);
      MultiSet<Ip> duplicateIps = new TreeMultiSet<>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
        String hostname = e.getKey();
        if (!nodes.contains(hostname)) {
          continue;
        }
        Configuration c = e.getValue();
        for (Interface iface : c.getInterfaces().values()) {
          if (!question.getInterfacesSpecifier().matches(iface)) {
            continue;
          }
          for (InterfaceAddress address : iface.getAllAddresses()) {
            Ip ip = address.getIp();
            if (!question.getEnabledIpsOnly() || iface.getActive()) {
              duplicateIps.add(ip);
            }
          }
        }
      }
      for (Entry<String, Configuration> e : configurations.entrySet()) {
        String hostname = e.getKey();
        if (!nodes.contains(hostname)) {
          continue;
        }
        Configuration c = e.getValue();
        for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
          String interfaceName = e2.getKey();
          Interface iface = e2.getValue();
          if (!question.getInterfacesSpecifier().matches(iface)) {
            continue;
          }
          for (InterfaceAddress address : iface.getAllAddresses()) {
            Ip ip = address.getIp();
            if ((!question.getEnabledIpsOnly() || iface.getActive())
                && duplicateIps.count(ip) != 1) {
              answerElement.add(answerElement.getDuplicateIps(), ip, hostname, interfaceName);
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
   * @param interfacesSpecifier Specification for interfaces to consider. Default value is '.*' (all
   *     interfaces).
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("UniqueIpAssignments", nodeRegex='as2.*') Answers the question only for
   *     nodes whose names start with 'as2'.
   */
  public static class UniqueIpAssignmentsQuestion extends Question {

    private static final String PROP_ENABLED_IPS_ONLY = "enabledIpsOnly";

    private static final String PROP_INTERFACES_SPECIFIER = "interfacesSpecifier";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private boolean _enabledIpsOnly;

    private InterfacesSpecifier _interfacesSpecifier;

    private NodesSpecifier _nodeRegex;

    @JsonCreator
    public UniqueIpAssignmentsQuestion(
        @JsonProperty(PROP_ENABLED_IPS_ONLY) Boolean enabledIpsOnly,
        @JsonProperty(PROP_INTERFACES_SPECIFIER) InterfacesSpecifier interfacesSpecifier,
        @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodesSpecifier) {
      _enabledIpsOnly = enabledIpsOnly != null && enabledIpsOnly.booleanValue();
      _interfacesSpecifier =
          interfacesSpecifier == null ? InterfacesSpecifier.ALL : interfacesSpecifier;
      _nodeRegex = nodesSpecifier == null ? NodesSpecifier.ALL : nodesSpecifier;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "uniqueipassignments";
    }

    @JsonProperty(PROP_ENABLED_IPS_ONLY)
    public boolean getEnabledIpsOnly() {
      return _enabledIpsOnly;
    }

    @JsonProperty(PROP_INTERFACES_SPECIFIER)
    public InterfacesSpecifier getInterfacesSpecifier() {
      return _interfacesSpecifier;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @Override
    public String prettyPrint() {
      return String.format(
          "uniqueipassignments %senabledIpsOnly=%s, interfacesSpecifier=%s, nodeRegex=\"%s\"",
          prettyPrintBase(), _enabledIpsOnly, _interfacesSpecifier, _nodeRegex);
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new UniqueIpAssignmentsAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new UniqueIpAssignmentsQuestion(null, null, null);
  }
}
