package org.batfish.grammar.logicblox;

import org.batfish.collections.LBValueTypeList;
import org.batfish.collections.PredicateValueTypeMap;
import org.batfish.grammar.logicblox.LogiQLParser.*;
import org.batfish.logicblox.LBValueType;
import org.batfish.common.BatfishException;

public class LogQLPredicateInfoExtractor extends LogiQLParserBaseListener {

   private PredicateValueTypeMap _unqualifiedPredicateValueTypes;

   public LogQLPredicateInfoExtractor(
         PredicateValueTypeMap unqualifiedPredicateValueTypes) {
      _unqualifiedPredicateValueTypes = unqualifiedPredicateValueTypes;
   }

   private void addPredicate(String predicateName) {
      if (_unqualifiedPredicateValueTypes.get(predicateName) != null) {
         throw new BatfishException("Predicate already declared: "
               + predicateName);
      }
      LBValueTypeList list = new LBValueTypeList();
      _unqualifiedPredicateValueTypes.put(predicateName, list);
   }

   private void addPredicateValueType(String predicateName,
         LBValueType lbValueType) {
      LBValueTypeList list = _unqualifiedPredicateValueTypes.get(predicateName);
      list.add(lbValueType);
   }

   @Override
   public void enterBoolean_predicate_decl(Boolean_predicate_declContext ctx) {
      String predicateName = ctx.predicate.getText();
      addPredicate(predicateName);
   }

   @Override
   public void enterRefmode_decl(Refmode_declContext ctx) {
      String predicateName = ctx.refmode_predicate.getText();
      LBValueType refType;
      switch (predicateName) {

      case "AdvertisementType":
         refType = LBValueType.ENTITY_REF_ADVERTISEMENT_TYPE;
         break;

      case "AsPath":
         refType = LBValueType.ENTITY_REF_AS_PATH;
         break;

      case "AutonomousSystem":
         refType = LBValueType.ENTITY_REF_AUTONOMOUS_SYSTEM;
         break;

      case "FlowTag":
         refType = LBValueType.ENTITY_REF_FLOW_TAG;
         break;

      case "Interface":
         refType = LBValueType.ENTITY_REF_INTERFACE;
         break;

      case "Ip":
         refType = LBValueType.ENTITY_REF_IP;
         break;

      case "Node":
         refType = LBValueType.ENTITY_REF_NODE;
         break;

      case "OriginType":
         refType = LBValueType.ENTITY_REF_ORIGIN_TYPE;
         break;

      case "PolicyMap":
         refType = LBValueType.ENTITY_REF_POLICY_MAP;
         break;

      case "RoutingProtocol":
         refType = LBValueType.ENTITY_REF_ROUTING_PROTOCOL;
         break;

      default:
         LBValueType valueType = getTypeDeclValueType(ctx.type_decl());
         refType = getRefType(valueType);
         break;
      }
      addPredicate(predicateName);
      addPredicateValueType(predicateName, refType);
   }

   @Override
   public void exitEntity_predicate_decl(Entity_predicate_declContext ctx) {
      String predicateName = ctx.predicate.getText();
      LBValueType entityValueType;
      switch (predicateName) {
      case "BgpAdvertisement":
         entityValueType = LBValueType.ENTITY_INDEX_BGP_ADVERTISEMENT;
         break;

      case "Flow":
         entityValueType = LBValueType.ENTITY_INDEX_FLOW;
         break;

      case "Network":
         entityValueType = LBValueType.ENTITY_INDEX_NETWORK;
         break;

      case "Route":
         entityValueType = LBValueType.ENTITY_INDEX_ROUTE;
         break;

      default:
         entityValueType = LBValueType.ENTITY_INDEX_INT;
      }
      addPredicate(predicateName);
      addPredicateValueType(predicateName, entityValueType);
   }

   private LBValueType getRefType(LBValueType valueType) {
      switch (valueType) {
      case INT:
         return LBValueType.ENTITY_REF_INT;

      case STRING:
         return LBValueType.ENTITY_REF_STRING;

         // $CASES-OMITTED$
      default:
         throw new BatfishException("no refmode type for given value type: "
               + valueType.toString());
      }
   }

   private LBValueType getTypeDeclValueType(Type_declContext ctx) {
      String typeName = ctx.type.getText();
      switch (typeName) {
      case "int":
         return LBValueType.INT;
      case "string":
         return LBValueType.STRING;
      default:
         throw new BatfishException("invalid refmode value type: " + typeName);
      }
   }

}
