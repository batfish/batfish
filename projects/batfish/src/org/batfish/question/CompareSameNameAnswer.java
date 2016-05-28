package org.batfish.question;

import java.util.List;
import java.util.Map;

import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
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
      processRouteFilterLists(nodes, configurations);
   }
   
   private void processAccessPathLists(List<String> nodes, Map<String, Configuration> configurations)
   {
      CompareSameNameAnswerElement<AsPathAccessList> ae = new CompareSameNameAnswerElement<AsPathAccessList>(AsPathAccessList.class.getSimpleName());
      for (String node : nodes) {
         
         // Process AsAccessPathList structures.
         Map<String, AsPathAccessList> asPathAccessLists =  configurations.get(node).getAsPathAccessLists();
            for (String asPathAccessListName : asPathAccessLists.keySet()) {
               ae.add(node, asPathAccessListName, asPathAccessLists.get(asPathAccessListName));
         }
      }
      addAnswerElement(ae);
   }   
   
   private void processCommunityLists(List<String> nodes, Map<String, Configuration> configurations)
   {
      CompareSameNameAnswerElement<CommunityList> ae = new CompareSameNameAnswerElement<CommunityList>(CommunityList.class.getSimpleName());
      for (String node : nodes) {
         Map<String, CommunityList> communityLists =  configurations.get(node).getCommunityLists();
         for (String communityListName : communityLists.keySet()) {
            ae.add(node, communityListName, communityLists.get(communityListName));
         }     
      }
      addAnswerElement(ae);
   }
   
   private void processRouteFilterLists(List<String> nodes, Map<String, Configuration> configurations)
   {
      CompareSameNameAnswerElement<RouteFilterList> ae = new CompareSameNameAnswerElement<RouteFilterList>(RouteFilterList.class.getSimpleName());
      for (String node : nodes) {
         // Process route filters
         Map<String, RouteFilterList> routeFilterLists =  configurations.get(node).getRouteFilterLists();
         for (String routeFilterListName : routeFilterLists.keySet()) {
            ae.add(node, routeFilterListName, routeFilterLists.get(routeFilterListName));  
         }
      }
      addAnswerElement(ae);
   }   
   
}

