package org.batfish.z3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.collections.EdgeSet;
import org.batfish.collections.FibMap;
import org.batfish.collections.FibRow;
import org.batfish.collections.FlowSinkInterface;
import org.batfish.collections.FlowSinkSet;
import org.batfish.collections.NodeSet;
import org.batfish.collections.PolicyRouteFibIpMap;
import org.batfish.collections.PolicyRouteFibNodeMap;
import org.batfish.collections.RoleSet;
import org.batfish.main.BatfishException;
import org.batfish.representation.Configuration;
import org.batfish.representation.Edge;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.IpAccessList;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchIpAccessListLine;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchType;
import org.batfish.representation.PolicyMapSetLine;
import org.batfish.representation.PolicyMapSetNextHopLine;
import org.batfish.representation.PolicyMapSetType;
import org.batfish.representation.Prefix;
import org.batfish.util.SubRange;
import org.batfish.util.Util;
import org.batfish.z3.node.AcceptExpr;
import org.batfish.z3.node.AclDenyExpr;
import org.batfish.z3.node.AclMatchExpr;
import org.batfish.z3.node.AclNoMatchExpr;
import org.batfish.z3.node.AclPermitExpr;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.BooleanExpr;
import org.batfish.z3.node.Comment;
import org.batfish.z3.node.DeclareRelExpr;
import org.batfish.z3.node.DeclareVarExpr;
import org.batfish.z3.node.DestinationRouteExpr;
import org.batfish.z3.node.DropExpr;
import org.batfish.z3.node.EqExpr;
import org.batfish.z3.node.ExternalDestinationIpExpr;
import org.batfish.z3.node.ExternalSourceIpExpr;
import org.batfish.z3.node.ExtractExpr;
import org.batfish.z3.node.FalseExpr;
import org.batfish.z3.node.IntExpr;
import org.batfish.z3.node.LitIntExpr;
import org.batfish.z3.node.NodeAcceptExpr;
import org.batfish.z3.node.NodeDropExpr;
import org.batfish.z3.node.NodeTransitExpr;
import org.batfish.z3.node.NotExpr;
import org.batfish.z3.node.OrExpr;
import org.batfish.z3.node.OriginateExpr;
import org.batfish.z3.node.PacketRelExpr;
import org.batfish.z3.node.PolicyDenyExpr;
import org.batfish.z3.node.PolicyExpr;
import org.batfish.z3.node.PolicyMatchExpr;
import org.batfish.z3.node.PolicyNoMatchExpr;
import org.batfish.z3.node.PolicyPermitExpr;
import org.batfish.z3.node.PostInExpr;
import org.batfish.z3.node.PostInInterfaceExpr;
import org.batfish.z3.node.PostOutInterfaceExpr;
import org.batfish.z3.node.PreInInterfaceExpr;
import org.batfish.z3.node.PreOutEdgeExpr;
import org.batfish.z3.node.PreOutExpr;
import org.batfish.z3.node.PreOutInterfaceExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RoleAcceptExpr;
import org.batfish.z3.node.RoleOriginateExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;
import org.batfish.z3.node.Statement;
import org.batfish.z3.node.TrueExpr;
import org.batfish.z3.node.VarIntExpr;

public class Synthesizer {
   public static final String DST_IP_VAR = "dst_ip";
   public static final String DST_PORT_VAR = "dst_port";
   public static final String FAKE_INTERFACE_PREFIX = "TenGigabitEthernet200/";
   private static final String FLOW_SINK_TERMINATION_NAME = "flow_sink_termination";
   public static final int IP_BITS = 32;
   public static final String IP_PROTOCOL_VAR = "ip_prot";
   private static final String NODE_NONE_NAME = "(none)";
   public static final Map<String, Integer> PACKET_VAR_SIZES = initPacketVarSizes();
   public static final List<String> PACKET_VARS = getPacketVars();
   private static final int PORT_BITS = 16;
   private static final int PORT_MAX = 65535;
   private static final int PORT_MIN = 0;
   private static final int PROTOCOL_BITS = 9;
   public static final String SRC_IP_VAR = "src_ip";
   public static final String SRC_PORT_VAR = "src_port";

   public static BooleanExpr bitvectorGEExpr(String bv, long lb, int numBits) {
      // these masks refer to nested conditions, not to bitwise and, or
      int numBitsLeft = numBits;

      BooleanExpr finalExpr = null;
      BooleanExpr currentExpr = null;
      OrExpr currentOrExpr = null;
      AndExpr currentAndExpr = null;
      while (numBitsLeft > 0) {
         // find largest remaining 'subnet mask' not overlapping with lowerbound
         int orSpread = -1;
         long orMask = 0;
         int orStartPos;
         int orEndPos;
         orEndPos = numBitsLeft - 1;
         for (int i = orEndPos; i >= 0; i--) {
            orMask |= (1L << i);
            if ((lb & orMask) != 0) {
               orMask ^= (1L << i);
               break;
            }
            else {
               orSpread++;
               numBitsLeft--;
            }
         }
         if (orSpread >= 0) {
            orStartPos = orEndPos - orSpread;
            LitIntExpr zeroExpr = new LitIntExpr(0L, orStartPos, orEndPos);
            IntExpr extractExpr = newExtractExpr(bv, numBits, orStartPos,
                  orEndPos);
            EqExpr eqExpr = new EqExpr(extractExpr, zeroExpr);
            NotExpr notExpr = new NotExpr(eqExpr);
            OrExpr oldOrExpr = currentOrExpr;
            currentOrExpr = new OrExpr();
            currentOrExpr.addDisjunct(notExpr);
            if (currentExpr != null) {
               if (currentExpr == currentAndExpr) {
                  currentAndExpr.addConjunct(currentOrExpr);
               }
               else if (currentExpr == oldOrExpr) {
                  oldOrExpr.addDisjunct(currentOrExpr);
               }
            }
            else {
               finalExpr = currentOrExpr;
            }
            currentExpr = currentOrExpr;
         }

         // find largest remaining 'subnet mask' not overlapping with lowerbound
         int andSpread = -1;
         long andMask = 0;
         int andStartPos;
         int andEndPos;
         andEndPos = numBitsLeft - 1;
         for (int i = andEndPos; i >= 0; i--) {
            andMask |= (1L << i);
            if ((lb & andMask) != andMask) {
               andMask ^= (1L << i);
               break;
            }
            else {
               andSpread++;
               numBitsLeft--;
            }
         }
         if (andSpread >= 0) {
            andStartPos = andEndPos - andSpread;
            LitIntExpr andMaskExpr = new LitIntExpr(andMask, andStartPos,
                  andEndPos);
            IntExpr extractExpr = newExtractExpr(bv, numBits, andStartPos,
                  andEndPos);
            EqExpr eqExpr = new EqExpr(extractExpr, andMaskExpr);

            AndExpr oldAndExpr = currentAndExpr;
            currentAndExpr = new AndExpr();
            currentAndExpr.addConjunct(eqExpr);
            if (currentExpr != null) {
               if (currentExpr == currentOrExpr) {
                  currentOrExpr.addDisjunct(currentAndExpr);
               }
               else if (currentExpr == oldAndExpr) {
                  oldAndExpr.addConjunct(currentAndExpr);
               }
            }
            else {
               finalExpr = currentAndExpr;
            }
            currentExpr = currentAndExpr;
         }
      }
      return finalExpr;
   }

   public static BooleanExpr bitvectorLEExpr(String bv, long lb, int numBits) {
      OrExpr leExpr = new OrExpr();
      LitIntExpr upperBound = new LitIntExpr(lb, numBits);
      VarIntExpr var = new VarIntExpr(bv);
      EqExpr exactMatch = new EqExpr(var, upperBound);
      BooleanExpr ge = bitvectorGEExpr(bv, lb, numBits);
      NotExpr lessThan = new NotExpr(ge);
      leExpr.addDisjunct(exactMatch);
      leExpr.addDisjunct(lessThan);
      return leExpr;
   }

   private static List<String> getPacketVars() {
      List<String> vars = new ArrayList<String>();
      vars.add(SRC_IP_VAR);
      vars.add(DST_IP_VAR);
      vars.add(SRC_PORT_VAR);
      vars.add(DST_PORT_VAR);
      vars.add(IP_PROTOCOL_VAR);
      return vars;
   }

   public static List<Statement> getVarDeclExprs() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Variable Declarations"));
      for (Entry<String, Integer> e : PACKET_VAR_SIZES.entrySet()) {
         String var = e.getKey();
         int size = e.getValue();
         statements.add(new DeclareVarExpr(var, size));
      }
      return statements;
   }

   public static String indent(int n) {
      String output = "";
      for (int i = 0; i < n; i++) {
         output += "   ";
      }
      return output;
   }

   private static Map<String, Integer> initPacketVarSizes() {
      Map<String, Integer> varSizes = new LinkedHashMap<String, Integer>();
      varSizes.put(SRC_IP_VAR, IP_BITS);
      varSizes.put(DST_IP_VAR, IP_BITS);
      varSizes.put(SRC_PORT_VAR, PORT_BITS);
      varSizes.put(DST_PORT_VAR, PORT_BITS);
      varSizes.put(IP_PROTOCOL_VAR, PROTOCOL_BITS);
      return varSizes;
   }

   private static boolean isLoopbackInterface(String ifaceName) {
      String lcIfaceName = ifaceName.toLowerCase();
      return lcIfaceName.startsWith("lo");
   }

   private static IntExpr newExtractExpr(String var, int low, int high) {
      int varSize = PACKET_VAR_SIZES.get(var);
      return newExtractExpr(var, varSize, low, high);
   }

   private static IntExpr newExtractExpr(String var, int varSize, int low,
         int high) {
      if (low == 0 && high == varSize - 1) {
         return new VarIntExpr(var);
      }
      else {
         return new ExtractExpr(var, low, high);
      }
   }

   private final Map<String, Configuration> _configurations;
   private final FibMap _fibs;
   private FlowSinkSet _flowSinks;
   private final PolicyRouteFibNodeMap _prFibs;
   private final boolean _simplify;
   private final EdgeSet _topologyEdges;
   private final Map<String, Set<Interface>> _topologyInterfaces;
   private List<String> _warnings;

   public Synthesizer(Map<String, Configuration> configurations, FibMap fibs,
         PolicyRouteFibNodeMap prFibs, EdgeSet topologyEdges, boolean simplify,
         FlowSinkSet flowSinks) {
      _configurations = configurations;
      pruneInterfaces();
      _fibs = fibs;
      _topologyEdges = topologyEdges;
      _prFibs = prFibs;
      _simplify = simplify;
      _topologyInterfaces = new TreeMap<String, Set<Interface>>();
      _warnings = new ArrayList<String>();
      _flowSinks = flowSinks;
      computeTopologyInterfaces();
   }

   private void computeTopologyInterfaces() {
      for (Edge edge : _topologyEdges) {
         String hostname = edge.getNode1();
         if (!_topologyInterfaces.containsKey(hostname)) {
            _topologyInterfaces.put(hostname, new TreeSet<Interface>());
         }
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         String interfaceName = edge.getInt1();
         if (interfaceName.startsWith(FAKE_INTERFACE_PREFIX)) {
            continue;
         }
         Interface i = _configurations.get(hostname).getInterfaces()
               .get(interfaceName);
         interfaces.add(i);
      }
      for (String hostname : _configurations.keySet()) {
         Configuration c = _configurations.get(hostname);
         Map<String, Interface> nodeInterfaces = c.getInterfaces();
         for (String ifaceName : nodeInterfaces.keySet()) {
            if (isFlowSink(hostname, ifaceName)) {
               Interface iface = nodeInterfaces.get(ifaceName);
               if (!_topologyInterfaces.containsKey(hostname)) {
                  _topologyInterfaces.put(hostname, new TreeSet<Interface>());
               }
               Set<Interface> interfaces = _topologyInterfaces.get(hostname);
               interfaces.add(iface);
            }
         }
      }
   }

   private List<Statement> getAcceptRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Node accept lead to universal accept"));
      for (String nodeName : _configurations.keySet()) {
         NodeAcceptExpr nodeAccept = new NodeAcceptExpr(nodeName);
         RuleExpr connectAccepts = new RuleExpr(nodeAccept, AcceptExpr.INSTANCE);
         statements.add(connectAccepts);
      }
      return statements;
   }

   private List<Statement> getDestRouteToPreOutEdgeRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment(
                  "Rules for sending destination routed packets to preoutIface stage"));
      for (String hostname : _fibs.keySet()) {
         TreeSet<FibRow> fibSet = _fibs.get(hostname);
         FibRow firstRow = fibSet.first();
         if (!firstRow.getPrefix().equals(Prefix.ZERO)) {
            // no default route, so add one that drops traffic
            FibRow dropDefaultRow = new FibRow(Prefix.ZERO,
                  FibRow.DROP_INTERFACE, "", "");
            fibSet.add(dropDefaultRow);
         }
         FibRow[] fib = fibSet.toArray(new FibRow[] {});
         for (int i = 0; i < fib.length; i++) {
            FibRow currentRow = fib[i];
            if (currentRow.getInterface().startsWith(FAKE_INTERFACE_PREFIX)) {
               continue;
            }
            Set<FibRow> notRows = new TreeSet<FibRow>();
            for (int j = i + 1; j < fib.length; j++) {
               FibRow specificRow = fib[j];
               long currentStart = currentRow.getPrefix().getAddress().asLong();
               long currentEnd = currentRow.getPrefix().getEndAddress()
                     .asLong();
               long specificStart = specificRow.getPrefix().getAddress()
                     .asLong();
               long specificEnd = specificRow.getPrefix().getEndAddress()
                     .asLong();
               // check whether later prefix is contained in this one
               if (currentStart <= specificStart && specificEnd <= currentEnd) {
                  if (currentStart == specificStart
                        && currentEnd == specificEnd) {
                     // load balancing
                     continue;
                  }
                  if (currentRow.getInterface().equals(
                        specificRow.getInterface())
                        && currentRow.getNextHop().equals(
                              specificRow.getNextHop())
                        && currentRow.getNextHopInterface().equals(
                              specificRow.getNextHopInterface())) {
                     // no need to exclude packets matching the more specific
                     // prefix,
                     // since they would go out same edge
                     continue;
                  }
                  // exclude packets that match a more specific prefix that
                  // would go out a different interface
                  notRows.add(specificRow);
               }
               else {
                  break;
               }
            }
            AndExpr conditions = new AndExpr();
            DestinationRouteExpr destRoute = new DestinationRouteExpr(hostname);
            conditions.addConjunct(destRoute);
            String ifaceOutName = currentRow.getInterface();
            PacketRelExpr action;
            if (ifaceOutName.equals(FibRow.DROP_INTERFACE)
                  || isLoopbackInterface(ifaceOutName)
                  || Util.isNullInterface(ifaceOutName)) {
               action = new NodeDropExpr(hostname);
            }
            else {
               String nextHop = currentRow.getNextHop();
               String ifaceInName = currentRow.getNextHopInterface();
               action = new PreOutEdgeExpr(hostname, ifaceOutName, nextHop,
                     ifaceInName);
            }

            // must not match more specific routes
            for (FibRow notRow : notRows) {
               int prefixLength = notRow.getPrefix().getPrefixLength();
               long prefix = notRow.getPrefix().getAddress().asLong();
               int first = IP_BITS - prefixLength;
               if (first >= IP_BITS) {
                  continue;
               }
               int last = IP_BITS - 1;
               LitIntExpr prefixFragmentLit = new LitIntExpr(prefix, first,
                     last);
               IntExpr prefixFragmentExt = newExtractExpr(DST_IP_VAR, first,
                     last);
               NotExpr noPrefixMatch = new NotExpr();
               EqExpr prefixMatch = new EqExpr(prefixFragmentExt,
                     prefixFragmentLit);
               noPrefixMatch.SetArgument(prefixMatch);
               conditions.addConjunct(noPrefixMatch);
            }

            // must match route
            int prefixLength = currentRow.getPrefix().getPrefixLength();
            long prefix = currentRow.getPrefix().getAddress().asLong();
            int first = IP_BITS - prefixLength;
            if (first < IP_BITS) {
               int last = IP_BITS - 1;
               LitIntExpr prefixFragmentLit = new LitIntExpr(prefix, first,
                     last);
               IntExpr prefixFragmentExt = newExtractExpr(DST_IP_VAR, first,
                     last);
               EqExpr prefixMatch = new EqExpr(prefixFragmentExt,
                     prefixFragmentLit);
               conditions.addConjunct(prefixMatch);
            }

            // then we forward out specified interface (or drop)
            RuleExpr rule = new RuleExpr(conditions, action);
            statements.add(rule);
         }
      }
      return statements;
   }

   private List<Statement> getDropRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Node drop lead to universal drop"));
      for (String nodeName : _configurations.keySet()) {
         NodeDropExpr nodeDrop = new NodeDropExpr(nodeName);
         RuleExpr connectDrops = new RuleExpr(nodeDrop, DropExpr.INSTANCE);
         statements.add(connectDrops);
      }
      return statements;
   }

   private List<Statement> getExternalDstIpRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment(
                  "Rule for matching external Source IP - one not assigned to an active interface of any provided node"));
      Set<Ip> interfaceIps = new TreeSet<Ip>();
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         Configuration c = e.getValue();
         for (Interface i : c.getInterfaces().values()) {
            if (i.getActive()) {
               Prefix prefix = i.getPrefix();
               if (prefix != null) {
                  Ip ip = prefix.getAddress();
                  interfaceIps.add(ip);
               }
            }
         }
      }
      OrExpr dstIpMatchesSomeInterfaceIp = new OrExpr();
      for (Ip ip : interfaceIps) {
         EqExpr dstIpMatchesSpecificInterfaceIp = new EqExpr(new VarIntExpr(
               DST_IP_VAR), new LitIntExpr(ip));
         dstIpMatchesSomeInterfaceIp
               .addDisjunct(dstIpMatchesSpecificInterfaceIp);
      }
      NotExpr externalDstIp = new NotExpr(dstIpMatchesSomeInterfaceIp);
      RuleExpr externalDstIpRule = new RuleExpr(externalDstIp,
            ExternalDestinationIpExpr.INSTANCE);
      statements.add(externalDstIpRule);
      return statements;
   }

   private List<Statement> getExternalSrcIpRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment(
                  "Rule for matching external Source IP - one not assigned to an active interface of any provided node"));
      Set<Ip> interfaceIps = new TreeSet<Ip>();
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         Configuration c = e.getValue();
         for (Interface i : c.getInterfaces().values()) {
            if (i.getActive()) {
               Prefix prefix = i.getPrefix();
               if (prefix != null) {
                  Ip ip = prefix.getAddress();
                  interfaceIps.add(ip);
               }
            }
         }
      }
      OrExpr srcIpMatchesSomeInterfaceIp = new OrExpr();
      for (Ip ip : interfaceIps) {
         EqExpr srcIpMatchesSpecificInterfaceIp = new EqExpr(new VarIntExpr(
               SRC_IP_VAR), new LitIntExpr(ip));
         srcIpMatchesSomeInterfaceIp
               .addDisjunct(srcIpMatchesSpecificInterfaceIp);
      }
      NotExpr externalSrcIp = new NotExpr(srcIpMatchesSomeInterfaceIp);
      RuleExpr externalSrcIpRule = new RuleExpr(externalSrcIp,
            ExternalSourceIpExpr.INSTANCE);
      statements.add(externalSrcIpRule);
      return statements;
   }

   private List<Statement> getFlowSinkAcceptRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Post out flow sink interface leads to node accept"));
      for (FlowSinkInterface f : _flowSinks) {
         String hostname = f.getNode();
         String ifaceName = f.getInterface();
         if (isFlowSink(hostname, ifaceName)) {
            PostOutInterfaceExpr postOutIface = new PostOutInterfaceExpr(
                  hostname, ifaceName);
            NodeAcceptExpr nodeAccept = new NodeAcceptExpr(hostname);
            RuleExpr flowSinkAccept = new RuleExpr(postOutIface, nodeAccept);
            statements.add(flowSinkAccept);
         }
      }
      return statements;
   }

   private List<Statement> getMatchAclRules() {
      List<Statement> statements = new ArrayList<Statement>();
      Comment comment = new Comment("Rules for how packets can match acl lines");
      statements.add(comment);
      Map<String, Map<String, IpAccessList>> matchAcls = new TreeMap<String, Map<String, IpAccessList>>();
      // first we find out which acls we need to process
      for (String hostname : _topologyInterfaces.keySet()) {
         Map<String, IpAccessList> aclMap = new TreeMap<String, IpAccessList>();
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            if (iface.getPrefix() != null) {
               IpAccessList aclIn = iface.getIncomingFilter();
               IpAccessList aclOut = iface.getOutgoingFilter();
               PolicyMap routePolicy = iface.getRoutingPolicy();
               if (aclIn != null) {
                  String name = aclIn.getName();
                  aclMap.put(name, aclIn);
               }
               if (aclOut != null) {
                  String name = aclOut.getName();
                  aclMap.put(name, aclOut);
               }
               if (routePolicy != null) {
                  for (PolicyMapClause clause : routePolicy.getClauses()) {
                     for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                        if (matchLine.getType() == PolicyMapMatchType.IP_ACCESS_LIST) {
                           PolicyMapMatchIpAccessListLine matchAclLine = (PolicyMapMatchIpAccessListLine) matchLine;
                           for (IpAccessList acl : matchAclLine.getLists()) {
                              String name = acl.getName();
                              aclMap.put(name, acl);
                           }
                        }
                     }
                  }
               }
            }
         }
         if (aclMap.size() > 0) {
            matchAcls.put(hostname, aclMap);
         }
      }
      for (Entry<String, Map<String, IpAccessList>> e : matchAcls.entrySet()) {
         String hostname = e.getKey();
         Map<String, IpAccessList> aclMap = e.getValue();
         for (Entry<String, IpAccessList> e2 : aclMap.entrySet()) {
            String aclName = e2.getKey();
            IpAccessList acl = e2.getValue();
            List<IpAccessListLine> lines = acl.getLines();
            for (int i = 0; i < lines.size(); i++) {
               IpAccessListLine line = lines.get(i);
               // TODO: fix
               String invalidMessage = line.getInvalidMessage();
               boolean valid = invalidMessage == null;
               if (!valid) {
                  _warnings.add("WARNING: IpAccessList " + aclName + " line "
                        + i + ": disabled: " + invalidMessage + "\n");
               }

               Set<Prefix> srcIpRanges = line.getSourceIpRanges();
               Set<Prefix> dstIpRanges = line.getDestinationIpRanges();

               Set<IpProtocol> protocols = line.getProtocols();
               List<SubRange> srcPortRanges = line.getSrcPortRanges();
               List<SubRange> dstPortRanges = line.getDstPortRanges();

               AndExpr matchConditions = new AndExpr();

               // ** must not match previous rule **
               BooleanExpr prevNoMatch = (i > 0) ? new AclNoMatchExpr(hostname,
                     aclName, i - 1) : TrueExpr.INSTANCE;

               AndExpr matchLineCriteria = new AndExpr();
               matchConditions.addConjunct(matchLineCriteria);
               matchConditions.addConjunct(prevNoMatch);

               // match protocol
               if (protocols.size() > 0) {
                  OrExpr matchesSomeProtocol = new OrExpr();
                  for (IpProtocol protocol : protocols) {
                     int protocolNumber = protocol.number();
                     VarIntExpr protocolVar = new VarIntExpr(IP_PROTOCOL_VAR);
                     LitIntExpr protocolLit = new LitIntExpr(protocolNumber,
                           PROTOCOL_BITS);
                     EqExpr matchProtocol = new EqExpr(protocolVar, protocolLit);
                     matchesSomeProtocol.addDisjunct(matchProtocol);
                  }
                  matchLineCriteria.addConjunct(matchesSomeProtocol);
               }

               // match srcIp
               if (srcIpRanges.size() > 0) {
                  OrExpr matchSomeSrcIpRange = new OrExpr();
                  for (Prefix srcPrefix : srcIpRanges) {
                     long srcIp = srcPrefix.getAddress().asLong();

                     int srcIpWildcardBits = IP_BITS
                           - srcPrefix.getPrefixLength();
                     int srcIpStart = srcIpWildcardBits;
                     int srcIpEnd = IP_BITS - 1;
                     if (srcIpStart < IP_BITS) {
                        IntExpr extractsrcIp = newExtractExpr(SRC_IP_VAR,
                              srcIpStart, srcIpEnd);
                        LitIntExpr srcIpMatchLit = new LitIntExpr(srcIp,
                              srcIpStart, srcIpEnd);
                        EqExpr matchsrcIp = new EqExpr(extractsrcIp,
                              srcIpMatchLit);
                        matchSomeSrcIpRange.addDisjunct(matchsrcIp);
                     }
                     else {
                        matchSomeSrcIpRange.addDisjunct(TrueExpr.INSTANCE);
                     }
                  }
                  matchLineCriteria.addConjunct(matchSomeSrcIpRange);
               }

               // match dstIp
               if (dstIpRanges.size() > 0) {
                  OrExpr matchSomeDstIpRange = new OrExpr();
                  for (Prefix dstPrefix : dstIpRanges) {
                     long dstIp = dstPrefix.getAddress().asLong();

                     int dstIpWildcardBits = IP_BITS
                           - dstPrefix.getPrefixLength();
                     int dstIpStart = dstIpWildcardBits;
                     int dstIpEnd = IP_BITS - 1;
                     if (dstIpStart < IP_BITS) {
                        IntExpr extractDstIp = newExtractExpr(DST_IP_VAR,
                              dstIpStart, dstIpEnd);
                        LitIntExpr dstIpMatchLit = new LitIntExpr(dstIp,
                              dstIpStart, dstIpEnd);
                        EqExpr matchDstIp = new EqExpr(extractDstIp,
                              dstIpMatchLit);
                        matchSomeDstIpRange.addDisjunct(matchDstIp);
                     }
                     else {
                        matchSomeDstIpRange.addDisjunct(TrueExpr.INSTANCE);
                     }
                  }
                  matchLineCriteria.addConjunct(matchSomeDstIpRange);
               }

               // match srcport
               if (srcPortRanges != null && srcPortRanges.size() > 0) {
                  BooleanExpr matchSrcPort = getMatchAclRules_portHelper(
                        srcPortRanges, SRC_PORT_VAR);
                  matchLineCriteria.addConjunct(matchSrcPort);
               }

               // matchdstport
               if (dstPortRanges != null && dstPortRanges.size() > 0) {
                  BooleanExpr matchDstPort = getMatchAclRules_portHelper(
                        dstPortRanges, DST_PORT_VAR);
                  matchLineCriteria.addConjunct(matchDstPort);
               }

               AclMatchExpr match = new AclMatchExpr(hostname, aclName, i);

               RuleExpr matchRule = new RuleExpr(valid ? matchConditions
                     : FalseExpr.INSTANCE, match);
               statements.add(matchRule);

               // no match rule
               AndExpr noMatchConditions = new AndExpr();
               BooleanExpr noMatchLineCriteria = valid ? new NotExpr(
                     matchLineCriteria) : TrueExpr.INSTANCE;
               noMatchConditions.addConjunct(noMatchLineCriteria);
               noMatchConditions.addConjunct(prevNoMatch);
               AclNoMatchExpr noMatch = new AclNoMatchExpr(hostname, aclName, i);
               RuleExpr noMatchRule = new RuleExpr(noMatchConditions, noMatch);
               statements.add(noMatchRule);

               // permit/deny rule for match
               PolicyExpr aclAction;
               switch (line.getAction()) {
               case ACCEPT:
                  aclAction = new AclPermitExpr(hostname, aclName);
                  break;

               case REJECT:
                  aclAction = new AclDenyExpr(hostname, aclName);
                  break;

               default:
                  throw new Error("invalid action");
               }
               RuleExpr action = new RuleExpr(match, aclAction);
               statements.add(action);

            }
            // deny rule for not matching last line

            int lastLineIndex = acl.getLines().size() - 1;
            AclDenyExpr aclDeny = new AclDenyExpr(hostname, aclName);
            AclNoMatchExpr noMatchLast = new AclNoMatchExpr(hostname, aclName,
                  lastLineIndex);
            RuleExpr implicitDeny = new RuleExpr(noMatchLast, aclDeny);
            statements.add(implicitDeny);
         }
      }
      return statements;
   }

   private BooleanExpr getMatchAclRules_portHelper(List<SubRange> ranges,
         String portVar) {
      OrExpr or = new OrExpr();
      for (SubRange srcPortRange : ranges) {
         long low = srcPortRange.getStart();
         long high = srcPortRange.getEnd();
         if (low == high) {
            VarIntExpr portVarExpr = new VarIntExpr(portVar);
            LitIntExpr portLitExpr = new LitIntExpr(low, PORT_BITS);
            EqExpr exactMatch = new EqExpr(portVarExpr, portLitExpr);
            or.addDisjunct(exactMatch);
         }
         else {
            boolean doLE = (high < PORT_MAX);
            boolean doGE = (low > PORT_MIN);
            AndExpr and = new AndExpr();
            if (doGE) {
               BooleanExpr geExpr = bitvectorGEExpr(portVar, low, PORT_BITS);
               and.addConjunct(geExpr);
            }
            if (doLE) {
               BooleanExpr leExpr = bitvectorLEExpr(portVar, high, PORT_BITS);
               and.addConjunct(leExpr);
            }
            if (!doGE && !doLE) {
               // all ports match
               return TrueExpr.INSTANCE;
            }
            and.addConjunct(TrueExpr.INSTANCE);
            or.addDisjunct(and);
         }
      }
      or.addDisjunct(FalseExpr.INSTANCE);
      return or;
   }

   private List<Statement> getNodeAcceptToRoleAcceptRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Connect node_accept to role_accept"));
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String hostname = e.getKey();
         Configuration c = e.getValue();
         NodeAcceptExpr nodeAccept = new NodeAcceptExpr(hostname);
         RoleSet roles = c.getRoles();
         if (roles != null) {
            for (String role : roles) {
               RoleAcceptExpr roleAccept = new RoleAcceptExpr(role);
               RuleExpr rule = new RuleExpr(nodeAccept, roleAccept);
               statements.add(rule);
            }
         }
      }
      return statements;
   }

   public NodeSet getNodeSet() {
      NodeSet nodes = new NodeSet();
      nodes.addAll(_configurations.keySet());
      return nodes;
   }

   private List<Statement> getOriginateToPostInRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Connect originate to post_in"));
      for (String hostname : _configurations.keySet()) {
         OriginateExpr originate = new OriginateExpr(hostname);
         PostInExpr postIn = new PostInExpr(hostname);
         RuleExpr rule = new RuleExpr(originate, postIn);
         statements.add(rule);
      }
      return statements;
   }

   private List<Statement> getPolicyRouteRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Policy-based routing rules"));

      for (Entry<String, Set<Interface>> e : _topologyInterfaces.entrySet()) {
         String hostname = e.getKey();
         PreOutExpr preOut = new PreOutExpr(hostname);
         PolicyRouteFibIpMap ipMap = _prFibs.get(hostname);
         Set<Interface> interfaces = e.getValue();
         for (Interface iface : interfaces) {
            String ifaceName = iface.getName();
            PostInInterfaceExpr postInInterface = new PostInInterfaceExpr(
                  hostname, ifaceName);
            PolicyMap p = iface.getRoutingPolicy();
            if (p != null) {
               String policyName = p.getMapName();
               PolicyPermitExpr permit = new PolicyPermitExpr(hostname,
                     policyName);
               PolicyDenyExpr deny = new PolicyDenyExpr(hostname, policyName);

               List<PolicyMapClause> clauses = p.getClauses();
               for (int i = 0; i < clauses.size(); i++) {
                  PolicyMapClause clause = clauses.get(i);
                  PolicyMapAction action = clause.getAction();
                  PolicyMatchExpr match = new PolicyMatchExpr(hostname,
                        policyName, i);
                  PolicyNoMatchExpr noMatch = new PolicyNoMatchExpr(hostname,
                        policyName, i);
                  BooleanExpr prevNoMatch = (i > 0) ? new PolicyNoMatchExpr(
                        hostname, policyName, i - 1) : TrueExpr.INSTANCE;
                  /**
                   * If clause matches, and clause number (matched) is that of a
                   * permit clause, and out interface is among next hops, then
                   * policy permit on out interface
                   */
                  switch (action) {
                  case PERMIT:
                     for (PolicyMapSetLine setLine : clause.getSetLines()) {
                        if (setLine.getType() == PolicyMapSetType.NEXT_HOP) {
                           PolicyMapSetNextHopLine setNextHopLine = (PolicyMapSetNextHopLine) setLine;
                           for (Ip nextHopIp : setNextHopLine.getNextHops()) {
                              EdgeSet edges = ipMap.get(nextHopIp);
                              /**
                               * If packet reaches postin_interface on inInt,
                               * and preout, and inInt has policy, and policy
                               * matches on out interface, then preout_edge on
                               * out interface and corresponding in interface
                               *
                               */
                              for (Edge edge : edges) {
                                 String outInterface = edge.getInt1();
                                 String nextHop = edge.getNode2();
                                 String inInterface = edge.getInt2();
                                 if (!hostname.equals(edge.getNode1())) {
                                    throw new BatfishException("Invalid edge");
                                 }
                                 AndExpr forwardConditions = new AndExpr();
                                 forwardConditions.addConjunct(postInInterface);
                                 forwardConditions.addConjunct(preOut);
                                 forwardConditions.addConjunct(match);
                                 if (Util.isNullInterface(outInterface)) {
                                    NodeDropExpr nodeDrop = new NodeDropExpr(
                                          hostname);
                                    RuleExpr dropRule = new RuleExpr(
                                          forwardConditions, nodeDrop);
                                    statements.add(dropRule);
                                 }
                                 else {
                                    PreOutEdgeExpr preOutEdge = new PreOutEdgeExpr(
                                          hostname, outInterface, nextHop,
                                          inInterface);
                                    RuleExpr preOutEdgeRule = new RuleExpr(
                                          forwardConditions, preOutEdge);
                                    statements.add(preOutEdgeRule);
                                 }
                              }
                           }
                        }
                     }
                     RuleExpr permitRule = new RuleExpr(match, permit);
                     statements.add(permitRule);
                     break;
                  case DENY:
                     /**
                      * If clause matches and clause is deny clause, just deny
                      */
                     RuleExpr denyRule = new RuleExpr(match, deny);
                     statements.add(denyRule);
                     break;
                  default:
                     throw new Error("bad action");
                  }

                  /**
                   * For each clause, if we reach that clause, then if any acl
                   * in that clause permits, or there are no acls, clause, if
                   * the packet then the packet is matched by that clause.
                   *
                   * If all (at least one) acls deny, then the packed is not
                   * matched by that clause
                   *
                   * If there are no acls to match, then the packet is matched
                   * by that clause.
                   *
                   */
                  boolean hasMatchIp = false;
                  AndExpr allAclsDeny = new AndExpr();
                  OrExpr someAclPermits = new OrExpr();
                  for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                     if (matchLine.getType() == PolicyMapMatchType.IP_ACCESS_LIST) {
                        hasMatchIp = true;
                        PolicyMapMatchIpAccessListLine matchIpLine = (PolicyMapMatchIpAccessListLine) matchLine;
                        for (IpAccessList acl : matchIpLine.getLists()) {
                           String aclName = acl.getName();
                           AclDenyExpr currentAclDeny = new AclDenyExpr(
                                 hostname, aclName);
                           allAclsDeny.addConjunct(currentAclDeny);
                           AclPermitExpr currentAclPermit = new AclPermitExpr(
                                 hostname, aclName);
                           someAclPermits.addDisjunct(currentAclPermit);
                        }
                     }
                  }
                  AndExpr matchConditions = new AndExpr();
                  matchConditions.addConjunct(prevNoMatch);
                  if (hasMatchIp) {
                     /**
                      * no match if all acls deny
                      */
                     AndExpr noMatchConditions = new AndExpr();
                     noMatchConditions.addConjunct(prevNoMatch);
                     noMatchConditions.addConjunct(allAclsDeny);
                     RuleExpr noMatchRule = new RuleExpr(noMatchConditions,
                           noMatch);
                     statements.add(noMatchRule);

                     /**
                      * match if some acl permits
                      */
                     matchConditions.addConjunct(someAclPermits);
                  }
                  RuleExpr matchRule = new RuleExpr(matchConditions, match);
                  statements.add(matchRule);
               }
               /**
                * If the packet reaches the last clause, and is not matched by
                * that clause, then it is denied by the policy.
                */
               int lastIndex = p.getClauses().size() - 1;
               PolicyNoMatchExpr noMatchLast = new PolicyNoMatchExpr(hostname,
                     policyName, lastIndex);
               RuleExpr noMatchDeny = new RuleExpr(noMatchLast, deny);
               statements.add(noMatchDeny);
            }
         }
      }
      return statements;
   }

   private List<Statement> getPostInInterfaceToPostInRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules for connecting postInInterface to postIn"));
      for (Entry<String, Set<Interface>> e : _topologyInterfaces.entrySet()) {
         String hostname = e.getKey();
         Set<Interface> interfaces = e.getValue();
         for (Interface i : interfaces) {
            String ifaceName = i.getName();
            PostInInterfaceExpr postInIface = new PostInInterfaceExpr(hostname,
                  ifaceName);
            PostInExpr postIn = new PostInExpr(hostname);
            RuleExpr rule = new RuleExpr(postInIface, postIn);
            statements.add(rule);
         }
      }
      return statements;
   }

   private List<Statement> getPostInToNodeAcceptRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment("Rules for connecting post_in to node_accept"));
      for (Configuration c : _configurations.values()) {
         String hostname = c.getHostname();
         OrExpr someDstIpMatches = new OrExpr();
         for (Interface i : c.getInterfaces().values()) {
            Prefix prefix = i.getPrefix();
            if (prefix != null) {
               Ip ip = prefix.getAddress();
               EqExpr dstIpMatches = new EqExpr(new VarIntExpr(DST_IP_VAR),
                     new LitIntExpr(ip));
               someDstIpMatches.addDisjunct(dstIpMatches);
            }
         }
         PostInExpr postIn = new PostInExpr(hostname);
         NodeAcceptExpr nodeAccept = new NodeAcceptExpr(hostname);
         AndExpr conditions = new AndExpr();
         conditions.addConjunct(postIn);
         conditions.addConjunct(someDstIpMatches);
         RuleExpr rule = new RuleExpr(conditions, nodeAccept);
         statements.add(rule);
      }
      return statements;
   }

   private List<Statement> getPostInToPreOutRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment(
                  "postin ==> preout:",
                  "forward to preout if for each ip address on an interface, destination ip does not match"));
      for (Configuration c : _configurations.values()) {
         String hostname = c.getHostname();
         OrExpr someDstIpMatch = new OrExpr();
         for (Interface i : c.getInterfaces().values()) {
            Prefix prefix = i.getPrefix();
            if (prefix != null) {
               Ip ip = prefix.getAddress();
               EqExpr dstIpMatches = new EqExpr(new VarIntExpr(DST_IP_VAR),
                     new LitIntExpr(ip));
               someDstIpMatch.addDisjunct(dstIpMatches);
            }
         }
         NotExpr noDstIpMatch = new NotExpr(someDstIpMatch);
         PostInExpr postIn = new PostInExpr(hostname);
         PreOutExpr preOut = new PreOutExpr(hostname);
         AndExpr conditions = new AndExpr();
         conditions.addConjunct(postIn);
         conditions.addConjunct(noDstIpMatch);
         RuleExpr rule = new RuleExpr(conditions, preOut);
         statements.add(rule);
      }
      return statements;
   }

   private List<Statement> getPostOutIfaceToNodeTransitRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules connecting postout_iface to node_transit"));
      for (Entry<String, Set<Interface>> e : _topologyInterfaces.entrySet()) {
         String hostname = e.getKey();
         Set<Interface> interfaces = e.getValue();
         NodeTransitExpr nodeTransit = new NodeTransitExpr(hostname);
         for (Interface iface : interfaces) {
            String ifaceName = iface.getName();
            PostOutInterfaceExpr postOutIface = new PostOutInterfaceExpr(
                  hostname, ifaceName);
            RuleExpr rule = new RuleExpr(postOutIface, nodeTransit);
            statements.add(rule);
         }
      }
      return statements;
   }

   private List<Statement> getPreInInterfaceToPostInInterfaceRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment(
                  "Connect prein_interface to postin_interface, possibly through acl"));
      for (String hostname : _topologyInterfaces.keySet()) {
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            String ifaceName = iface.getName();
            if (ifaceName.startsWith(FAKE_INTERFACE_PREFIX)
                  || isFlowSink(hostname, ifaceName)) {
               continue;
            }
            NodeDropExpr nodeDrop = new NodeDropExpr(hostname);
            PreInInterfaceExpr preInIface = new PreInInterfaceExpr(hostname,
                  ifaceName);
            PostInInterfaceExpr postInIface = new PostInInterfaceExpr(hostname,
                  ifaceName);
            AndExpr conditions = new AndExpr();
            conditions.addConjunct(preInIface);
            IpAccessList inAcl = iface.getIncomingFilter();
            if (inAcl != null) {
               String aclName = inAcl.getName();
               AclPermitExpr aclPermit = new AclPermitExpr(hostname, aclName);
               conditions.addConjunct(aclPermit);
               AndExpr dropConditions = new AndExpr();
               AclDenyExpr aclDeny = new AclDenyExpr(hostname, aclName);
               dropConditions.addConjunct(preInIface);
               dropConditions.addConjunct(aclDeny);
               RuleExpr drop = new RuleExpr(dropConditions, nodeDrop);
               statements.add(drop);
            }
            RuleExpr preInToPostIn = new RuleExpr(conditions, postInIface);
            statements.add(preInToPostIn);
         }
      }
      return statements;
   }

   private List<Statement> getPreOutEdgeToPreOutInterfaceRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("PreOutEdge => PreOutInterface"));
      for (FlowSinkInterface f : _flowSinks) {
         String hostnameOut = f.getNode();
         String hostnameIn = NODE_NONE_NAME;
         String intOut = f.getInterface();
         String intIn = FLOW_SINK_TERMINATION_NAME;
         PreOutEdgeExpr preOutEdge = new PreOutEdgeExpr(hostnameOut, intOut,
               hostnameIn, intIn);
         PreOutInterfaceExpr preOutInt = new PreOutInterfaceExpr(hostnameOut,
               intOut);
         RuleExpr rule = new RuleExpr(preOutEdge, preOutInt);
         statements.add(rule);
      }
      for (Edge edge : _topologyEdges) {
         String hostnameOut = edge.getNode1();
         String hostnameIn = edge.getNode2();
         String intOut = edge.getInt1();
         String intIn = edge.getInt2();
         if (intIn.startsWith(FAKE_INTERFACE_PREFIX)
               || intOut.startsWith(FAKE_INTERFACE_PREFIX)) {
            continue;
         }
         PreOutEdgeExpr preOutEdge = new PreOutEdgeExpr(hostnameOut, intOut,
               hostnameIn, intIn);
         PreOutInterfaceExpr preOutInt = new PreOutInterfaceExpr(hostnameOut,
               intOut);
         RuleExpr rule = new RuleExpr(preOutEdge, preOutInt);
         statements.add(rule);
      }
      return statements;
   }

   private List<Statement> getPreOutInterfaceToPostOutInterfaceRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment(
                  "Connect preout_interface to postout_interface, possibly through acl"));
      for (String hostname : _topologyInterfaces.keySet()) {
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            String ifaceName = iface.getName();
            if (ifaceName.startsWith(FAKE_INTERFACE_PREFIX)) {
               continue;
            }
            NodeDropExpr nodeDrop = new NodeDropExpr(hostname);
            PreOutInterfaceExpr preOutIface = new PreOutInterfaceExpr(hostname,
                  ifaceName);
            PostOutInterfaceExpr postOutIface = new PostOutInterfaceExpr(
                  hostname, ifaceName);
            AndExpr conditions = new AndExpr();
            conditions.addConjunct(preOutIface);
            IpAccessList outAcl = iface.getOutgoingFilter();
            if (outAcl != null) {
               String aclName = outAcl.getName();
               AclPermitExpr aclPermit = new AclPermitExpr(hostname, aclName);
               conditions.addConjunct(aclPermit);
               AndExpr dropConditions = new AndExpr();
               AclDenyExpr aclDeny = new AclDenyExpr(hostname, aclName);
               dropConditions.addConjunct(preOutIface);
               dropConditions.addConjunct(aclDeny);
               RuleExpr drop = new RuleExpr(dropConditions, nodeDrop);
               statements.add(drop);
            }
            RuleExpr preOutToPostOut = new RuleExpr(conditions, postOutIface);
            statements.add(preOutToPostOut);
         }
      }
      return statements;
   }

   private List<Statement> getPreOutToDestRouteRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules for sending packets from preout to destroute stage"));
      for (String hostname : _configurations.keySet()) {
         /**
          * if a packet whose source node is a given node reaches preout on that
          * node, then it reaches destroute
          */
         PreOutExpr preOut = new PreOutExpr(hostname);
         OriginateExpr originate = new OriginateExpr(hostname);
         DestinationRouteExpr destRoute = new DestinationRouteExpr(hostname);
         AndExpr originConditions = new AndExpr();
         originConditions.addConjunct(preOut);
         originConditions.addConjunct(originate);
         RuleExpr originDestRoute = new RuleExpr(originConditions, destRoute);
         statements.add(originDestRoute);
      }
      for (Entry<String, Set<Interface>> e : _topologyInterfaces.entrySet()) {
         String hostname = e.getKey();
         PreOutExpr preOut = new PreOutExpr(hostname);
         DestinationRouteExpr destRoute = new DestinationRouteExpr(hostname);
         Set<Interface> interfaces = e.getValue();
         for (Interface i : interfaces) {
            String ifaceName = i.getName();
            /**
             * if a packet reaches postin_interface on interface, and interface
             * is not policy-routed, and it reaches preout, then it reaches
             * destroute
             */
            /**
             * if a packet reaches postin_interface on intefrace, and interface
             * is policy-routed by policy, and policy denies, and it reaches
             * preout, then it reaches destroute
             *
             */
            PostInInterfaceExpr postInInterface = new PostInInterfaceExpr(
                  hostname, ifaceName);
            AndExpr receivedDestRouteConditions = new AndExpr();
            receivedDestRouteConditions.addConjunct(postInInterface);
            receivedDestRouteConditions.addConjunct(preOut);
            PolicyMap policy = i.getRoutingPolicy();
            if (policy != null) {
               String policyName = policy.getMapName();
               PolicyDenyExpr policyDeny = new PolicyDenyExpr(hostname,
                     policyName);
               receivedDestRouteConditions.addConjunct(policyDeny);
            }
            RuleExpr receivedDestRoute = new RuleExpr(
                  receivedDestRouteConditions, destRoute);
            statements.add(receivedDestRoute);
         }
      }
      return statements;
   }

   private List<Statement> getRelDeclExprs(List<Statement> existingStatements) {
      List<Statement> statements = new ArrayList<Statement>();
      Comment header = new Comment("Relation declarations");
      statements.add(header);
      Set<String> relations = new TreeSet<String>();
      for (Statement existingStatement : existingStatements) {
         relations.addAll(existingStatement.getRelations());
      }
      relations.add(QueryRelationExpr.NAME);
      for (String packetRel : relations) {
         List<Integer> sizes = new ArrayList<Integer>();
         sizes.addAll(PACKET_VAR_SIZES.values());
         DeclareRelExpr declaration = new DeclareRelExpr(packetRel, sizes);
         statements.add(declaration);
      }
      return statements;
   }

   private List<Statement> getRoleOriginateToNodeOriginateRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules connecting role_originate to R_originate"));
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String hostname = e.getKey();
         Configuration c = e.getValue();
         OriginateExpr nodeOriginate = new OriginateExpr(hostname);
         RoleSet roles = c.getRoles();
         if (roles != null) {
            for (String role : roles) {
               RoleOriginateExpr roleOriginate = new RoleOriginateExpr(role);
               RuleExpr rule = new RuleExpr(roleOriginate, nodeOriginate);
               statements.add(rule);
            }
         }
      }
      return statements;
   }

   private List<Statement> getSane() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Make sure packet fields make sense"));
      EqExpr tcp = new EqExpr(new VarIntExpr(IP_PROTOCOL_VAR), new LitIntExpr(
            IpProtocol.TCP.number(), PROTOCOL_BITS));
      EqExpr udp = new EqExpr(new VarIntExpr(IP_PROTOCOL_VAR), new LitIntExpr(
            IpProtocol.UDP.number(), PROTOCOL_BITS));
      AndExpr noPortNumbers = new AndExpr();
      EqExpr noDstPort = new EqExpr(new VarIntExpr(DST_PORT_VAR),
            new LitIntExpr(0, PORT_BITS));
      EqExpr noSrcPort = new EqExpr(new VarIntExpr(SRC_PORT_VAR),
            new LitIntExpr(0, PORT_BITS));
      noPortNumbers.addConjunct(noDstPort);
      noPortNumbers.addConjunct(noSrcPort);
      OrExpr isSane = new OrExpr();
      isSane.addDisjunct(tcp);
      isSane.addDisjunct(udp);
      isSane.addDisjunct(noPortNumbers);
      RuleExpr rule = new RuleExpr(isSane, SaneExpr.INSTANCE);
      statements.add(rule);
      return statements;
   }

   private List<Statement> getToNeighborsRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Topology edge rules"));
      for (Edge edge : _topologyEdges) {
         String hostnameOut = edge.getNode1();
         String hostnameIn = edge.getNode2();
         String intOut = edge.getInt1();
         String intIn = edge.getInt2();
         if (intIn.startsWith(FAKE_INTERFACE_PREFIX)
               || isFlowSink(hostnameIn, intIn)
               || intOut.startsWith(FAKE_INTERFACE_PREFIX)
               || isFlowSink(hostnameOut, intOut)) {
            continue;
         }

         PostOutInterfaceExpr postOutIface = new PostOutInterfaceExpr(
               hostnameOut, intOut);
         PreOutEdgeExpr preOutEdge = new PreOutEdgeExpr(hostnameOut, intOut,
               hostnameIn, intIn);
         PreInInterfaceExpr preInIface = new PreInInterfaceExpr(hostnameIn,
               intIn);
         AndExpr conditions = new AndExpr();
         conditions.addConjunct(postOutIface);
         conditions.addConjunct(preOutEdge);
         RuleExpr propagateToAdjacent = new RuleExpr(conditions, preInIface);
         statements.add(propagateToAdjacent);
      }
      return statements;
   }

   public List<String> getWarnings() {
      return _warnings;
   }

   private boolean isFlowSink(String hostname, String ifaceName) {
      FlowSinkInterface f = new FlowSinkInterface(hostname, ifaceName);
      return _flowSinks.contains(f);
   }

   private void pruneInterfaces() {
      for (Configuration c : _configurations.values()) {
         Set<String> prunedInterfaces = new HashSet<String>();
         Map<String, Interface> interfaces = c.getInterfaces();
         for (Interface i : interfaces.values()) {
            String ifaceName = i.getName();
            if (!i.getActive() || ifaceName.startsWith(FAKE_INTERFACE_PREFIX)) {
               prunedInterfaces.add(ifaceName);
            }
         }
         for (String i : prunedInterfaces) {
            interfaces.remove(i);
         }
      }
   }

   public String synthesize() {
      List<Statement> statements = new ArrayList<Statement>();
      List<Statement> rules = new ArrayList<Statement>();
      List<Statement> varDecls = getVarDeclExprs();
      List<Statement> dropRules = getDropRules();
      List<Statement> acceptRules = getAcceptRules();
      List<Statement> sane = getSane();
      List<Statement> flowSinkAcceptRules = getFlowSinkAcceptRules();
      List<Statement> originateToPostInRules = getOriginateToPostInRules();
      List<Statement> postInInterfaceToPostInRules = getPostInInterfaceToPostInRules();
      List<Statement> postInToNodeAcceptRules = getPostInToNodeAcceptRules();
      List<Statement> postInToPreOutRules = getPostInToPreOutRules();
      List<Statement> preOutToDestRouteRules = getPreOutToDestRouteRules();
      List<Statement> destRouteToPreOutEdgeRules = getDestRouteToPreOutEdgeRules();
      List<Statement> preOutEdgeToPreOutInterfaceRules = getPreOutEdgeToPreOutInterfaceRules();
      List<Statement> policyRouteRules = getPolicyRouteRules();
      List<Statement> matchAclRules = getMatchAclRules();
      List<Statement> toNeighborsRules = getToNeighborsRules();
      List<Statement> preInInterfaceToPostInInterfaceRules = getPreInInterfaceToPostInInterfaceRules();
      List<Statement> preOutInterfaceToPostOutInterfaceRules = getPreOutInterfaceToPostOutInterfaceRules();
      List<Statement> nodeAcceptToRoleAcceptRules = getNodeAcceptToRoleAcceptRules();
      List<Statement> externalSrcIpRules = getExternalSrcIpRules();
      List<Statement> externalDstIpRules = getExternalDstIpRules();
      List<Statement> postOutIfaceToNodeTransitRules = getPostOutIfaceToNodeTransitRules();
      List<Statement> roleOriginateToNodeOriginateRules = getRoleOriginateToNodeOriginateRules();

      rules.addAll(dropRules);
      rules.addAll(acceptRules);
      rules.addAll(sane);
      rules.addAll(flowSinkAcceptRules);
      rules.addAll(originateToPostInRules);
      rules.addAll(postInInterfaceToPostInRules);
      rules.addAll(postInToNodeAcceptRules);
      rules.addAll(postInToPreOutRules);
      rules.addAll(preOutToDestRouteRules);
      rules.addAll(destRouteToPreOutEdgeRules);
      rules.addAll(preOutEdgeToPreOutInterfaceRules);
      rules.addAll(policyRouteRules);
      rules.addAll(matchAclRules);
      rules.addAll(toNeighborsRules);
      rules.addAll(preInInterfaceToPostInInterfaceRules);
      rules.addAll(preOutInterfaceToPostOutInterfaceRules);
      rules.addAll(nodeAcceptToRoleAcceptRules);
      rules.addAll(externalSrcIpRules);
      rules.addAll(externalDstIpRules);
      rules.addAll(postOutIfaceToNodeTransitRules);
      rules.addAll(roleOriginateToNodeOriginateRules);

      List<Statement> relDecls = getRelDeclExprs(rules);

      statements.addAll(varDecls);
      statements.addAll(relDecls);
      statements.addAll(rules);

      StringBuilder sb = new StringBuilder();
      for (Statement statement : statements) {
         if (_simplify) {
            Statement simplifiedStatement = statement.simplify();
            simplifiedStatement.print(sb, 0);
         }
         else {
            statement.print(sb, 0);
         }
         sb.append("\n");
      }

      String output = sb.toString();
      // hack to fix interface names with colons
      output = output.replace(":", "_COLON_");
      // hack to fix node: "(none)"
      output = output.replace("(none)", "_none_");
      return output;
   }

}
