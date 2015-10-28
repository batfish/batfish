package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.grammar.question.VariableType;
import org.batfish.main.Settings;
import org.batfish.representation.Ip;

public class VariableDeclarationStatement implements Statement {

   private final VariableType _type;

   private final String _var;

   public VariableDeclarationStatement(String var, VariableType type) {
      _var = var;
      _type = type;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      switch (_type) {
      case SET_IP:
         initSetIp(environment, logger, settings);
         break;

      case INT:
      case IP:
      case ROUTE_FILTER:
      case SET_INT:
      case SET_ROUTE_FILTER:
      case SET_STRING:
      case STRING:
      default:
         throw new BatfishException(
               "initialization for this type not yet implemented: "
                     + _type.toString());
      }
   }

   private void initSetIp(Environment environment, BatfishLogger logger,
         Settings settings) {
      Map<String, Set<Ip>> ipSets = environment.getIpSets();
      Set<Ip> ipSet = ipSets.get(_var);
      if (ipSet == null) {
         ipSet = new HashSet<Ip>();
         ipSets.put(_var, ipSet);
      }
      else {
         ipSet.clear();
      }
   }

}
