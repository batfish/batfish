package org.batfish.answerer;

import java.util.List;
import java.util.Map;

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

   public CompareSameNameAnswerer(Question question, Batfish batfish) {
      super(question, batfish);
   }

   @Override
   public AnswerElement answer(TestrigSettings testrigSettings) {

      CompareSameNameQuestion question = (CompareSameNameQuestion) _question;

      _batfish.checkConfigurations(testrigSettings);
      Map<String, Configuration> configurations = _batfish
            .loadConfigurations(testrigSettings);

      CompareSameNameAnswerElement answerElement = new CompareSameNameAnswerElement();

      // collect relevant nodes in a list.
      List<String> nodes = CommonUtil.getMatchingStrings(
            question.getNodeRegex(), configurations.keySet());

      answerElement.add(AsPathAccessList.class.getSimpleName(),
            processAccessPathLists(nodes, configurations));
      answerElement.add(CommunityList.class.getSimpleName(),
            processCommunityLists(nodes, configurations));
      answerElement.add(IkeGateway.class.getSimpleName(),
            processIkeGateways(nodes, configurations));
      answerElement.add(RouteFilterList.class.getSimpleName(),
            processRouteFilterLists(nodes, configurations));

      return answerElement;
   }

   private NamedStructureEquivalenceSets<AsPathAccessList> processAccessPathLists(
         List<String> nodes, Map<String, Configuration> configurations) {
      NamedStructureEquivalenceSets<AsPathAccessList> ae = new NamedStructureEquivalenceSets<AsPathAccessList>(
            AsPathAccessList.class.getSimpleName());
      for (String node : nodes) {
         Map<String, AsPathAccessList> lists = configurations.get(node)
               .getAsPathAccessLists();
         for (String listName : lists.keySet()) {
            ae.add(node, listName, lists.get(listName));
         }
      }
      return ae;
   }

   private NamedStructureEquivalenceSets<CommunityList> processCommunityLists(
         List<String> nodes, Map<String, Configuration> configurations) {
      NamedStructureEquivalenceSets<CommunityList> ae = new NamedStructureEquivalenceSets<CommunityList>(
            CommunityList.class.getSimpleName());
      for (String node : nodes) {
         Map<String, CommunityList> lists = configurations.get(node)
               .getCommunityLists();
         for (String listName : lists.keySet()) {
            ae.add(node, listName, lists.get(listName));
         }
      }
      return ae;
   }

   private NamedStructureEquivalenceSets<IkeGateway> processIkeGateways(
         List<String> nodes, Map<String, Configuration> configurations) {
      NamedStructureEquivalenceSets<IkeGateway> ae = new NamedStructureEquivalenceSets<IkeGateway>(
            IkeGateway.class.getSimpleName());
      for (String node : nodes) {
         Map<String, IkeGateway> lists = configurations.get(node)
               .getIkeGateways();
         for (String listName : lists.keySet()) {
            ae.add(node, listName, lists.get(listName));
         }
      }
      return ae;
   }

   private NamedStructureEquivalenceSets<RouteFilterList> processRouteFilterLists(
         List<String> nodes, Map<String, Configuration> configurations) {
      NamedStructureEquivalenceSets<RouteFilterList> ae = new NamedStructureEquivalenceSets<RouteFilterList>(
            RouteFilterList.class.getSimpleName());
      for (String node : nodes) {
         // Process route filters
         Map<String, RouteFilterList> lists = configurations.get(node)
               .getRouteFilterLists();
         for (String listName : lists.keySet()) {
            ae.add(node, listName, lists.get(listName));
         }
      }
      return ae;
   }
}
