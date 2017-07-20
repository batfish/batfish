package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.IkePolicy;
import org.batfish.datamodel.IkeProposal;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecPolicy;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.questions.INodeRegexQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class CompareSameNameQuestionPlugin extends QuestionPlugin {

   public static class CompareSameNameAnswerElement implements AnswerElement {

      /**
       * Equivalence sets are keyed by classname
       */
      private SortedMap<String, NamedStructureEquivalenceSets<?>> _equivalenceSets;
      
      private List<String> _nodes;

      private final String EQUIVALENCE_SETS_MAP_VAR = "equivalenceSetsMap";
      
      private final String NODES_VAR = "nodes";

      public CompareSameNameAnswerElement() {
         _equivalenceSets = new TreeMap<>();
      }

      public void add(String className, NamedStructureEquivalenceSets<?> sets) {
         _equivalenceSets.put(className, sets);
      }

      private String equivalenceSetToString(
            String indent, String name,
            NamedStructureEquivalenceSets<?> nseSets) {
         StringBuilder sb = new StringBuilder(indent + name + "\n");
         sb.append(nseSets.prettyPrint(indent + indent));
         return sb.toString();
      }

      @JsonProperty(EQUIVALENCE_SETS_MAP_VAR)
      public SortedMap<String, NamedStructureEquivalenceSets<?>> getEquivalenceSets() {
         return _equivalenceSets;
      }
      
      @JsonProperty(NODES_VAR)
      public List<String> getNodes() {
         return _nodes;
      }

      @Override
      public String prettyPrint() {
         StringBuilder sb = new StringBuilder(
               "Results for comparing same name structure\n");
         for (String name : _equivalenceSets.keySet()) {
            if (_equivalenceSets.get(name).size() > 0) {
               sb.append(equivalenceSetToString("  ", name,
                     _equivalenceSets.get(name)));
            }
         }
         return sb.toString();
      }

      @JsonProperty(EQUIVALENCE_SETS_MAP_VAR)
      public void setEquivalenceSets(
            SortedMap<String, NamedStructureEquivalenceSets<?>> equivalenceSets) {
         _equivalenceSets = equivalenceSets;
      }
      
      @JsonProperty(NODES_VAR)
      public void setNodes(List<String> nodes) {
         _nodes = nodes;
      }
   }

   public static class CompareSameNameAnswerer extends Answerer {

      private CompareSameNameAnswerElement _answerElement;

      private Map<String, Configuration> _configurations;

      private Set<String> _namedStructTypes;

      private List<String> _nodes;

      private boolean _singletons;
      
      private boolean _missing;

      public CompareSameNameAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      private <T> void add(
            Class<T> structureClass,
            Function<Configuration, Map<String, T>> structureMapRetriever) {
         if (_namedStructTypes.isEmpty() || _namedStructTypes
               .contains(structureClass.getSimpleName().toLowerCase())) {
            _answerElement.add(
                  structureClass.getSimpleName(),
                  processStructures(structureClass, _nodes, _configurations,
                        structureMapRetriever));
         }
      }

      @Override
      public CompareSameNameAnswerElement answer() {

         CompareSameNameQuestion question = (CompareSameNameQuestion) _question;
         _batfish.checkConfigurations();
         _configurations = _batfish.loadConfigurations();
         // collect relevant nodes in a list.
         _nodes = CommonUtil.getMatchingStrings(
               question.getNodeRegex(),
               _configurations.keySet());
         _namedStructTypes = question.getNamedStructTypes().stream()
               .map(s -> s.toLowerCase()).collect(Collectors.toSet());
         _singletons = question.getSingletons();
         _missing = question.getMissing();
         
         _answerElement = new CompareSameNameAnswerElement();
         _answerElement.setNodes(_nodes);

         add(AsPathAccessList.class, c -> c.getAsPathAccessLists());
         add(CommunityList.class, c -> c.getCommunityLists());
         add(IkeGateway.class, c -> c.getIkeGateways());
         add(IkePolicy.class, c -> c.getIkePolicies());
         add(IkeProposal.class, c -> c.getIkeProposals());
         add(Interface.class, c -> c.getInterfaces());
         add(Ip6AccessList.class, c -> c.getIp6AccessLists());
         add(IpAccessList.class, c -> c.getIpAccessLists());
         add(IpsecPolicy.class, c -> c.getIpsecPolicies());
         add(IpsecProposal.class, c -> c.getIpsecProposals());
         add(IpsecVpn.class, c -> c.getIpsecVpns());
         add(Route6FilterList.class, c -> c.getRoute6FilterLists());
         add(RouteFilterList.class, c -> c.getRouteFilterLists());
         add(RoutingPolicy.class, c -> c.getRoutingPolicies());
         add(Vrf.class, c -> c.getVrfs());
         add(Zone.class, c -> c.getZones());

         return _answerElement;
      }

      private <T> NamedStructureEquivalenceSets<T> processStructures(
            Class<T> structureClass, List<String> hostnames,
            Map<String, Configuration> configurations,
            Function<Configuration, Map<String, T>> structureMapRetriever) {
         NamedStructureEquivalenceSets<T> ae = new NamedStructureEquivalenceSets<>(
               structureClass.getSimpleName());
         // collect the set of all names for structures of type T, across all nodes
         Set<String> allNames = new TreeSet<String>();
         for (String hostname : hostnames) {
            Configuration node = configurations.get(hostname);
            Map<String,T> structureMap = structureMapRetriever.apply(node);
            allNames.addAll(structureMap.keySet());
         }
         for (String hostname : hostnames) {
            Configuration node = configurations.get(hostname);
            Map<String, T> structureMap = structureMapRetriever.apply(node);            
            for (String structName : allNames) {
               if (structName.startsWith("~")) {
                  continue;
               }
               T struct = structureMap.get(structName);
               if (struct != null || _missing)
                  ae.add(hostname, structName, struct);
            }
         }
         if (!_singletons) {
            ae.clean();
         }
         return ae;
      }

   }

   // <question_page_comment>

   /**
    * Compares named structures with identical names across multiple nodes.
    * <p>
    * Named structures refer to constructs like route-maps and access-control
    * lists. Often, identical functionality is needed on multiple routers and it
    * is common to have the same name for those structures across routers. We
    * compare the contents of structures with the same name across different
    * routers. When the contents of a same-named structure differ across
    * routers, it usually indicates a configuration error. For instance, if the
    * ACL named ``\verb|block_non_http_ssh|'' has identical content on nine out
    * of ten routers, but is different in the tenth router, the ACL is likely
    * misconfigured on the tenth router.
    *
    * @type CompareSameName multifile
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
    *
    * @param singletons
    *           Defaults to false. Specifies whether or not to include named
    *           structures for which there is only one equivalence class.
    *
    * @param missing
    *           Defaults to false. Specifies whether or not to create an equivalence
    *           class for nodes that are missing a structure of a given name.
    * 
    */
   public static final class CompareSameNameQuestion extends Question implements INodeRegexQuestion {

      private static final String NAMED_STRUCT_TYPES_VAR = "namedStructTypes";

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private static final String SINGLETONS_VAR = "singletons";
      
      private static final String MISSING_VAR = "missing";

      private SortedSet<String> _namedStructTypes;

      private String _nodeRegex;

      private boolean _singletons;
      
      private boolean _missing;

      public CompareSameNameQuestion() {
         _namedStructTypes = new TreeSet<>();
         _nodeRegex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "comparesamename";
      }

      @JsonProperty(NAMED_STRUCT_TYPES_VAR)
      public SortedSet<String> getNamedStructTypes() {
         return _namedStructTypes;
      }

      @JsonProperty(NODE_REGEX_VAR)
      public String getNodeRegex() {
         return _nodeRegex;
      }

      @JsonProperty(SINGLETONS_VAR)
      public boolean getSingletons() {
         return _singletons;
      }
      
      @JsonProperty(MISSING_VAR)
      public boolean getMissing() {
         return _missing;
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

      @JsonProperty(SINGLETONS_VAR)
      public void setSingletons(boolean singletons) {
         _singletons = singletons;
      }
      
      @JsonProperty(MISSING_VAR)
      public void setMissing(boolean missing) {
         _missing = missing;
      }

   }

   @Override
   protected CompareSameNameAnswerer createAnswerer(Question question, IBatfish batfish) {
      return new CompareSameNameAnswerer(question, batfish);
   }

   @Override
   protected CompareSameNameQuestion createQuestion() {
      return new CompareSameNameQuestion();
   }

}
