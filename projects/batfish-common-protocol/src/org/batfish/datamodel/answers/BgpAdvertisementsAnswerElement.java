package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Configuration;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class BgpAdvertisementsAnswerElement implements AnswerElement {

   private final Set<BgpAdvertisement> _allRequestedAdvertisements;

   private final Map<String, Set<BgpAdvertisement>> _receivedEbgpAdvertisements;

   private final Map<String, Set<BgpAdvertisement>> _receivedIbgpAdvertisements;

   private final Map<String, Set<BgpAdvertisement>> _sentEbgpAdvertisements;

   private final Map<String, Set<BgpAdvertisement>> _sentIbgpAdvertisements;

   public BgpAdvertisementsAnswerElement(
         Map<String, Configuration> configurations, Pattern nodeRegex, 
         boolean ebgp, boolean ibgp, boolean received, boolean sent) {
      _allRequestedAdvertisements = new TreeSet<BgpAdvertisement>();
      _receivedEbgpAdvertisements = new TreeMap<String, Set<BgpAdvertisement>>();
      _sentEbgpAdvertisements = new TreeMap<String, Set<BgpAdvertisement>>();
      _receivedIbgpAdvertisements = new TreeMap<String, Set<BgpAdvertisement>>();
      _sentIbgpAdvertisements = new TreeMap<String, Set<BgpAdvertisement>>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         Matcher nodeMatcher = nodeRegex.matcher(hostname);
         if (!nodeMatcher.matches()) {
            break;
         }
         Configuration configuration = e.getValue();
         if (received) {
            if (ebgp) {
               Set<BgpAdvertisement> advertisements = configuration
                     .getReceivedEbgpAdvertisements();
               _receivedEbgpAdvertisements.put(hostname, advertisements);
               _allRequestedAdvertisements.addAll(advertisements);
            }
            if (ibgp) {
               Set<BgpAdvertisement> advertisements = configuration
                     .getReceivedIbgpAdvertisements();
               _receivedIbgpAdvertisements.put(hostname, advertisements);
               _allRequestedAdvertisements.addAll(advertisements);
            }
         }
         if (sent) {
            if (ebgp) {
               Set<BgpAdvertisement> advertisements = configuration
                     .getSentEbgpAdvertisements();
               _sentEbgpAdvertisements.put(hostname, advertisements);
               _allRequestedAdvertisements.addAll(advertisements);
            }
            if (ibgp) {
               Set<BgpAdvertisement> advertisements = configuration
                     .getSentIbgpAdvertisements();
               _sentIbgpAdvertisements.put(hostname, advertisements);
               _allRequestedAdvertisements.addAll(advertisements);
            }
         }
      }
   }

   public Set<BgpAdvertisement> getAllRequestedAdvertisements() {
      return _allRequestedAdvertisements;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Set<BgpAdvertisement>> getReceivedEbgpAdvertisements() {
      return _receivedEbgpAdvertisements;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Set<BgpAdvertisement>> getReceivedIbgpAdvertisements() {
      return _receivedIbgpAdvertisements;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Set<BgpAdvertisement>> getSentEbgpAdvertisements() {
      return _sentEbgpAdvertisements;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Set<BgpAdvertisement>> getSentIbgpAdvertisements() {
      return _sentIbgpAdvertisements;
   }

}
