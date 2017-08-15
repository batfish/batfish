package org.batfish.smt;


import com.microsoft.z3.*;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.*;
import org.batfish.datamodel.questions.smt.HeaderQuestion;

import java.util.*;


/**
 * <p>A class responsible for building a symbolic encoding of the
 * entire network. The encoder does this by maintaining a
 * collection of encoding <b>slices<b/>, where each slice
 * encodes the forwarding behavior for a particular packet.</p>
 *
 *><p>The encoder object is architected this way to allow for
 * modeling of features such as iBGP or non-local next-hop ip
 * addresses in static routes, where the forwarding behavior
 * of one packet depends on that of other packets.</p>
 *
 * <p>Symbolic variables that are common to all slices
 * are maintained in this class. That includes, for example,
 * the collection of variables representing topology failures.</p>
 *
 * @author Ryan Beckett
 */
public class Encoder {

    public static final boolean ENABLE_UNSAT_CORE = false;

    public static final boolean ENABLE_DEBUGGING = false;

    public static final String MAIN_SLICE_NAME = "SLICE-MAIN_";

    public static final boolean MODEL_EXTERNAL_COMMUNITIES = true;

    static final int DEFAULT_CISCO_VLAN_OSPF_COST = 1;

    private int _encodingId;

    private boolean _modelIgp;

    private HeaderQuestion _question;

    private Map<String, EncoderSlice> _slices;

    private Map<String, Map<String, BoolExpr>> _sliceReachability;

    private Encoder _previousEncoder;

    private SymbolicFailures _symbolicFailures;

    private Map<String, Expr> _allVariables;

    private List<SymbolicRecord> _allSymbolicRecords;

    private Graph _graph;

    private Context _ctx;

    private Solver _solver;

    private UnsatCore _unsatCore;

    /**
     * Create an encoder object that will consider all packets in the provided headerspace.
     * @param batfish  The Batfish object
     */
    public Encoder(IBatfish batfish, HeaderQuestion q) {
        this(new Graph(batfish), q);
    }

    /**
     * Create an encoder object that will consider all packets in the provided headerspace.
     * @param graph  The network graph
     */
    public Encoder(Graph graph, HeaderQuestion q) {
        this(null, graph, q, null, null, null, 0);
    }

    /**
     * Create an encoder object from an existing encoder.
     * @param e An existing encoder object
     * @param g An existing network graph
     */
    Encoder(Encoder e, Graph g) {
        this(e, g, e._question,  e.getCtx(), e.getSolver(), e.getAllVariables(), e.getId() + 1);
    }

    /**
     * Create an encoder object while possibly reusing the partial encoding
     * of another encoder. If the context and solver are null, then a new
     * encoder is created. Otherwise the old encoder is used.
     */
    private Encoder(Encoder enc, Graph graph, HeaderQuestion q, Context ctx, Solver solver, Map<String, Expr> vars, int id) {
        _graph = graph;
        _previousEncoder = enc;
        _modelIgp = true;
        _encodingId = id;
        _question = q;
        _slices = new HashMap<>();
        _sliceReachability = new HashMap<>();

        HashMap<String, String> cfg = new HashMap<>();

        // allows for unsat core when debugging
        if (ENABLE_UNSAT_CORE) {
            cfg.put("proof", "true");
            cfg.put("auto-config", "false");
        }


        _ctx = (ctx == null ? new Context(cfg) : ctx);

        if (solver == null) {
            if (ENABLE_UNSAT_CORE) {
                _solver = _ctx.mkSolver();
            } else {
                Tactic t1 = _ctx.mkTactic("simplify");
                Tactic t2 = _ctx.mkTactic("propagate-values");
                Tactic t3 = _ctx.mkTactic("solve-eqs");
                Tactic t4 = _ctx.mkTactic("smt");
                Tactic t = _ctx.then(t1, t2, t3, t4);
                _solver = _ctx.mkSolver(t);
                // System.out.println("Help: \n" + _solver.getHelp());
            }
        } else {
            _solver = solver;
        }

        // Set parameters
        Params p = _ctx.mkParams();
        p.add("ite_extra_rules", true);
        p.add("pull_cheap_ite", true);
        _solver.setParameters(p);

        _symbolicFailures = new SymbolicFailures();
        _allSymbolicRecords = new ArrayList<>();

        if (vars == null) {
            _allVariables = new HashMap<>();
        } else {
            _allVariables = vars;
        }

        if (ENABLE_DEBUGGING) {
            System.out.println(graph.toString());
        }

        _unsatCore = new UnsatCore(ENABLE_UNSAT_CORE);

        initConfigurations();
        initFailedLinkVariables();
        initSlices(_question.getHeaderSpace(), graph);
    }

    /*
     * Initialize any interface costs in OSPF
     */
    private void initConfigurations() {
        _graph.getConfigurations().forEach((router, conf) -> {
            initOspfInterfaceCosts(conf);
        });
    }

    /**
     * TODO:
     * This was copied from BdpDataPlanePlugin.java
     * to initialize the OSPF inteface costs
     */
    private void initOspfInterfaceCosts(Configuration conf) {
        if (conf.getDefaultVrf().getOspfProcess() != null) {
            conf.getInterfaces().forEach((interfaceName, i) -> {
                if (i.getActive()) {
                    Integer ospfCost = i.getOspfCost();
                    if (ospfCost == null) {
                        if (interfaceName.startsWith("Vlan")) {
                            // TODO: fix for non-cisco
                            ospfCost = DEFAULT_CISCO_VLAN_OSPF_COST;
                        } else {
                            if (i.getBandwidth() != null) {
                                ospfCost = Math.max((int) (conf.getDefaultVrf().getOspfProcess()
                                                               .getReferenceBandwidth() / i
                                        .getBandwidth()), 1);
                            } else {
                                throw new BatfishException("Expected non-null interface " +
                                        "bandwidth" + " for \"" + conf.getHostname() + "\":\"" +
                                        interfaceName + "\"");
                            }
                        }
                    }
                    i.setOspfCost(ospfCost);
                }
            });
        }
    }

    /*
     * Initialize symbolic variables to represent link failures.
     */
    private void initFailedLinkVariables() {
        _graph.getEdgeMap().forEach((router, edges) -> {
            for (GraphEdge ge : edges) {
                if (ge.getPeer() == null) {
                    Interface i = ge.getStart();
                    String name = getId() + "_FAILED-EDGE_" + ge.getRouter() + "_" + i.getName();
                    ArithExpr var = getCtx().mkIntConst(name);
                    _symbolicFailures.getFailedEdgeLinks().put(ge, var);
                    _allVariables.put(var.toString(), var);
                }
            }
        });
        _graph.getNeighbors().forEach((router, peers) -> {
            for (String peer : peers) {
                // sort names for unique
                String pair = (router.compareTo(peer) < 0 ? router + "_" + peer : peer + "_" + router);
                String name = getId() + "_FAILED-EDGE_" + pair;
                ArithExpr var = _ctx.mkIntConst(name);
                _symbolicFailures.getFailedInternalLinks().put(router, peer, var);
                _allVariables.put(var.toString(), var);
            }
        });
    }

    /*
     * Initialize each encoding slice.
     * For iBGP, we also add reachability information for each pair of neighbors,
     * to determine if messages sent to/from a neighbor will arrive.
     */
    private void initSlices(HeaderSpace h, Graph g) {
        if (g.getIbgpNeighbors().isEmpty() || !_modelIgp) {
            _slices.put(MAIN_SLICE_NAME, new EncoderSlice(this, h, g, ""));
        } else {
            _slices.put(MAIN_SLICE_NAME, new EncoderSlice(this, h, g, MAIN_SLICE_NAME));
        }

        if (_modelIgp) {
            SortedSet<Pair<String, Ip>> ibgpRouters = new TreeSet<>();

            g.getIbgpNeighbors().forEach((ge, n) -> {

                String router = ge.getRouter();
                Ip ip = n.getLocalIp();
                Pair<String, Ip> pair = new Pair<>(router, ip);

                // Add one slice per (router, source ip) pair
                if (!ibgpRouters.contains(pair)) {

                    ibgpRouters.add(pair);

                    // Create a control plane slice only for this ip
                    HeaderSpace hs = new HeaderSpace();

                    // Make sure messages are sent to this destination IP
                    SortedSet<IpWildcard> ips = new TreeSet<>();
                    ips.add(new IpWildcard(n.getLocalIp()));
                    hs.setDstIps(ips);

                    // Make sure messages use TCP port 179
                    SortedSet<SubRange> dstPorts = new TreeSet<>();
                    dstPorts.add(new SubRange(179,179));
                    hs.setDstPorts(dstPorts);

                    // Make sure messages use the TCP protocol
                    SortedSet<IpProtocol> protocols = new TreeSet<>();
                    protocols.add(IpProtocol.TCP);
                    hs.setIpProtocols(protocols);

                    String sliceName = "SLICE-" + router + "_";

                    EncoderSlice slice = new EncoderSlice(this, hs, g, sliceName);
                    _slices.put(sliceName, slice);

                    PropertyAdder pa = new PropertyAdder(slice);
                    Map<String, BoolExpr> reachVars = pa.instrumentReachability(router);
                    _sliceReachability.put(router, reachVars);
                }
            });
        }

    }

    // Create a symbolic boolean
    BoolExpr Bool(boolean val) {
        return getCtx().mkBool(val);
    }

    // Symbolic boolean negation
    BoolExpr Not(BoolExpr e) {
        return getCtx().mkNot(e);
    }

    // Symbolic boolean disjunction
    BoolExpr Or(BoolExpr... vals) {
        return getCtx().mkOr(vals);
    }

    // Symbolic boolean implication
    BoolExpr Implies(BoolExpr e1, BoolExpr e2) {
        return getCtx().mkImplies(e1, e2);
    }

    // Symbolic boolean conjunction
    BoolExpr And(BoolExpr... vals) {
        return getCtx().mkAnd(vals);
    }

    // Symbolic true value
    BoolExpr True() {
        return getCtx().mkBool(true);
    }

    // Symbolic false value
    BoolExpr False() {
        return getCtx().mkBool(false);
    }

    // Symbolic arithmetic less than
    BoolExpr Lt(Expr e1, Expr e2) {
        if (e1 instanceof BoolExpr && e2 instanceof BoolExpr) {
            return And((BoolExpr) e2, Not((BoolExpr) e1));
        }
        if (e1 instanceof ArithExpr && e2 instanceof ArithExpr) {
            return getCtx().mkLt((ArithExpr) e1, (ArithExpr) e2);
        }
        if (e1 instanceof BitVecExpr && e2 instanceof BitVecExpr) {
            return getCtx().mkBVULT((BitVecExpr) e1, (BitVecExpr) e2);
        }
        throw new BatfishException("Invalid call to Lt while encoding control plane");
    }

    // Symbolic greater than
    BoolExpr Gt(Expr e1, Expr e2) {
        if (e1 instanceof BoolExpr && e2 instanceof BoolExpr) {
            return And((BoolExpr) e1, Not((BoolExpr) e2));
        }
        if (e1 instanceof ArithExpr && e2 instanceof ArithExpr) {
            return getCtx().mkGt((ArithExpr) e1, (ArithExpr) e2);
        }
        if (e1 instanceof BitVecExpr && e2 instanceof BitVecExpr) {
            return getCtx().mkBVUGT((BitVecExpr) e1, (BitVecExpr) e2);
        }
        throw new BatfishException("Invalid call the Le while encoding control plane");
    }

    // Symbolic arithmetic subtraction
    ArithExpr Sub(ArithExpr e1, ArithExpr e2) {
        return getCtx().mkSub(e1, e2);
    }

    // Symbolic if-then-else for booleans
    BoolExpr If(BoolExpr cond, BoolExpr case1, BoolExpr case2) {
        return (BoolExpr) getCtx().mkITE(cond, case1, case2);
    }

    // Symbolic if-then-else for arithmetic
    ArithExpr If(BoolExpr cond, ArithExpr case1, ArithExpr case2) {
        return (ArithExpr) getCtx().mkITE(cond, case1, case2);
    }

    // Create a symbolic integer
    ArithExpr Int(long l) {
        return getCtx().mkInt(l);
    }

    // Symbolic arithmetic addition
    ArithExpr Sum(ArithExpr e1, ArithExpr e2) {
        return getCtx().mkAdd(e1, e2);
    }

    // Symbolic greater than or equal to
    BoolExpr Ge(Expr e1, Expr e2) {
        if (e1 instanceof ArithExpr && e2 instanceof ArithExpr) {
            return getCtx().mkGe((ArithExpr) e1, (ArithExpr) e2);
        }
        if (e1 instanceof BitVecExpr && e2 instanceof BitVecExpr) {
            return getCtx().mkBVUGE((BitVecExpr) e1, (BitVecExpr) e2);
        }
        throw new BatfishException("Invalid call the Le while encoding control plane");
    }

    // Symbolic less than or equal to
    BoolExpr Le(Expr e1, Expr e2) {
        if (e1 instanceof ArithExpr && e2 instanceof ArithExpr) {
            return getCtx().mkLe((ArithExpr) e1, (ArithExpr) e2);
        }
        if (e1 instanceof BitVecExpr && e2 instanceof BitVecExpr) {
            return getCtx().mkBVULE((BitVecExpr) e1, (BitVecExpr) e2);
        }
        throw new BatfishException("Invalid call the Le while encoding control plane");
    }

    // Symblic equality of expressions
    BoolExpr Eq(Expr e1, Expr e2) {
        return getCtx().mkEq(e1, e2);
    }

    // Add a boolean variable to the model
    void add(BoolExpr e) {
        _unsatCore.track(_solver, _ctx, e);
    }

    /*
     * Adds the constraint that at most k links have failed.
     * This is done in two steps. First we ensure that each link
     * variable is constrained to take on a value between 0 and 1:
     *
     * 0 <= link_i <= 1
     *
     * Then we ensure that the sum of all links is never more than k:
     *
     * link_1 + link_2 + ... + link_n <= k
     */
    private void addFailedConstraints(int k) {
        Set<ArithExpr> vars = new HashSet<>();
        getSymbolicFailures().getFailedInternalLinks().forEach((router, peer, var) -> vars.add(var));
        getSymbolicFailures().getFailedEdgeLinks().forEach((ge, var) -> vars.add(var));

        ArithExpr sum = Int(0);
        for (ArithExpr var : vars) {
            sum = Sum(sum, var);
            add(Ge(var, Int(0)));
            add(Le(var, Int(1)));
        }
        if (k == 0) {
            for (ArithExpr var : vars) {
                add(Eq(var, Int(0)));
            }
        } else {
            add(Le(sum, Int(k)));
        }
    }

    /*
     * Check if a community value should be displayed to the human
     */
    private boolean displayCommunity(CommunityVar cvar) {
        if (cvar.getType() == CommunityVar.Type.OTHER) {
            return false;
        }
        if (cvar.getType() == CommunityVar.Type.EXACT) {
            return true;
        }
        return true; //!StringUtils.containsAny(cvar.getValue(), "$^*+[()]");

    }

    /*
     * Add the relevant variables in the counterexample to
     * display to the user in a human-readable fashion
     */
    private void buildCounterExample(Encoder enc, Model m, SortedMap<String, String> model, SortedMap<String, String> packetModel, SortedSet<String> fwdModel, SortedMap<String, SortedMap<String, String>> envModel, SortedSet<String> failures) {
        SortedMap<Expr, String> valuation = new TreeMap<>();

        // If user asks for the full model
        _allVariables.forEach((name, e) -> {
            Expr val = m.evaluate(e, false);
            if (!val.equals(e)) {
                String s = val.toString();
                if (_question.getFullModel()) {
                    model.put(name, s);
                }
                valuation.put(e, s);
            }
        });

        // Packet model
        SymbolicPacket p = enc.getMainSlice().getSymbolicPacket();
        String dstIp = valuation.get(p.getDstIp());
        String srcIp = valuation.get(p.getSrcIp());
        String dstPt = valuation.get(p.getDstPort());
        String srcPt = valuation.get(p.getSrcPort());
        String icmpCode = valuation.get(p.getIcmpCode());
        String icmpType = valuation.get(p.getIcmpType());
        String ipProtocol = valuation.get(p.getIpProtocol());
        String tcpAck = valuation.get(p.getTcpAck());
        String tcpCwr = valuation.get(p.getTcpCwr());
        String tcpEce = valuation.get(p.getTcpEce());
        String tcpFin = valuation.get(p.getTcpFin());
        String tcpPsh = valuation.get(p.getTcpPsh());
        String tcpRst = valuation.get(p.getTcpRst());
        String tcpSyn = valuation.get(p.getTcpSyn());
        String tcpUrg = valuation.get(p.getTcpUrg());

        Ip dip = new Ip(Long.parseLong(dstIp));
        Ip sip = new Ip(Long.parseLong(srcIp));

        packetModel.put("dstIp", dip.toString());

        if (sip.asLong() != 0) {
            packetModel.put("srcIp", sip.toString());
        }
        if (dstPt != null && !dstPt.equals("0")) {
            packetModel.put("dstPort", dstPt);
        }
        if (srcPt != null && !srcPt.equals("0")) {
            packetModel.put("srcPort", srcPt);
        }
        if (icmpCode != null && !icmpCode.equals("0")) {
            packetModel.put("icmpCode", icmpCode);
        }
        if (icmpType != null && !icmpType.equals("0")) {
            packetModel.put("icmpType", icmpType);
        }
        if (ipProtocol != null && !ipProtocol.equals("0")) {
            Integer number = Integer.parseInt(ipProtocol);
            IpProtocol proto = IpProtocol.fromNumber(number);
            packetModel.put("protocol", proto.toString());
        }
        if (tcpAck != null && tcpAck.equals("true")) {
            packetModel.put("tcpAck", "set");
        }
        if (tcpCwr != null && tcpCwr.equals("true")) {
            packetModel.put("tcpCwr", "set");
        }
        if (tcpEce != null && tcpEce.equals("true")) {
            packetModel.put("tcpEce", "set");
        }
        if (tcpFin != null && tcpFin.equals("true")) {
            packetModel.put("tcpFin", "set");
        }
        if (tcpPsh != null && tcpPsh.equals("true")) {
            packetModel.put("tcpPsh", "set");
        }
        if (tcpRst != null && tcpRst.equals("true")) {
            packetModel.put("tcpRst", "set");
        }
        if (tcpSyn != null && tcpSyn.equals("true")) {
            packetModel.put("tcpSyn", "set");
        }
        if (tcpUrg != null && tcpUrg.equals("true")) {
            packetModel.put("tcpUrg", "set");
        }

        enc.getSlices().forEach((name, slice) -> {

            slice.getLogicalGraph().getEnvironmentVars().forEach((lge, r) -> {

                if (valuation.get(r.getPermitted()).equals("true")) {
                    SortedMap<String, String> recordMap = new TreeMap<>();
                    GraphEdge ge = lge.getEdge();
                    String nodeIface = ge.getRouter() + "," + ge.getStart().getName() + " (BGP)";
                    envModel.put(nodeIface, recordMap);
                    if (r.getPrefixLength() != null) {
                        String x = valuation.get(r.getPrefixLength());
                        if (x != null) {
                            int len = Integer.parseInt(x);
                            Prefix p1 = new Prefix(dip, len);
                            Prefix p2 = p1.getNetworkPrefix();
                            recordMap.put("prefix", p2.toString());
                        }
                    }
                    if (r.getAdminDist() != null) {
                        String x = valuation.get(r.getAdminDist());
                        if (x != null) {
                            recordMap.put("admin distance", x);
                        }
                    }
                    if (r.getLocalPref() != null) {
                        String x = valuation.get(r.getLocalPref());
                        if (x != null) {
                            recordMap.put("local preference", x);
                        }
                    }
                    if (r.getMetric() != null) {
                        String x = valuation.get(r.getMetric());
                        if (x != null) {
                            recordMap.put("protocol metric", x);
                        }
                    }
                    if (r.getMed() != null) {
                        String x = valuation.get(r.getMed());
                        if (x != null) {
                            recordMap.put("multi-exit disc.", valuation.get(r.getMed()));
                        }
                    }
                    if (r.getOspfArea() != null && r.getOspfArea().getBitVec() != null) {
                        String x = valuation.get(r.getOspfArea().getBitVec());
                        if (x != null) {
                            Integer i = Integer.parseInt(x);
                            Long area = r.getOspfArea().value(i);
                            recordMap.put("OSPF Area", area.toString());
                        }
                    }
                    if (r.getOspfType() != null && r.getOspfType().getBitVec() != null) {
                        String x = valuation.get(r.getOspfType().getBitVec());
                        if (x != null) {
                            Integer i = Integer.parseInt(x);
                            OspfType type = r.getOspfType().value(i);
                            recordMap.put("OSPF Type", type.toString());
                        }
                    }

                    r.getCommunities().forEach((cvar, e) -> {
                        String c = valuation.get(e);
                        // TODO: what about OTHER type?
                        if (c != null && c.equals("true")) {
                            if (displayCommunity(cvar)) {
                                String s = cvar.getValue();
                                String t = slice.getNamedCommunities().get(cvar.getValue());
                                s = (t == null ? s : t);
                                recordMap.put("community " + s, "");
                            }
                        }
                    });
                }
            });

        });

        // Forwarding Model
        enc.getMainSlice().getSymbolicDecisions().getDataForwarding().forEach((router, edge, e) -> {
            String s = valuation.get(e);
            if (s != null && s.equals("true")) {
                fwdModel.add(edge.toString());
            }
        });

        _symbolicFailures.getFailedInternalLinks().forEach((x, y, e) -> {
            String s = valuation.get(e);
            if (s != null && s.equals("1")) {
                String pair = (x.compareTo(y) < 0 ? x + "," + y : y + "," + x);
                failures.add("link(" + pair + ")");
            }
        });
        _symbolicFailures.getFailedEdgeLinks().forEach((ge, e) -> {
            String s = valuation.get(e);
            if (s != null && s.equals("1")) {
                failures.add("link(" + ge.getRouter() + "," + ge.getStart().getName() + ")");
            }
        });
    }

    /*
     * Generate a blocking clause for the encoding that says that one
     * of the environments that was true before must now be false.
     */
    private BoolExpr environmentBlockingClause(Model m) {
        BoolExpr acc1 = False();
        BoolExpr acc2 = True();

        // Disable an environment edge if possible
        Map<LogicalEdge, SymbolicRecord> map = getMainSlice().getLogicalGraph().getEnvironmentVars();
        for (Map.Entry<LogicalEdge, SymbolicRecord> entry : map.entrySet()) {
            SymbolicRecord record = entry.getValue();
            BoolExpr per = record.getPermitted();
            Expr x = m.evaluate(per, false);
            if (x.toString().equals("true")) {
                acc1 = Or(acc1, Not(per));
            } else {
                acc2 = And(acc2, Not(per));
            }
        }

        // Disable a community value if possible
        for (Map.Entry<LogicalEdge, SymbolicRecord> entry : map.entrySet()) {
            SymbolicRecord record = entry.getValue();
            for (Map.Entry<CommunityVar, BoolExpr> centry : record.getCommunities().entrySet()) {
                BoolExpr comm = centry.getValue();
                Expr x = m.evaluate(comm, false);
                if (x.toString().equals("true")) {
                    acc1 = Or(acc1, Not(comm));
                } else {
                    acc2 = And(acc2, Not(comm));
                }
            }
        }

        return And(acc1,acc2);
    }


    /**
     * <p>Checks that a property is always true by seeing if the encoding
     * is unsatisfiable. If the model is satisfiable, then there is a
     * counter example to the property.</p>
     *
     * @return A VerificationResult indicating the status of the check.
     */
    public VerificationResult verify() {

        EncoderSlice mainSlice = _slices.get(MAIN_SLICE_NAME);

        int numVariables = _allVariables.size();
        int numConstraints = _solver.getAssertions().length;
        int numNodes = mainSlice.getGraph().getConfigurations().size();
        int numEdges = 0;
        for (Map.Entry<String, Set<String>> e : mainSlice.getGraph().getNeighbors().entrySet()) {
            numEdges += e.getValue().size();
        }

        long start = System.currentTimeMillis();
        Status status = _solver.check();
        long time = System.currentTimeMillis() - start;

        VerificationStats stats = new VerificationStats(numNodes, numEdges, numVariables,
                numConstraints, time);

        if (ENABLE_DEBUGGING) {
            System.out.println("Constraints: " + stats.getNumConstraints());
            System.out.println("Variables: " + stats.getNumVariables());
            System.out.println("Z3 Time: " + stats.getTime());
        }

        if (status == Status.UNSATISFIABLE) {
            return new VerificationResult(true, null, null, null, null, null);
        } else if (status == Status.UNKNOWN) {
            throw new BatfishException("ERROR: satisfiability unknown");
        } else {
            VerificationResult result;

            while (true) {
                Model m = _solver.getModel();
                SortedMap<String, String> model = new TreeMap<>();
                SortedMap<String, String> packetModel = new TreeMap<>();
                SortedSet<String> fwdModel = new TreeSet<>();
                SortedMap<String, SortedMap<String, String>> envModel = new TreeMap<>();
                SortedSet<String> failures = new TreeSet<>();
                buildCounterExample(this, m, model, packetModel, fwdModel, envModel, failures);
                if (_previousEncoder != null) {
                    buildCounterExample(_previousEncoder, m, model, packetModel, fwdModel, envModel, failures);
                }

                result = new VerificationResult(false, model, packetModel, envModel, fwdModel, failures);

                if (!_question.getMinimize()) {
                    break;
                }

                BoolExpr blocking = environmentBlockingClause(m);
                add(blocking);

                Status s = _solver.check();
                if (s == Status.UNSATISFIABLE) {
                    break;
                }
                if (s == Status.UNKNOWN) {
                    throw new BatfishException("ERROR: satisfiability unknown");
                }
            }

            return result;
        }
    }

    /**
     * <p>Adds all the constraints to capture the interactions of messages
     * among all protocols in the network. This should be called prior
     * to calling the <b>verify method</b></p>
     */
    public void computeEncoding() {
        addFailedConstraints(_question.getFailures());
        getMainSlice().computeEncoding();
        _slices.forEach((name, slice) -> {
            if (!name.equals(MAIN_SLICE_NAME)) {
                slice.computeEncoding();
            }
        });
    }

    /*
     * Getters and setters
     */

    public SymbolicFailures getSymbolicFailures() {
        return _symbolicFailures;
    }

    public EncoderSlice getSlice(String router) {
        String s = "SLICE-" + router + "_";
        return _slices.get(s);
    }

    public Context getCtx() {
        return _ctx;
    }

    public EncoderSlice getMainSlice() {
        return _slices.get(MAIN_SLICE_NAME);
    }

    public Solver getSolver() {
        return _solver;
    }

    public Map<String, Expr> getAllVariables() {
        return _allVariables;
    }

    public boolean getNoEnvironment() {
        return _question.getNoEnvironment();
    }

    public int getId() {
        return _encodingId;
    }

    public boolean getModelIgp() {
        return _modelIgp;
    }

    public Map<String, Map<String, BoolExpr>> getSliceReachability() {
        return _sliceReachability;
    }

    public List<SymbolicRecord> getAllSymbolicRecords() {
        return _allSymbolicRecords;
    }

    public UnsatCore getUnsatCore() {
        return _unsatCore;
    }

    public int getFailures() {
        return _question.getFailures();
    }

    public boolean getFullModel() {
        return _question.getFullModel();
    }

    public Map<String, EncoderSlice> getSlices() {
        return _slices;
    }

    public HeaderQuestion getQuestion() {
        return _question;
    }

    public void setQuestion(HeaderQuestion _question) {
        this._question = _question;
    }
}
