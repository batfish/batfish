package batfish.z3;

import java.util.List;

import batfish.collections.VarIndex;
import batfish.z3.node.QueryExpr;

public class QuerySynthesizer {

   private QueryExpr _query;
   private String _queryText;
   private VarIndex _varIndices;
   private List<String> _vars;

   public QuerySynthesizer(List<String> vars) {
      _vars = vars;

   }

   public String getQueryText() {
      return _queryText;
   }
   
   public VarIndex getVarIndices() {
      return _varIndices;
   }
   
   protected void setQuery(QueryExpr query) {
      _query = query;
      _varIndices = _query.getVarIndices(_vars);
   }
   
   protected void setQueryText(String queryText) {
      _queryText = queryText;
   }
   
}
