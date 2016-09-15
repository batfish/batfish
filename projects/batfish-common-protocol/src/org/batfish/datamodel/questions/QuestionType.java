package org.batfish.datamodel.questions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum QuestionType {
   ACL_REACHABILITY("aclreachability"),
   BGP_ADVERTISEMENTS("bgpadvertisements"),
   BGP_SESSION_CHECK("bgpsessioncheck"),
   COMPARE_SAME_NAME("comparesamename"),
   ENVIRONMENT_CREATION("environmentcreation"),
   ERROR("error"),
   IPSEC_VPN_CHECK("ipsecvpncheck"),
   ISIS_LOOPBACKS("isisloopbacks"),
   NEIGHBORS("neighbors"),
   NODES("nodes"),
   OSPF_LOOPBACKS("ospfloopbacks"),
   PAIRWISE_VPN_CONNECTIVITY("pairwisevpnconnectivity"),
   PROTOCOL_DEPENDENCIES("protocoldependencies"),
   REACHABILITY("reachability"),
   SELF_ADJACENCIES("selfadjacencies"),
   TRACEROUTE("traceroute"),
   UNDEFINED_REFERENCES("undefinedreferences"),
   UNIQUE_BGP_PREFIX_ORIGINATION("uniquebgpprefixorigination"),
   UNIQUE_IP_ASSIGNMENTS("uniqueipassignments"),
   UNUSED_STRUCTURES("unusedstructures");

   private final static Map<String, QuestionType> _map = buildMap();

   private static Map<String, QuestionType> buildMap() {
      Map<String, QuestionType> map = new HashMap<>();
      for (QuestionType value : QuestionType.values()) {
         String name = value._name;
         map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
   }

   @JsonCreator
   public static QuestionType fromName(String name) {
      return _map.get(name.toLowerCase());
   }

   private final String _name;

   private QuestionType(String name) {
      _name = name;
   }

   public String questionTypeName() {
      return _name;
   }

}
