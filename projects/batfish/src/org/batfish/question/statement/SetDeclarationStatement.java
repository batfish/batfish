package org.batfish.question.statement;

import java.util.HashSet;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.grammar.question.VariableType;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterList;

public class SetDeclarationStatement implements Statement {

   private final VariableType _type;

   private final String _var;

   public SetDeclarationStatement(String var, VariableType type) {
      _var = var;
      _type = type;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      switch (_type) {
      case SET_IP:
         initIpSet(environment, logger, settings);
         break;

      case SET_INT:
         initIntSet(environment, logger, settings);
         break;

      case SET_PREFIX:
         initPrefixSet(environment, logger, settings);

      case SET_ROUTE_FILTER:
         initRouteFilterSet(environment, logger, settings);
         break;

      case SET_STRING:
         initStringSet(environment, logger, settings);
         break;

      case ACTION:
      case INT:
      case IP:
      case PREFIX:
      case RANGE:
      case ROUTE_FILTER:
      case REGEX:
      case STRING:
         throw new BatfishException("not a set type: " + _type.toString());

      default:
         throw new BatfishException(
               "initialization for this type not yet implemented: "
                     + _type.toString());
      }
   }

   private void initIntSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getIntegerSets().get(_var) == null) {
         environment.getIntegerSets().put(_var, new HashSet<Integer>());
      }
   }

   private void initIpSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getIpSets().get(_var) == null) {
         environment.getIpSets().put(_var, new HashSet<Ip>());
      }
   }

   private void initPrefixSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getPrefixSets().get(_var) == null) {
         environment.getPrefixSets().put(_var, new HashSet<Prefix>());
      }
   }

   private void initRouteFilterSet(Environment environment,
         BatfishLogger logger, Settings settings) {
      if (environment.getRouteFilterSets().get(_var) == null) {
         environment.getRouteFilterSets().put(_var,
               new HashSet<RouteFilterList>());
      }
   }

   private void initStringSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getStringSets().get(_var) == null) {
         environment.getStringSets().put(_var, new HashSet<String>());
      }
   }

}
