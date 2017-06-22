package org.batfish.question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.Answerer;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.questions.Question;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InferRolesQuestionPlugin extends QuestionPlugin {

   public static class InferRolesAnswerElement implements AnswerElement {
      
      private List<Set<String>> _roles;

      private final String ROLES_VAR = "roles";

      public InferRolesAnswerElement() {
      }

      @JsonProperty(ROLES_VAR)
      public List<Set<String>> getRoles() {
         return _roles;
      }

      @Override
      public String prettyPrint() {
         StringBuilder sb = new StringBuilder(
               "Results for role inference\n");         
         for(Set<String> role : _roles) {
            sb.append(role.toString() + "\n");
         }
         return sb.toString();
      }

      @JsonProperty(ROLES_VAR)
      public void setRoles(List<Set<String>> roles) {
         _roles = roles;
      }
   }

   public static class InferRolesAnswerer extends Answerer {

      private InferRolesAnswerElement _answerElement;
      
      private List<String> _nodes;

      public InferRolesAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public InferRolesAnswerElement answer() {

         InferRolesQuestion question = (InferRolesQuestion) _question;
         _answerElement = new InferRolesAnswerElement();
         
         // first get the results of compareSameName
         CompareSameNameQuestionPlugin.CompareSameNameQuestion inner = 
               new CompareSameNameQuestionPlugin.CompareSameNameQuestion();
         inner.setNodeRegex(question.getNodeRegex());
         inner.setNamedStructTypes(question.getNamedStructTypes());
         CompareSameNameQuestionPlugin.CompareSameNameAnswerer innerAnswerer = 
               new CompareSameNameQuestionPlugin().createAnswerer(inner, _batfish);
         CompareSameNameQuestionPlugin.CompareSameNameAnswerElement innerAnswer = 
               innerAnswerer.answer();
         
         SortedMap<String, NamedStructureEquivalenceSets<?>> equivalenceSets = 
               innerAnswer.getEquivalenceSets();
         _nodes = innerAnswer.getNodes();
         
         // use the CompareSameName info to create a data vector for each node
         Map<String,Set<String>> dataVectors = createDataVectors(equivalenceSets);

         // now do k-modes clustering on this data
         List<Set<String>> clusters = kModes(question.getNumRoles(), dataVectors.keySet());
         
         List<Set<String>> roles = new ArrayList<>(clusters.size());
         for(Set<String> cluster : clusters) {
            Set<String> role = new TreeSet<>();
            for (String vector : cluster)
               role.addAll(dataVectors.get(vector));
            roles.add(role);
         }
         _answerElement.setRoles(roles);
         
         return _answerElement;
      }
      

      private <T> void addToDataVectors(NamedStructureEquivalenceSets<T> eSets, 
            Map<String, StringBuilder> vectors) {

         // append a 1 to the vector of each node that contains a structure of this name and a 0 for all other nodes
         
         for(StringBuilder sb : vectors.values())
            sb.append('0');
         
         for (Set<NamedStructureEquivalenceSet<T>> eSet : eSets.getSameNamedStructures().values()) {
            for (NamedStructureEquivalenceSet<T> eClass : eSet) {
               for (String node : eClass.getNodes()) {
                  StringBuilder sb = vectors.get(node);
                  sb.setCharAt(sb.length() - 1, '1');
               }
            }
         }
      }
      
      private Map<String,Set<String>> createDataVectors(SortedMap<String, NamedStructureEquivalenceSets<?>> equivalenceSets) {
         
         // TODO: Currently each data vector is a String of 0 and 1 characters; consider replacing with a BitSet for better efficiency.
         
         Map<String, StringBuilder> vectors = new TreeMap<>();
         for(String node : _nodes) {
            vectors.put(node, new StringBuilder());
         }
         for (NamedStructureEquivalenceSets<?> eSets : equivalenceSets.values()) {
            addToDataVectors(eSets, vectors);
         }
         
         // invert vecs to produce a mapping from vectors to the nodes that have that vector
         Map<String,Set<String>> invertedVectors = new TreeMap<>();
         for (Map.Entry<String,StringBuilder> entry : vectors.entrySet()) {
            String vector = entry.getValue().toString();
            Set<String> nodes = invertedVectors.get(vector);
            if (nodes != null)
               nodes.add(entry.getKey());
            else {
               nodes = new TreeSet<String>();
               nodes.add(entry.getKey());
               invertedVectors.put(vector, nodes);
            }
         }
         return invertedVectors;
      }
      
      private int hammingDistance(String s1, String s2) {
         // we assume the strings have the same length
         int dist = 0;
         for(int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) != s2.charAt(i))
               dist++;
         }
         return dist;
      }
      
      // find the element in choice[0..(i-1)] with the minimum hamming distance to s
      // return a pair of the index and hamming distance
      private Pair<Integer,Integer> minHammingDistance(String s, String[] choices, int len) {
         int min = Integer.MAX_VALUE;
         int minIndex = -1;
         for(int i = 0; i < len; i++) {
            int dist = hammingDistance(s, choices[i]);
            if(dist < min) {
               min = dist;
               minIndex = i;
            }
         }
         return new Pair<Integer,Integer>(minIndex, min);
      }
      
      // produce a new vector whose ith element is the mode of the ith elements of the given vectors
      private String elementwiseMode(Set<String> vectors, int strLen) {
         StringBuilder sb = new StringBuilder();
         for(int i = 0; i < strLen; i++) {
            char mode = 'a';
            int modeCount = 0;
            for(String vec : vectors) {
               char c = vec.charAt(i);
               int count = 0;
               for(String vec2 : vectors) {
                  if(c == vec2.charAt(i))
                     count++;      
               }
               if (count > modeCount) {
                  modeCount = count;
                  mode = c;
               }
            }
            sb.append(mode);
         }
         return sb.toString();
      }
      
      // standard k-modes clustering, with the seeding technique from the k-means++ algorithm
      private List<Set<String>> kModes(int k, Set<String> vectors) {
         
         int vecLen = 0;
         for(String vector : vectors) {
            vecLen = vector.length();
            break;
         }
         
         String[] centers = new String[k];
         List<Set<String>> clusters = null;
         
         Random rand = new Random();         
         String[] vecArray = vectors.toArray(new String[vectors.size()]);
         int numVecs = vecArray.length;
         
         // use the k-means++ seeding algorithm:
         // choose the first center randomly
         centers[0] = vecArray[rand.nextInt(numVecs)];
         for (int c = 1; c < k; c++) {
            // for each vector, find its minimal distance to any already-chosen center, and square it
            int[] minDistancesSquared = new int[numVecs];
            int sumOfSquares = 0;
            for(int i = 0; i < numVecs; i++) {
               int minDist = minHammingDistance(vecArray[i], centers, c).getSecond();
               int minDistSquared = minDist * minDist;
               sumOfSquares += minDistSquared;
               minDistancesSquared[i] = minDistSquared;
            }
            // randomly choose the next center, with the probability of choosing vector i being
            // minDistancesSquared[i] / sumOfSquares
            double target = rand.nextDouble() * sumOfSquares;
            for(int i = 0; i < numVecs; i++) {
               target -= minDistancesSquared[i];
               if (target <= 0.0) {
                  centers[c] = vecArray[i];
                  break;
               }
            }
            
         }
         
         boolean done = false;
         
         while(!done) {
         
            // compute the cluster of each vector: 
            // the cluster whose center has the minimum hamming distance to the vector
            clusters = new ArrayList<>(k);
            for(int i = 0; i < k; i++)
               clusters.add(new TreeSet<>());
            for (String vector : vectors) {
               int cluster = minHammingDistance(vector, centers, centers.length).getFirst();
               Set<String> clusterSet = clusters.get(cluster);
               clusterSet.add(vector);
            }
         
            // compute a new center for each cluster:
            // the vector containing the mode value for each element
            String[] newCenters = new String[k];
            int c = 0;
            for (Set<String> cluster : clusters) {
               newCenters[c] = elementwiseMode(cluster, vecLen);
               c++;
            }
            
            done = Arrays.equals(centers, newCenters);
            centers = newCenters;
         }
         return clusters;         
      }
   }

   // <question_page_comment>
   /**
    * Uses a form of clustering to partition nodes into sets of roles.
    * <p>
    * It is common for the nodes in a network to be partitioned into roles that
    * each have a specific function in the network.  For example, border routers
    * are responsible for mediating the interactions between the network and its 
    * peer networks, and distribution routers are responsible for delivering packets
    * to end hosts within the network.  Role information can be useful for improving
    * the precision and utility of several other questions.  For example, it may only
    * make sense to run the CompareSameName question on nodes that have the same role.
    *
    * @type InferRoles multifile
    *
    * @param namedStructTypes
    *           Set of structure types to analyze drawn from ( AsPathAccessList,
    *           CommunityList, IkeGateway, IkePolicies, IkeProposal,
    *           IpAccessList, IpsecPolicy, IpsecProposal, IpsecVpn,
    *           RouteFilterList, RoutingPolicy) Default value is '[]' (which
    *           denotes all structure types).
    * @param nodeRegex
    *           Regular expression for names of nodes to include. Default value
    *           is '.*' (all nodes).
    * @param numRoles
    *           The number of roles to cluster nodes into.  Default value is 5.
    *
    */
   public static final class InferRolesQuestion extends Question {

      private static final String NAMED_STRUCT_TYPES_VAR = "namedStructTypes";

      private static final String NODE_REGEX_VAR = "nodeRegex";
      
      private static final String NUM_ROLES_VAR = "numRoles";

      private SortedSet<String> _namedStructTypes;

      private String _nodeRegex;
      
      private int _numRoles;

      public InferRolesQuestion() {
         _namedStructTypes = new TreeSet<>();
         _nodeRegex = ".*";
         _numRoles = 5;
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "inferroles";
      }

      @JsonProperty(NAMED_STRUCT_TYPES_VAR)
      public SortedSet<String> getNamedStructTypes() {
         return _namedStructTypes;
      }

      @JsonProperty(NODE_REGEX_VAR)
      public String getNodeRegex() {
         return _nodeRegex;
      }
      
      @JsonProperty(NUM_ROLES_VAR)
      public int getNumRoles() {
         return _numRoles;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @JsonProperty(NAMED_STRUCT_TYPES_VAR)
      public void setNamedStructTypes(SortedSet<String> namedStructTypes) {
         _namedStructTypes = namedStructTypes;
      }

      @JsonProperty(NODE_REGEX_VAR)
      public void setNodeRegex(String regex) {
         _nodeRegex = regex;
      }
      
      @JsonProperty(NUM_ROLES_VAR)
      public void setNumRoles(int numRoles) {
         _numRoles = numRoles;
      }

   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new InferRolesAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new InferRolesQuestion();
   }

}
