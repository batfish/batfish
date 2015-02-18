package org.batfish.grammar.logicblox;

import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.batfish.collections.FunctionSet;
import org.batfish.collections.LBValueTypeList;
import org.batfish.collections.PredicateSemantics;
import org.batfish.collections.PredicateValueTypeMap;
import org.batfish.collections.QualifiedNameMap;
import org.batfish.grammar.logicblox.LogiQLParser.*;
import org.batfish.logicblox.LBValueType;
import org.batfish.main.BatfishException;

public class LogiQLPredicateInfoResolver extends LogiQLParserBaseListener {

   private static final String MODULE_NAME = "libbatfish";

   private String _blockName;

   private Map<String, LBValueType> _currentArgTypes;

   private String _currentOutputVar;

   private String _currentPredicateName;

   private String _currentPredicateSemantics;

   private FunctionSet _functions;

   private PredicateSemantics _predicateSemantics;

   private PredicateValueTypeMap _predicateValueTypes;

   private QualifiedNameMap _qualifiedNameMap;

   public LogiQLPredicateInfoResolver(
         PredicateValueTypeMap predicateValueTypes,
         QualifiedNameMap qualifiedNameMap, FunctionSet functions,
         PredicateSemantics predicateSemantics) {
      _predicateValueTypes = predicateValueTypes;
      _qualifiedNameMap = qualifiedNameMap;
      _functions = functions;
      _predicateSemantics = predicateSemantics;
   }

   private void addPredicate(String predicateName, LBValueTypeList list) {
      String qualifiedPredicateName = getQualifiedPredicateName(predicateName);
      if (_predicateValueTypes.get(predicateName) != null) {
         throw new BatfishException("Predicate already declared: "
               + predicateName);
      }
      _predicateValueTypes.put(predicateName, list);
      _qualifiedNameMap.put(predicateName, qualifiedPredicateName);
   }

   private void addQualifiedPredicate(String predicateName) {
      String qualifiedPredicateName = getQualifiedPredicateName(predicateName);
      _qualifiedNameMap.put(predicateName, qualifiedPredicateName);
   }

   private void addSemantics(String predicateName) {
      if (_currentPredicateSemantics != null) {
         _predicateSemantics.put(predicateName, _currentPredicateSemantics);
         _currentPredicateSemantics = null;
      }
   }

   @Override
   public void enterBlock(BlockContext ctx) {
      _blockName = ctx.blockname.getText();
   }

   @Override
   public void enterBoolean_predicate_decl(Boolean_predicate_declContext ctx) {
      _currentPredicateName = ctx.predicate.getText();
      addSemantics(_currentPredicateName);
   }

   @Override
   public void enterEntity_predicate_decl(Entity_predicate_declContext ctx) {
      String predicateName = ctx.predicate.getText();
      addSemantics(predicateName);
      addQualifiedPredicate(predicateName);
   }

   @Override
   public void enterFunction_decl(Function_declContext ctx) {
      _currentPredicateName = ctx.function.getText();
      addSemantics(_currentPredicateName);
      _functions.add(_currentPredicateName);
      _currentOutputVar = ctx.output_var.getText();
   }

   @Override
   public void enterParameter_list(Parameter_listContext ctx) {
      _currentArgTypes = new LinkedHashMap<String, LBValueType>();
      for (Token varToken : ctx.vars) {
         String var = varToken.getText();
         _currentArgTypes.put(var, null);
      }
      if (_currentOutputVar != null) {
         _currentArgTypes.put(_currentOutputVar, null);
      }
   }

   @Override
   public void enterPredicate_semantics(Predicate_semanticsContext ctx) {
      StringBuilder sb = new StringBuilder();
      for (Token lineToken : ctx.lines) {
         String line = lineToken.getText().trim();
         sb.append(line + " ");
      }
      String semantics = sb.toString().trim();
      _currentPredicateSemantics = semantics;
   }

   @Override
   public void enterRefmode_decl(Refmode_declContext ctx) {
      String predicateName = ctx.refmode_predicate.getText();
      addQualifiedPredicate(predicateName);
      addSemantics(predicateName);
   }

   @Override
   public void enterRegular_predicate_decl(Regular_predicate_declContext ctx) {
      _currentPredicateName = ctx.predicate.getText();
      addSemantics(_currentPredicateName);
   }

   public void exitFunction_decl(Function_declContext ctx) {
      LBValueTypeList typeList = new LBValueTypeList();
      typeList.addAll(_currentArgTypes.values());
      addPredicate(_currentPredicateName, typeList);
      _currentPredicateName = null;
      _currentArgTypes = null;
      _currentOutputVar = null;
   }

   @Override
   public void exitRegular_predicate_decl(Regular_predicate_declContext ctx) {
      LBValueTypeList typeList = new LBValueTypeList();
      typeList.addAll(_currentArgTypes.values());
      addPredicate(_currentPredicateName, typeList);
      _currentPredicateName = null;
      _currentArgTypes = null;
   }

   @Override
   public void exitType_decl_list(Type_decl_listContext ctx) {
      for (Type_declContext decl : ctx.type_decls) {
         String var = decl.var.getText();
         LBValueType type = getTypeDeclType(decl);
         _currentArgTypes.put(var, type);
      }
   }

   private LBValueType getNonPrimitiveType(String typeName) {
      return _predicateValueTypes.get(typeName).get(0);
   }

   private String getQualifiedPredicateName(String unqualifiedPredicateName) {
      return MODULE_NAME + ":" + _blockName + ":" + unqualifiedPredicateName;
   }

   private LBValueType getTypeDeclType(Type_declContext ctx) {
      String typeName = ctx.type.getText();
      switch (typeName) {
      case "int":
         return LBValueType.INT;

      case "string":
         return LBValueType.STRING;

      case "float":
         return LBValueType.FLOAT;

      default:
         return getNonPrimitiveType(typeName);
      }
   }

}
