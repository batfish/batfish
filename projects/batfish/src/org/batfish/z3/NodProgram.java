package org.batfish.z3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;

public class NodProgram {

   private Context _context;

   private final List<BoolExpr> _queries;

   private final Map<String, FuncDecl> _relationDeclarations;

   private final List<BoolExpr> _rules;

   private final Map<String, Integer> _variables;

   public NodProgram(Context context) {
      _context = context;
      _queries = new ArrayList<BoolExpr>();
      _relationDeclarations = new LinkedHashMap<String, FuncDecl>();
      _rules = new ArrayList<BoolExpr>();
      _variables = new LinkedHashMap<String, Integer>();
   }

   public NodProgram append(NodProgram queryProgram) {
      NodProgram result = new NodProgram(_context);
      result._queries.addAll(_queries);
      result._relationDeclarations.putAll(_relationDeclarations);
      result._rules.addAll(_rules);
      result._variables.putAll(_variables);
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

   public Map<String, Integer> getVariables() {
      return _variables;
   }

}
