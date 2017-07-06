package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeRoleSpecifier {

   private static final String ROLE_MAP_VAR = "roleMap";
   
   private static final String ROLE_REGEXES_VAR = "roleRegexes";
   
   private static final long serialVersionUID = 1L;
   
   // a map from roles to the set of nodes that have that role
   private SortedMap<String, SortedSet<String>> _roleMap;
   
   // an ordered list of regexes used to identify roles from node names.
   // each regex in regexes has a single group in it that locates the role name within a node name.
   // there are multiple regexes to handle node names that have different formats.
   private List<String> _roleRegexes;

   public NodeRoleSpecifier() {
   }

   @JsonProperty(ROLE_MAP_VAR)
   public SortedMap<String, SortedSet<String>> getRoleMap() {
      return _roleMap;
   }
   
   @JsonProperty(ROLE_REGEXES_VAR)
   public List<String> getRoleRegexes() {
      return _roleRegexes;
   }
   
   @JsonProperty(ROLE_MAP_VAR)
   public void setRoleMap(SortedMap<String, SortedSet<String>> roleMap) {
      _roleMap = roleMap;
   }
   
   @JsonProperty(ROLE_REGEXES_VAR)
   public void setRoleRegexes(List<String> roleRegexes) {
      _roleRegexes = roleRegexes;
   }
   

   private void addToRoleMap(SortedMap<String, SortedSet<String>> nodeRolesMap, Set<String> nodes) {
      List<Pattern> patList = new ArrayList<>();
      for (String regex : _roleRegexes) {
         try {
            patList.add(Pattern.compile(regex));
         }
         catch (PatternSyntaxException e) {
            throw new BatfishException(
                  "Supplied regex is not a valid Java regex: \"" + regex + "\"",
                  e);
         }
      }
      for (String node : nodes) {
         for (Pattern pattern : patList) {
            Matcher matcher = pattern.matcher(node);
            if (matcher.matches()) {
               try {
                  String role = matcher.group(1);
                  SortedSet<String> currRoles = nodeRolesMap.get(node);
                  if (currRoles == null) {
                     currRoles = new TreeSet<>();
                     nodeRolesMap.put(node,  currRoles);
                  }  
                  currRoles.add(role);
               }
               catch (IndexOutOfBoundsException e) {
                  throw new BatfishException(
                        "Supplied regex does not contain a group: \"" + pattern.pattern() + "\"",
                        e);
               } 
               break;
            }
         }
      }  
   }
   
   // return a map from each node name to the set of roles that it plays
   public SortedMap<String, SortedSet<String>> createNodeRolesMap(Set<String> allNodes) {

      SortedMap<String, SortedSet<String>> nodeRolesMap = new TreeMap<>();
      
      // invert the map from roles to nodes, to create a map from nodes to roles
      if(_roleMap != null)
         _roleMap.forEach(
               (role, nodes) -> {
                  for (String node : nodes) {
                     SortedSet<String> nodeRoles = nodeRolesMap.get(node);
                     if (nodeRoles == null) {
                        nodeRoles = new TreeSet<String>();
                        nodeRolesMap.put(node, nodeRoles);
                     }
                     nodeRoles.add(role);
                  }
               });
      
      if (_roleRegexes != null)
         addToRoleMap(nodeRolesMap, allNodes);
      return nodeRolesMap;
               
   }


}

