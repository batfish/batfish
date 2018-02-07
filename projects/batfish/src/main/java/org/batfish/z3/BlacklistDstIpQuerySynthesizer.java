package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.QueryExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.expr.visitors.BoolExprTransformer;
import org.batfish.z3.state.Query;

public class BlacklistDstIpQuerySynthesizer extends BaseQuerySynthesizer {

  private Set<Ip> _blacklistIps;

  public BlacklistDstIpQuerySynthesizer(
      @Nullable Set<Ip> explicitBlacklistIps,
      Set<String> blacklistNodes,
      Set<NodeInterfacePair> blacklistInterfaces,
      SortedSet<Edge> blacklistEdges,
      Map<String, Configuration> configurations) {
    _blacklistIps = new TreeSet<>();
    if (explicitBlacklistIps != null) {
      _blacklistIps.addAll(explicitBlacklistIps);
    }
    if (blacklistNodes != null) {
      for (String hostname : blacklistNodes) {
        Configuration node = configurations.get(hostname);
        for (Interface iface : node.getInterfaces().values()) {
          if (iface.getActive()) {
            InterfaceAddress address = iface.getAddress();
            if (address != null) {
              _blacklistIps.add(address.getIp());
            }
          }
        }
      }
    }
    if (blacklistInterfaces != null) {
      for (NodeInterfacePair p : blacklistInterfaces) {
        String hostname = p.getHostname();
        String ifaceName = p.getInterface();
        Configuration node = configurations.get(hostname);
        Interface iface = node.getInterfaces().get(ifaceName);
        if (iface.getActive()) {
          InterfaceAddress address = iface.getAddress();
          if (address != null) {
            _blacklistIps.add(address.getIp());
          }
        }
      }
    }
    if (blacklistEdges != null) {
      for (Edge edge : blacklistEdges) {
        Ip ip1 =
            configurations
                .get(edge.getNode1())
                .getInterfaces()
                .get(edge.getInt1())
                .getAddress()
                .getIp();
        Ip ip2 =
            configurations
                .get(edge.getNode2())
                .getInterfaces()
                .get(edge.getInt2())
                .getAddress()
                .getIp();
        _blacklistIps.add(ip1);
        _blacklistIps.add(ip2);
      }
    }
  }

  @Override
  public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
    NodProgram program = new NodProgram(baseProgram.getContext());
    ImmutableList.Builder<BooleanExpr> queryConditionsBuilder = ImmutableList.builder();
    queryConditionsBuilder.add(SaneExpr.INSTANCE);
    for (Ip blacklistIp : _blacklistIps) {
      BooleanExpr blacklistIpCondition =
          new NotExpr(new EqExpr(new VarIntExpr(HeaderField.DST_IP), new LitIntExpr(blacklistIp)));
      queryConditionsBuilder.add(blacklistIpCondition);
    }
    AndExpr queryConditions = new AndExpr(queryConditionsBuilder.build());
    RuleExpr queryRule = new RuleExpr(queryConditions, Query.EXPR);
    List<BoolExpr> rules = program.getRules();
    rules.add(BoolExprTransformer.toBoolExpr(queryRule.getSubExpression(), baseProgram));
    QueryExpr query = new QueryExpr(Query.EXPR);
    BoolExpr queryBoolExpr = BoolExprTransformer.toBoolExpr(query.getSubExpression(), baseProgram);
    program.getQueries().add(queryBoolExpr);
    return program;
  }
}
