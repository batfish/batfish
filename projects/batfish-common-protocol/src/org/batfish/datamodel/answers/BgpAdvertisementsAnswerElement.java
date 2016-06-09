package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class BgpAdvertisementsAnswerElement implements AnswerElement {

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
         boolean ebgp, boolean ibgp, PrefixSpace prefixSpace, boolean received,
         boolean sent) {
      _allRequestedAdvertisements = new TreeSet<BgpAdvertisement>();
      _receivedEbgpAdvertisements = (received && ebgp) ? new TreeMap<String, SortedSet<BgpAdvertisement>>()
            : null;
      _sentEbgpAdvertisements = (sent && ebgp) ? new TreeMap<String, SortedSet<BgpAdvertisement>>()
            : null;
      _receivedIbgpAdvertisements = (received && ibgp) ? new TreeMap<String, SortedSet<BgpAdvertisement>>()
            : null;
      _sentIbgpAdvertisements = (sent && ibgp) ? new TreeMap<String, SortedSet<BgpAdvertisement>>()
            : null;
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
      SortedSet<BgpAdvertisement> placedAdvertisements = new TreeSet<BgpAdvertisement>();
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
