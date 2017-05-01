package org.batfish.question;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

public class BgpAdvertisementsQuestionPlugin extends QuestionPlugin {

   @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
   public static class BgpAdvertisementsAnswerElement implements AnswerElement {

      private static final String ALL_REQUESTED_ADVERTISEMENTS_VAR = "allRequestedAdvertisements";

      private static final String RECEIVED_EBGP_ADVERTISEMENTS_VAR = "receivedEbgpAdvertisements";

      private static final String RECEIVED_IBGP_ADVERTISEMENTS_VAR = "receivedIbgpAdvertisements";

      private static final String SENT_EBGP_ADVERTISEMENTS_VAR = "sentEbgpAdvertisements";

      private static final String SENT_IBGP_ADVERTISEMENTS_VAR = "sentIbgpAdvertisements";

      private SortedSet<BgpAdvertisement> _allRequestedAdvertisements;

      private SortedMap<String, SortedSet<BgpAdvertisement>> _receivedEbgpAdvertisements;

      private SortedMap<String, SortedSet<BgpAdvertisement>> _receivedIbgpAdvertisements;

      private SortedMap<String, SortedSet<BgpAdvertisement>> _sentEbgpAdvertisements;

      private SortedMap<String, SortedSet<BgpAdvertisement>> _sentIbgpAdvertisements;

      @JsonCreator
      public BgpAdvertisementsAnswerElement() {
      }

      public BgpAdvertisementsAnswerElement(
            Map<String, Configuration> configurations, Pattern nodeRegex,
            boolean ebgp, boolean ibgp, PrefixSpace prefixSpace,
            boolean received, boolean sent) {
         _allRequestedAdvertisements = new TreeSet<>();
         _receivedEbgpAdvertisements = (received && ebgp) ? new TreeMap<>()
               : null;
         _sentEbgpAdvertisements = (sent && ebgp) ? new TreeMap<>() : null;
         _receivedIbgpAdvertisements = (received && ibgp) ? new TreeMap<>()
               : null;
         _sentIbgpAdvertisements = (sent && ibgp) ? new TreeMap<>() : null;
         for (Entry<String, Configuration> e : configurations.entrySet()) {
            String hostname = e.getKey();
            Matcher nodeMatcher = nodeRegex.matcher(hostname);
            if (!nodeMatcher.matches()) {
               continue;
            }
            Configuration configuration = e.getValue();
            if (received) {
               if (ebgp) {
                  Set<BgpAdvertisement> advertisements = configuration
                        .getReceivedEbgpAdvertisements();
                  fill(_receivedEbgpAdvertisements, hostname, advertisements,
                        prefixSpace);
               }
               if (ibgp) {
                  Set<BgpAdvertisement> advertisements = configuration
                        .getReceivedIbgpAdvertisements();
                  fill(_receivedIbgpAdvertisements, hostname, advertisements,
                        prefixSpace);
               }
            }
            if (sent) {
               if (ebgp) {
                  Set<BgpAdvertisement> advertisements = configuration
                        .getSentEbgpAdvertisements();
                  fill(_sentEbgpAdvertisements, hostname, advertisements,
                        prefixSpace);
               }
               if (ibgp) {
                  Set<BgpAdvertisement> advertisements = configuration
                        .getSentIbgpAdvertisements();
                  fill(_sentIbgpAdvertisements, hostname, advertisements,
                        prefixSpace);
               }
            }
         }
      }

      private void fill(Map<String, SortedSet<BgpAdvertisement>> map,
            String hostname, Set<BgpAdvertisement> advertisements,
            PrefixSpace prefixSpace) {
         SortedSet<BgpAdvertisement> placedAdvertisements = new TreeSet<>();
         map.put(hostname, placedAdvertisements);
         for (BgpAdvertisement advertisement : advertisements) {
            if (prefixSpace.isEmpty()
                  || prefixSpace.containsPrefix(advertisement.getNetwork())) {
               placedAdvertisements.add(advertisement);
               _allRequestedAdvertisements.add(advertisement);
            }
         }
      }

      @JsonProperty(ALL_REQUESTED_ADVERTISEMENTS_VAR)
      public SortedSet<BgpAdvertisement> getAllRequestedAdvertisements() {
         return _allRequestedAdvertisements;
      }

      @JsonIdentityReference(alwaysAsId = true)
      @JsonProperty(RECEIVED_EBGP_ADVERTISEMENTS_VAR)
      public SortedMap<String, SortedSet<BgpAdvertisement>> getReceivedEbgpAdvertisements() {
         return _receivedEbgpAdvertisements;
      }

      @JsonIdentityReference(alwaysAsId = true)
      @JsonProperty(RECEIVED_IBGP_ADVERTISEMENTS_VAR)
      public SortedMap<String, SortedSet<BgpAdvertisement>> getReceivedIbgpAdvertisements() {
         return _receivedIbgpAdvertisements;
      }

      @JsonIdentityReference(alwaysAsId = true)
      @JsonProperty(SENT_EBGP_ADVERTISEMENTS_VAR)
      public SortedMap<String, SortedSet<BgpAdvertisement>> getSentEbgpAdvertisements() {
         return _sentEbgpAdvertisements;
      }

      @JsonIdentityReference(alwaysAsId = true)
      @JsonProperty(SENT_IBGP_ADVERTISEMENTS_VAR)
      public SortedMap<String, SortedSet<BgpAdvertisement>> getSentIbgpAdvertisements() {
         return _sentIbgpAdvertisements;
      }

      @JsonProperty(ALL_REQUESTED_ADVERTISEMENTS_VAR)
      public void setAllRequestedAdvertisements(
            SortedSet<BgpAdvertisement> allRequestedAdvertisements) {
         _allRequestedAdvertisements = allRequestedAdvertisements;
      }

      @JsonProperty(RECEIVED_EBGP_ADVERTISEMENTS_VAR)
      public void setReceivedEbgpAdvertisements(
            SortedMap<String, SortedSet<BgpAdvertisement>> receivedEbgpAdvertisements) {
         _receivedEbgpAdvertisements = receivedEbgpAdvertisements;
      }

      @JsonProperty(RECEIVED_IBGP_ADVERTISEMENTS_VAR)
      public void setReceivedIbgpAdvertisements(
            SortedMap<String, SortedSet<BgpAdvertisement>> receivedIbgpAdvertisements) {
         _receivedIbgpAdvertisements = receivedIbgpAdvertisements;
      }

      @JsonProperty(SENT_EBGP_ADVERTISEMENTS_VAR)
      public void setSentEbgpAdvertisements(
            SortedMap<String, SortedSet<BgpAdvertisement>> sentEbgpAdvertisements) {
         _sentEbgpAdvertisements = sentEbgpAdvertisements;
      }

      @JsonProperty(SENT_IBGP_ADVERTISEMENTS_VAR)
      public void setSentIbgpAdvertisements(
            SortedMap<String, SortedSet<BgpAdvertisement>> sentIbgpAdvertisements) {
         _sentIbgpAdvertisements = sentIbgpAdvertisements;
      }

   }

   public static class BgpAdvertisementsAnswerer extends Answerer {

      public BgpAdvertisementsAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public AnswerElement answer() {
         BgpAdvertisementsQuestion question = (BgpAdvertisementsQuestion) _question;
         Pattern nodeRegex;
         try {
            nodeRegex = Pattern.compile(question.getNodeRegex());
         }
         catch (PatternSyntaxException e) {
            throw new BatfishException(
                  "Supplied regex for nodes is not a valid java regex: \""
                        + question.getNodeRegex() + "\"",
                  e);
         }
         _batfish.checkDataPlaneQuestionDependencies();
         Map<String, Configuration> configurations = _batfish
               .loadConfigurations();
         _batfish.initBgpAdvertisements(configurations);
         BgpAdvertisementsAnswerElement answerElement = new BgpAdvertisementsAnswerElement(
               configurations, nodeRegex, question.getEbgp(),
               question.getIbgp(), question.getPrefixSpace(),
               question.getReceived(), question.getSent());
         return answerElement;
      }

   }

   // <question_page_comment>
   /**
    * Lists BGP adverstisements in the network.
    * <p>
    * It can output all eBGP/iBGP advertisements that are exchanged in the
    * network.
    *
    * @type BgpAdvertisements dataplane
    *
    * @param ebgp
    *           (True|False) which indicates if eBGP advertisements should be
    *           included. Default is True.
    * @param ibgp
    *           (True|False) which indicates if iBGP advertisements should be
    *           included. Default is True.
    * @param nodeRegex
    *           Regular expression for names of nodes to include. Default value
    *           is '.*' (all nodes).
    * @param prefixRange
    *           Details coming. Default is entire range.
    * @param received
    *           (True|False) which indicates if received advertisements should
    *           be included. Default is True.
    * @param sent
    *           (True|False) which indicates if sent advertisements should be
    *           included. Default is True.
    *
    * @example bf_answer("BgpAdvertisements", sent='False', nodeRegex='as1.*')
    *          Lists all BGP advertisements received (not sent) by nodes whose
    *          names start with 'as1'.
    */
   public static class BgpAdvertisementsQuestion extends Question {

      private static final String EBGP_VAR = "ebgp";

      private static final String IBGP_VAR = "ibgp";

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private static final String PREFIX_SPACE_VAR = "prefixSpace";

      private static final String RECEIVED_VAR = "received";

      private static final String SENT_VAR = "sent";

      private boolean _ebgp;

      private boolean _ibgp;

      private String _nodeRegex;

      private PrefixSpace _prefixSpace;

      private boolean _received;

      private boolean _sent;

      public BgpAdvertisementsQuestion() {
         _nodeRegex = ".*";
         _ebgp = true;
         _ibgp = true;
         _nodeRegex = ".*";
         _received = true;
         _sent = true;
         _prefixSpace = new PrefixSpace();
      }

      @Override
      public boolean getDataPlane() {
         return true;
      }

      @JsonProperty(EBGP_VAR)
      public boolean getEbgp() {
         return _ebgp;
      }

      @JsonProperty(IBGP_VAR)
      public boolean getIbgp() {
         return _ibgp;
      }

      @Override
      public String getName() {
         return "bgpadvertisements";
      }

      @JsonProperty(NODE_REGEX_VAR)
      public String getNodeRegex() {
         return _nodeRegex;
      }

      @JsonProperty(PREFIX_SPACE_VAR)
      public PrefixSpace getPrefixSpace() {
         return _prefixSpace;
      }

      @JsonProperty(RECEIVED_VAR)
      public boolean getReceived() {
         return _received;
      }

      @JsonProperty(SENT_VAR)
      public boolean getSent() {
         return _sent;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() {
         String retString = String.format(
               "%s %s%s=\"%s\" %s=\"%s\" %s=\"%s\" %s=\"%s\" %s=\"%s\" %s=\"%s\"",
               getName(), prettyPrintBase(), EBGP_VAR, _ebgp, IBGP_VAR, _ibgp,
               NODE_REGEX_VAR, _nodeRegex, PREFIX_SPACE_VAR,
               _prefixSpace.toString(), RECEIVED_VAR, _received, SENT_VAR,
               _sent);
         return retString;
      }

      @JsonProperty(EBGP_VAR)
      public void setEbgp(boolean ebgp) {
         _ebgp = ebgp;
      }

      @JsonProperty(IBGP_VAR)
      public void setIbgp(boolean ibgp) {
         _ibgp = ibgp;
      }

      @JsonProperty(NODE_REGEX_VAR)
      public void setNodeRegex(String nodeRegex) {
         _nodeRegex = nodeRegex;
      }

      @JsonProperty(PREFIX_SPACE_VAR)
      private void setPrefixSpace(PrefixSpace prefixSpace) {
         _prefixSpace = prefixSpace;
      }

      @JsonProperty(RECEIVED_VAR)
      public void setReceived(boolean received) {
         _received = received;
      }

      @JsonProperty(SENT_VAR)
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
