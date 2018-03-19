package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
public class BgpAsnUseQuestionPlugin extends QuestionPlugin {

  public static class BgpAsnUseAnswerElement extends AnswerElement {

    public static final String PROP_ASNS = "asns";

    private SortedSetMultimap<Integer, String> _asns;

    public BgpAsnUseAnswerElement(@JsonProperty(PROP_ASNS) TreeMultimap<Integer, String> asns) {
      _asns = (asns == null) ? TreeMultimap.create() : asns;
    }

    @JsonProperty(PROP_ASNS)
    public SortedSetMultimap<Integer, String> getAsns() {
      return _asns;
    }

    @Override
    public String prettyPrint() {
      StringBuilder sb = new StringBuilder("Results for BGP ASN use\n");
      if (_asns != null) {
        for (Integer asn : _asns.keySet()) {
          sb.append("  " + asn + "\n");
          for (String node : _asns.get(asn)) {
            sb.append("    " + node + "\n");
          }
        }
      }
      return sb.toString();
    }
  }

  public static class BgpAsnUseAnswerer extends Answerer {

    public BgpAsnUseAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {

      BgpAsnUseQuestion question = (BgpAsnUseQuestion) _question;

      BgpAsnUseAnswerElement answerElement = new BgpAsnUseAnswerElement(null);
      SortedSetMultimap<Integer, String> asns = TreeMultimap.create();
      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      Set<String> nodes = question.getNodeRegex().getMatchingNodes(configurations);
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
              asns.put(neighbor.getLocalAs(), hostname);
            }
          }
        }
      }
      // is there streams way of multimap to multimap conversion with a filter?
      for (Integer asn : asns.keySet()) {
        if (asns.get(asn).size() >= question.getMinCount()) {
          answerElement.getAsns().putAll(asn, asns.get(asn));
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
   * @type BgpAsnUse multifile
   * @param minCount Only report ASNs that are used at least this number of nodes
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("BgpAsnUse", nodeRegex='as2.*') Answers the question only for nodes whose
   *     names start with 'as2'.
   */
  public static class BgpAsnUseQuestion extends Question {

    private static final String PROP_MIN_COUNT = "minCount";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private Integer _minCount;

    private NodesSpecifier _nodeRegex;

    public BgpAsnUseQuestion(
        @JsonProperty(PROP_MIN_COUNT) Integer minCount,
        @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex) {
      _minCount = minCount == null ? 0 : minCount;
      _nodeRegex = nodeRegex == null ? NodesSpecifier.ALL : nodeRegex;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "bgpasnuse";
    }

    @JsonProperty(PROP_MIN_COUNT)
    public int getMinCount() {
      return _minCount;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format(
              "bgpasnuse %snodeRegex='%s' minCount=%d", prettyPrintBase(), _nodeRegex, _minCount);
      return retString;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new BgpAsnUseAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new BgpAsnUseQuestion(null, null);
  }
}
