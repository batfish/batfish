package org.batfish.question.statement;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.ip_expr.IpExpr;
import org.batfish.representation.Ip;

public class AddIpStatement implements Statement {

   private IpExpr _ipExpr;

   private String _target;

   public AddIpStatement(String target, IpExpr ipExpr) {
      _target = target;
      _ipExpr = ipExpr;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Map<String, Set<Ip>> ipSets = environment.getIpSets();
      Set<Ip> ips = ipSets.get(_target);
      if (ips == null) {
         ips = new HashSet<Ip>();
         ipSets.put(_target, ips);
      }
      Ip ip = _ipExpr.evaluate(environment);
      ips.add(ip);
   }

}
