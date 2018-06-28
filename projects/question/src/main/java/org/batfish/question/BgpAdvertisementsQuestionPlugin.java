package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.HashSet;
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
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class BgpAdvertisementsQuestionPlugin extends QuestionPlugin {

  public static class BgpAdvertisementsAnswerElement extends AnswerElement {

    private static final String PROP_BGP_ADVERTISEMENTS = "bgpAdvertisements";

    private SortedMap<String, SortedSet<BgpAdvertisement>> _bgpAdvertisements;

    @JsonCreator
    private BgpAdvertisementsAnswerElement(
        @JsonProperty(PROP_BGP_ADVERTISEMENTS)
            SortedMap<String, SortedSet<BgpAdvertisement>> bgpAdvertisements) {
      _bgpAdvertisements = makeImmutable(bgpAdvertisements);
    }

    public BgpAdvertisementsAnswerElement(
        Set<BgpAdvertisement> externalAdverts,
        Map<String, Configuration> configurations,
        Set<String> includeNodes,
        PrefixSpace prefixSpace) {
      SortedMap<String, SortedSet<BgpAdvertisement>> bgpAdvertisements = new TreeMap<>();
      Set<String> allowedHostnames = new HashSet<>();
      for (String hostname : configurations.keySet()) {
        if (includeNodes.contains(hostname)) {
          allowedHostnames.add(hostname);
          bgpAdvertisements.put(hostname, new TreeSet<>());
        }
      }
      for (BgpAdvertisement advertisement : externalAdverts) {
        String hostname = advertisement.getDstNode();
        if (allowedHostnames.contains(hostname)
            && (prefixSpace.isEmpty() || prefixSpace.containsPrefix(advertisement.getNetwork()))) {
          bgpAdvertisements.get(hostname).add(advertisement);
        }
      }
      _bgpAdvertisements = makeImmutable(bgpAdvertisements);
    }

    public BgpAdvertisementsAnswerElement(
        Map<String, Configuration> configurations,
        Set<String> includeNodes,
        boolean ebgp,
        boolean ibgp,
        PrefixSpace prefixSpace,
        boolean received,
        boolean sent) {
      SortedMap<String, SortedSet<BgpAdvertisement>> bgpAdvertisements = new TreeMap<>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
        String hostname = e.getKey();
        if (!includeNodes.contains(hostname)) {
          continue;
        }
        Configuration configuration = e.getValue();
        if (received) {
          if (ebgp) {
            Set<BgpAdvertisement> advertisements = configuration.getReceivedEbgpAdvertisements();
            fill(bgpAdvertisements, hostname, advertisements, prefixSpace);
          }
          if (ibgp) {
            Set<BgpAdvertisement> advertisements = configuration.getReceivedIbgpAdvertisements();
            fill(bgpAdvertisements, hostname, advertisements, prefixSpace);
          }
        }
        if (sent) {
          if (ebgp) {
            Set<BgpAdvertisement> advertisements = configuration.getSentEbgpAdvertisements();
            fill(bgpAdvertisements, hostname, advertisements, prefixSpace);
          }
          if (ibgp) {
            Set<BgpAdvertisement> advertisements = configuration.getSentIbgpAdvertisements();
            fill(bgpAdvertisements, hostname, advertisements, prefixSpace);
          }
        }
      }
      _bgpAdvertisements = makeImmutable(bgpAdvertisements);
    }

    private void fill(
        SortedMap<String, SortedSet<BgpAdvertisement>> bgpAdvertisements,
        String hostname,
        Set<BgpAdvertisement> advertisements,
        PrefixSpace prefixSpace) {
      SortedSet<BgpAdvertisement> placedAdvertisements =
          bgpAdvertisements.computeIfAbsent(hostname, k -> new TreeSet<>());
      for (BgpAdvertisement advertisement : advertisements) {
        if (prefixSpace.isEmpty() || prefixSpace.containsPrefix(advertisement.getNetwork())) {
          placedAdvertisements.add(advertisement);
        }
      }
    }

    @JsonProperty(PROP_BGP_ADVERTISEMENTS)
    public SortedMap<String, SortedSet<BgpAdvertisement>> getReceivedEbgpAdvertisements() {
      return _bgpAdvertisements;
    }

    private SortedMap<String, SortedSet<BgpAdvertisement>> makeImmutable(
        SortedMap<String, SortedSet<BgpAdvertisement>> bgpAdvertisements) {
      return bgpAdvertisements
          .entrySet()
          .stream()
          .collect(
              ImmutableSortedMap.toImmutableSortedMap(
                  Ordering.natural(), Entry::getKey, e -> ImmutableSortedSet.copyOf(e.getValue())));
    }

    @Override
    public String prettyPrint() {
      StringBuilder sb = new StringBuilder();
      _bgpAdvertisements.forEach(
          (hostname, adverts) ->
              adverts.forEach(advert -> sb.append(advert.prettyPrint(hostname + " "))));
      return sb.toString();
    }
  }

  public static class BgpAdvertisementsAnswerer extends Answerer {

    public BgpAdvertisementsAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public BgpAdvertisementsAnswerElement answer() {
      BgpAdvertisementsQuestion question = (BgpAdvertisementsQuestion) _question;
      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(_batfish);
      BgpAdvertisementsAnswerElement answerElement;
      if (question._fromEnvironment) {
        Set<BgpAdvertisement> externalAdverts =
            _batfish.loadExternalBgpAnnouncements(configurations);
        answerElement =
            new BgpAdvertisementsAnswerElement(
                externalAdverts, configurations, includeNodes, question.getPrefixSpace());
      } else {
        _batfish.initBgpAdvertisements(configurations);
        answerElement =
            new BgpAdvertisementsAnswerElement(
                configurations,
                includeNodes,
                question.getEbgp(),
                question.getIbgp(),
                question.getPrefixSpace(),
                question.getReceived(),
                question.getSent());
      }
      return answerElement;
    }
  }

  // <question_page_comment>

  /**
   * Lists BGP adverstisements in the network.
   *
   * <p>It can output all eBGP/iBGP advertisements that are exchanged in the network.
   *
   * @type BgpAdvertisements dataplane
   * @param ebgp (True|False) which indicates if eBGP advertisements should be included. Default is
   *     True.
   * @param ibgp (True|False) which indicates if iBGP advertisements should be included. Default is
   *     True.
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @param prefixRange Details coming. Default is entire range.
   * @param received (True|False) which indicates if received advertisements should be included.
   *     Default is True.
   * @param sent (True|False) which indicates if sent advertisements should be included. Default is
   *     True.
   * @example bf_answer("BgpAdvertisements", sent='False', nodeRegex='as1.*') Lists all BGP
   *     advertisements received (not sent) by nodes whose names start with 'as1'.
   */
  public static class BgpAdvertisementsQuestion extends Question {

    private static final String PROP_EBGP = "ebgp";

    private static final String PROP_FROM_ENVIRONMENT = "fromEnvironment";

    private static final String PROP_IBGP = "ibgp";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private static final String PROP_PREFIX_SPACE = "prefixSpace";

    private static final String PROP_RECEIVED = "received";

    private static final String PROP_SENT = "sent";

    private boolean _ebgp;

    private boolean _fromEnvironment;

    private boolean _ibgp;

    private NodesSpecifier _nodeRegex;

    private PrefixSpace _prefixSpace;

    private boolean _received;

    private boolean _sent;

    public BgpAdvertisementsQuestion() {
      _ebgp = true;
      _ibgp = true;
      _nodeRegex = NodesSpecifier.ALL;
      _received = true;
      _sent = true;
      _prefixSpace = new PrefixSpace();
    }

    @Override
    public boolean getDataPlane() {
      return true;
    }

    @JsonProperty(PROP_EBGP)
    public boolean getEbgp() {
      return _ebgp;
    }

    @JsonProperty(PROP_FROM_ENVIRONMENT)
    public boolean getFromEnvironment() {
      return _fromEnvironment;
    }

    @JsonProperty(PROP_IBGP)
    public boolean getIbgp() {
      return _ibgp;
    }

    @Override
    public String getName() {
      return "bgpadvertisements";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_PREFIX_SPACE)
    public PrefixSpace getPrefixSpace() {
      return _prefixSpace;
    }

    @JsonProperty(PROP_RECEIVED)
    public boolean getReceived() {
      return _received;
    }

    @JsonProperty(PROP_SENT)
    public boolean getSent() {
      return _sent;
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format(
              "%s %s%s=%s %s=%s %s=\"%s\" %s=%s %s=%s %s=%s",
              getName(),
              prettyPrintBase(),
              PROP_EBGP,
              _ebgp,
              PROP_IBGP,
              _ibgp,
              PROP_NODE_REGEX,
              _nodeRegex,
              PROP_PREFIX_SPACE,
              _prefixSpace.toString(),
              PROP_RECEIVED,
              _received,
              PROP_SENT,
              _sent);
      return retString;
    }

    @JsonProperty(PROP_EBGP)
    public void setEbgp(boolean ebgp) {
      _ebgp = ebgp;
    }

    @JsonProperty(PROP_FROM_ENVIRONMENT)
    public void setFromEnvironment(boolean fromEnvironment) {
      _fromEnvironment = fromEnvironment;
    }

    @JsonProperty(PROP_IBGP)
    public void setIbgp(boolean ibgp) {
      _ibgp = ibgp;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier nodeRegex) {
      _nodeRegex = nodeRegex;
    }

    @JsonProperty(PROP_PREFIX_SPACE)
    private void setPrefixSpace(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
    }

    @JsonProperty(PROP_RECEIVED)
    public void setReceived(boolean received) {
      _received = received;
    }

    @JsonProperty(PROP_SENT)
    public void setSent(boolean sent) {
      _sent = sent;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new BgpAdvertisementsAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new BgpAdvertisementsQuestion();
  }
}
