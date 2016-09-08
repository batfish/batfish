package org.batfish.nls;

import java.io.Serializable;

import org.batfish.datamodel.collections.FunctionSet;
import org.batfish.datamodel.collections.LBValueTypeList;
import org.batfish.datamodel.collections.PredicateSemantics;
import org.batfish.datamodel.collections.PredicateValueTypeMap;
import org.batfish.datamodel.collections.QualifiedNameMap;

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

   public LBValueTypeList getPredicateValueTypes(
         String unqualifiedPredicateName) {
      return _predicateValueTypes.get(unqualifiedPredicateName);
   }

   public boolean isFunction(String relationName) {
      return _functions.contains(relationName);
   }

}
