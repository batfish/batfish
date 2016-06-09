package org.batfish.question;

import java.util.List;
import java.util.Map;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.CompareSameNameAnswerElement;
import org.batfish.datamodel.questions.CompareSameNameQuestion;
import org.batfish.main.Batfish;

public class CompareSameNameAnswer extends Answer {

   public CompareSameNameAnswer(Batfish batfish,
         CompareSameNameQuestion question) {
      
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();

      // collect relevant nodes in a list.
      List<String> nodes = CommonUtil.getMatchingStrings(question.getNodeRegex(), configurations.keySet());
      
      processAccessPathLists(nodes, configurations);
      processCommunityLists(nodes,configurations);
      processIkeGateways(nodes, configurations);
      processRouteFilterLists(nodes, configurations);
   }
  
   private void processAccessPathLists(List<String> nodes, Map<String, Configuration> configurations)
   {
      CompareSameNameAnswerElement<AsPathAccessList> ae = new CompareSameNameAnswerElement<AsPathAccessList>(AsPathAccessList.class.getSimpleName());
      for (String node : nodes) {       
         Map<String, AsPathAccessList> lists =  configurations.get(node).getAsPathAccessLists();
            for (String listName : lists.keySet()) {
               ae.add(node, listName, lists.get(listName));
         }
      }
      addAnswerElement(ae);
   }   
   
   private void processCommunityLists(List<String> nodes, Map<String, Configuration> configurations)
   {
      CompareSameNameAnswerElement<CommunityList> ae = new CompareSameNameAnswerElement<CommunityList>(CommunityList.class.getSimpleName());
      for (String node : nodes) {
         Map<String, CommunityList> lists =  configurations.get(node).getCommunityLists();
         for (String listName : lists.keySet()) {
            ae.add(node, listName, lists.get(listName));
         }     
      }
      addAnswerElement(ae);
   }
   
   private void processIkeGateways(List<String> nodes, Map<String, Configuration> configurations)
   {
      CompareSameNameAnswerElement<IkeGateway> ae = new CompareSameNameAnswerElement<IkeGateway>(IkeGateway.class.getSimpleName());
      for (String node : nodes) {
         Map<String, IkeGateway> lists =  configurations.get(node).getIkeGateways();
         for (String listName : lists.keySet()) {
            ae.add(node, listName, lists.get(listName));
         }     
      }
      addAnswerElement(ae);
   }
   
   private void processRouteFilterLists(List<String> nodes, Map<String, Configuration> configurations)
   {
      CompareSameNameAnswerElement<RouteFilterList> ae = new CompareSameNameAnswerElement<RouteFilterList>(RouteFilterList.class.getSimpleName());
      for (String node : nodes) {
         // Process route filters
         Map<String, RouteFilterList> lists =  configurations.get(node).getRouteFilterLists();
         for (String listName : lists.keySet()) {
            ae.add(node, listName, lists.get(listName));  
         }
      }
      addAnswerElement(ae);
   }
}

