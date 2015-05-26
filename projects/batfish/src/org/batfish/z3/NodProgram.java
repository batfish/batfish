package org.batfish.z3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;

public class NodProgram {

   private Context _context;

   private final List<BoolExpr> _queries;

   private final Map<String, FuncDecl> _relationDeclarations;

   private final List<BoolExpr> _rules;

   private final Map<String, BitVecExpr> _variables;

   private final Map<String, BitVecExpr> _variablesAsConsts;

   private final Map<String, Integer> _variableSizes;

   public NodProgram(Context context) {
      _context = context;
      _queries = new ArrayList<BoolExpr>();
      _relationDeclarations = new LinkedHashMap<String, FuncDecl>();
      _rules = new ArrayList<BoolExpr>();
      _variables = new LinkedHashMap<String, BitVecExpr>();
      _variableSizes = new LinkedHashMap<String, Integer>();
      _variablesAsConsts = new LinkedHashMap<String, BitVecExpr>();
   }

   public NodProgram append(NodProgram queryProgram) {
      NodProgram result = new NodProgram(_context);
      result._queries.addAll(_queries);
      result._relationDeclarations.putAll(_relationDeclarations);
      result._rules.addAll(_rules);
      result._variables.putAll(_variables);
      result._variableSizes.putAll(_variableSizes);
      result._variablesAsConsts.putAll(_variablesAsConsts);
      result._queries.addAll(queryProgram._queries);
      result._relationDeclarations.putAll(queryProgram._relationDeclarations);
      result._rules.addAll(queryProgram._rules);
      result._variables.putAll(queryProgram._variables);
      result._variableSizes.putAll(queryProgram._variableSizes);
      result._variablesAsConsts.putAll(queryProgram._variablesAsConsts);
      return result;
   }

   public Context getContext() {
      return _context;
   }

   public List<BoolExpr> getQueries() {
      return _queries;
   }

   public Map<String, FuncDecl> getRelationDeclarations() {
      return _relationDeclarations;
   }

   public List<BoolExpr> getRules() {
      return _rules;
   }

   public Map<String, BitVecExpr> getVariables() {
      return _variables;
   }

   public Map<String, BitVecExpr> getVariablesAsConsts() {
      return _variablesAsConsts;
   }

   public Map<String, Integer> getVariableSizes() {
      return _variableSizes;
   }

}
