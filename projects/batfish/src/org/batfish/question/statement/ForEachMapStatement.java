package org.batfish.question.statement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.question.QMap;

public class ForEachMapStatement extends ForEachStatement<QMap> {

   public ForEachMapStatement(List<Statement> statements, String var,
         String setVar) {
      super(statements, var, setVar);
   }

   @Override
   protected Collection<QMap> getCollection(Environment environment) {
      return environment.getMaps().values();
   }

   @Override
   protected Map<String, Set<QMap>> getSetMap(Environment environment) {
      throw new BatfishException("unimplemented");
   }

   @Override
   protected Map<String, QMap> getVarMap(Environment environment) {
      return environment.getMaps();
   }

   @Override
   protected void writeVal(Environment environment, QMap t) {
      throw new BatfishException("unimplemented");
   }

}
