package org.batfish.answerer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.CompareSameNameAnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.questions.CompareSameNameQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.main.Batfish;
import org.batfish.main.Settings.TestrigSettings;

public class CompareSameNameAnswerer extends Answerer {

   private CompareSameNameAnswerElement _answerElement;

   private Map<String, Configuration> _configurations;

   private List<String> _nodes;

   public CompareSameNameAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   private <T> void add(Class<T> structureClass,
         Function<Configuration, Map<String, T>> structureMapRetriever) {
      _answerElement.add(
            structureClass.getSimpleName(),
            processStructures(structureClass, _nodes, _configurations,
                  structureMapRetriever));
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {

      CompareSameNameQuestion question = (CompareSameNameQuestion) _question;
      _batfish.checkConfigurations(testrigSettings);
      _configurations = _batfish.loadConfigurations(testrigSettings);
      _answerElement = new CompareSameNameAnswerElement();
      // collect relevant nodes in a list.
      _nodes = CommonUtil.getMatchingStrings(question.getNodeRegex(),
            _configurations.keySet());

      add(AsPathAccessList.class, c -> c.getAsPathAccessLists());
      add(CommunityList.class, c -> c.getCommunityLists());
      add(IkeGateway.class, c -> c.getIkeGateways());
      add(RouteFilterList.class, c -> c.getRouteFilterLists());

      return _answerElement;
   }

   private <T> NamedStructureEquivalenceSets<T> processStructures(
         Class<T> structureClass, List<String> hostnames,
         Map<String, Configuration> configurations,
         Function<Configuration, Map<String, T>> structureMapRetriever) {
      NamedStructureEquivalenceSets<T> ae = new NamedStructureEquivalenceSets<T>(
            structureClass.getSimpleName());
      for (String hostname : hostnames) {
         // Process route filters
         Configuration node = configurations.get(hostname);
         Map<String, T> structureMap = structureMapRetriever.apply(node);
         for (String listName : structureMap.keySet()) {
            ae.add(hostname, listName, structureMap.get(listName));
         }
      }
      ae.clean();
      return ae;
   }

}
