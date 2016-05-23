package org.batfish.question;

import java.util.List;
import java.util.Map;

import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.CompareSameNameAnswerElement;
import org.batfish.datamodel.questions.CompareSameNameQuestion;
import org.batfish.main.Batfish;
import org.batfish.util.Util;

public class CompareSameNameAnswer extends Answer {

   public CompareSameNameAnswer(Batfish batfish,
         CompareSameNameQuestion question) {
      
      batfish.checkConfigurations();
      Map<String, Configuration> configurations = batfish.loadConfigurations();

      //Configuration x = configurations.get("as1border1");
      //Map<String, RouteFilterList> y = x.getRouteFilterLists();
      //int z = y.keySet().size();
      
      // collect relevant nodes in a list.
      List<String> nodes = Util.getMatchingStrings(question.getNodeRegex(), configurations.keySet());
      
      // Add 
      CompareSameNameAnswerElement<AsPathAccessList> asPathAccessListAnswerElement = new CompareSameNameAnswerElement<AsPathAccessList>();
      addAnswerElement(asPathAccessListAnswerElement);
      
      CompareSameNameAnswerElement<RouteFilterList> routeFilterListAnswerElement = new CompareSameNameAnswerElement<RouteFilterList>();
      addAnswerElement(routeFilterListAnswerElement);
      
      for (String node : nodes) {
         
         // Process AsAccessPathList structures.
         Map<String, AsPathAccessList> asPathAccessLists =  configurations.get(node).getAsPathAccessLists();
            for (String asPathAccessListName : asPathAccessLists.keySet()) {
               asPathAccessListAnswerElement.add(node, asPathAccessListName, asPathAccessLists.get(asPathAccessListName));
         }
         
         // Process route filters
         Map<String, RouteFilterList> routeFilterLists =  configurations.get(node).getRouteFilterLists();
         for (String routeFilterListName : routeFilterLists.keySet()) {
            routeFilterListAnswerElement.add(node, routeFilterListName, routeFilterLists.get(routeFilterListName));
         }     
         
      }
      
   }   
}

