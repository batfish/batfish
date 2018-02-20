package org.batfish.question;

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
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class UniqueBgpAsnQuestionPlugin extends QuestionPlugin {

  public static class UniqueBgpAsnAnswerElement implements AnswerElement {

    private SortedMap<Integer, SortedSet<String>> _duplicateAsns;

    public UniqueBgpAsnAnswerElement() {
      _duplicateAsns = new TreeMap<>();
    }

    public void add(Integer asn, SortedSet<String> nodes) {
      _duplicateAsns.put(asn, nodes);
    }

    public SortedMap<Integer, SortedSet<String>> getDuplicateAsns() {
      return _duplicateAsns;
    }

    @Override
    public String prettyPrint() {
      StringBuilder sb = new StringBuilder("Results for unique Bgp ASN check\n");
      if (_duplicateAsns != null) {
        for (Integer asn : _duplicateAsns.keySet()) {
          sb.append("  " + asn + "\n");
          for (String node : _duplicateAsns.get(asn)) {
            sb.append("    " + node + "\n");
          }
        }
      }
      return sb.toString();
    }
  }

  public static class UniqueBgpAsnAnswerer extends Answerer {

    public UniqueBgpAsnAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {

      UniqueBgpAsnQuestion question = (UniqueBgpAsnQuestion) _question;

      UniqueBgpAsnAnswerElement answerElement = new UniqueBgpAsnAnswerElement();
      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      Set<String> nodes = question.getNodeRegex().getMatchingNodes(configurations);
      SortedMap<Integer, SortedSet<String>> asns = new TreeMap<>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
        String hostname = e.getKey();
        if (!nodes.contains(hostname)) {
          continue;
        }
        Configuration c = e.getValue();
        for (Vrf vrf : c.getVrfs().values()) {
          BgpProcess bgpProc = vrf.getBgpProcess();
          if (bgpProc != null) {
            for (BgpNeighbor neighbor : bgpProc.getNeighbors().values()) {
              SortedSet<String> bgpNodes =
                  asns.computeIfAbsent(neighbor.getLocalAs(), k -> new TreeSet<>());
              bgpNodes.add(hostname);
            }
          }
        }
      }
      for (Entry<Integer, SortedSet<String>> e : asns.entrySet()) {
        SortedSet<String> bgpNodes = e.getValue();
        if (bgpNodes.size() > 1) {
          answerElement.add(e.getKey(), bgpNodes);
        }
      }
      return answerElement;
    }
  }

  // <question_page_comment>

  /**
   * Lists ASNs that are being used by multiple nodes
   *
   * <p>In eBGP-based data centers, it is often desired that each router have its own ASN.
   *
   * @type UniqueBgpAsn multifile
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("UniqueBgpAsns", nodeRegex='as2.*') Answers the question only for nodes
   *     whose names start with 'as2'.
   */
  public static class UniqueBgpAsnQuestion extends Question {

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private NodesSpecifier _nodeRegex;

    public UniqueBgpAsnQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "uniquebgpasn";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format("uniquebgpasn %snodeRegex=\"%s", prettyPrintBase(), _nodeRegex);
      return retString;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier nodeRegex) {
      _nodeRegex = nodeRegex;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new UniqueBgpAsnAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new UniqueBgpAsnQuestion();
  }
}
