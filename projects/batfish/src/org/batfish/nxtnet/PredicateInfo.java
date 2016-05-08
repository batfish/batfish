package org.batfish.nxtnet;

import java.io.Serializable;

import org.batfish.common.collections.FunctionSet;
import org.batfish.common.collections.LBValueTypeList;
import org.batfish.common.collections.PredicateSemantics;
import org.batfish.common.collections.PredicateValueTypeMap;
import org.batfish.common.collections.QualifiedNameMap;

public class PredicateInfo implements Serializable {

   private static final long serialVersionUID = 1L;

   private FunctionSet _functions;
   private PredicateSemantics _predicateSemantics;
   private PredicateValueTypeMap _predicateValueTypes;
   private QualifiedNameMap _qualifiedNameMap;

   public PredicateInfo(PredicateSemantics predicateSemantics,
         PredicateValueTypeMap predicateValueTypes, FunctionSet functions,
         QualifiedNameMap qualifiedNameMap) {
      _functions = functions;
      _predicateSemantics = predicateSemantics;
      _predicateValueTypes = predicateValueTypes;
      _qualifiedNameMap = qualifiedNameMap;
   }

   public QualifiedNameMap getPredicateNames() {
      return _qualifiedNameMap;
   }

   public PredicateSemantics getPredicateSemantics() {
      return _predicateSemantics;
   }

   public String getPredicateSemantics(String unqualifiedPredicateName) {
      return _predicateSemantics.get(unqualifiedPredicateName);
   }

   public LBValueTypeList getPredicateValueTypes(String unqualifiedPredicateName) {
      return _predicateValueTypes.get(unqualifiedPredicateName);
   }

   public boolean isFunction(String relationName) {
      return _functions.contains(relationName);
   }

}
