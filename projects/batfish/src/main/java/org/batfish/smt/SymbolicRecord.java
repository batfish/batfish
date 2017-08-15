package org.batfish.smt;

import com.microsoft.z3.*;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>A symbolic record of control plane message attributes.
 * Attributes are specialized based on the protocol, and
 * which optimizations are applicable to the network. </p>
 *
 * @author Ryan Beckett
 */
class SymbolicRecord {

    private String _name;

    private Protocol _proto;

    private EncoderSlice _enc;

    private boolean _isUsed;

    private boolean _isEnv;

    private boolean _isBest;

    private boolean _isBestOverall;

    private boolean _isExport;

    private BoolExpr _permitted;

    private ArithExpr _prefixLength;

    private ArithExpr _adminDist;

    private ArithExpr _metric;

    private ArithExpr _med;

    private ArithExpr _localPref;

    private BoolExpr _bgpInternal;

    private ArithExpr _igpMetric;

    private SymbolicOriginatorId _clientId;

    private SymbolicEnum<Long> _ospfArea;

    private SymbolicOspfType _ospfType;

    private ArithExpr _routerId;

    private SymbolicEnum<Protocol> _protocolHistory;

    private Map<CommunityVar, BoolExpr> _communities;


    SymbolicRecord() {
        _name = null;
        _proto = null;
        _enc = null;
        _isUsed = false;
        _isBest = false;
        _isBestOverall = false;
        _isEnv = false;
        _isExport = false;
        _prefixLength = null;
        _metric = null;
        _adminDist = null;
        _med = null;
        _localPref = null;
        _bgpInternal = null;
        _clientId = null;
        _igpMetric = null;
        _routerId = null;
        _permitted = null;
        _ospfType = null;
        _protocolHistory = null;
    }

    SymbolicRecord(String name, Protocol proto) {
        _name = name;
        _proto = proto;
        _enc = null;
        _isUsed = false;
        _isBest = false;
        _isBestOverall = false;
        _isEnv = false;
        _isExport = false;
        _prefixLength = null;
        _metric = null;
        _adminDist = null;
        _med = null;
        _localPref = null;
        _bgpInternal = null;
        _clientId = null;
        _igpMetric = null;
        _routerId = null;
        _permitted = null;
        _ospfArea = null;
        _ospfType = null;
        _protocolHistory = null;
    }

    /*
     * Copy constructor used to create the SSA form.
     * To avoid changing values along different If branches,
     * we must create a new copy of several variables that can change.
     */
    SymbolicRecord(SymbolicRecord other) {
        _name = other._name;
        _proto = other._proto;
        _enc = other._enc;
        _isUsed = other._isUsed;
        _isBest = other._isBest;
        _isBestOverall = other._isBestOverall;
        _isEnv = other._isEnv;
        _isExport = other._isExport;
        _prefixLength = other._prefixLength;
        _metric = other._metric;
        _adminDist = other._adminDist;
        _med = other._med;
        _localPref = other._localPref;
        _bgpInternal = other._bgpInternal;
        _clientId = other._clientId;
        _igpMetric = other._igpMetric;
        _routerId = other._routerId;
        _permitted = other._permitted;
        _ospfArea = other._ospfArea;
        _ospfType = (other._ospfType == null ? null : new SymbolicOspfType(other._ospfType));
        _protocolHistory = other._protocolHistory;
        _communities = new HashMap<>(other._communities); // TODO: use a persistent map to avoid this penalty
    }


    SymbolicRecord(
            EncoderSlice enc, String name, String router, Protocol proto,
            Optimizations opts, SymbolicEnum<Protocol> h, boolean isAbstract) {

        _name = name;
        _proto = proto;
        _enc = enc;
        _isUsed = true;
        _isExport = _name.contains("EXPORT");
        _isEnv = _name.contains("_ENV-");
        _isBest = _name.contains("_BEST");
        _isBestOverall = (_isBest && _name.contains("_OVERALL"));

        Context ctx = enc.getCtx();

        boolean hasOspf = enc.getProtocols().get(router).contains(Protocol.OSPF);
        boolean hasBgp = enc.getProtocols().get(router).contains(Protocol.BGP);
        boolean multipleProtos = enc.getProtocols().get(router).size() > 1;
        boolean modelAd = !_isEnv && ((_isBestOverall && multipleProtos) || opts.getKeepAdminDist());
        boolean modelLp = !_isEnv && opts.getKeepLocalPref() && (proto.isBgp() || _isBestOverall);
        boolean modelIbgp = (opts.getNeedBgpInternal().contains(router));

        _protocolHistory = h;
        _ospfArea = null;
        _ospfType = null;
        _igpMetric = null;


        if (proto.isBest()) {
            _metric = ctx.mkIntConst(_name + "_metric");
            _localPref = (modelLp ? ctx.mkIntConst(_name + "_localPref") : null);
            _adminDist = (modelAd ? ctx.mkIntConst(_name + "_adminDist") : null);
            _med = (opts.getKeepMed() ? ctx.mkIntConst(_name + "_med") : null);
            _bgpInternal = (modelIbgp ? ctx.mkBoolConst(_name + "_bgpInternal") : null);
            _igpMetric = (modelIbgp ? ctx.mkIntConst(_name + "_igpMetric") : null);

            if (hasOspf && opts.getKeepOspfType()) {
                _ospfType = new SymbolicOspfType(enc, _name + "_ospfType");
            }

            // Set OSPF area only for best OSPF or OVERALL choice
            if (hasOspf && (_isBestOverall || _name.contains("_OSPF_"))) {
                List<Long> areaIds = new ArrayList<>(enc.getGraph().getAreaIds().get(router));
                if (areaIds.size() > 1) {
                    _ospfArea = new SymbolicEnum<>(enc, areaIds, _name + "_ospfArea");
                }
            }

        } else if (proto.isConnected()) {
            _metric = null;
            _localPref = null;
            _adminDist = null;
            _med = null;
            _bgpInternal = null;
            _ospfArea = null;
            _ospfType = null;

        } else if (proto.isStatic()) {
            _metric = null;
            _localPref = null;
            _adminDist = null;
            _med = null;
            _bgpInternal = null;
            _ospfArea = null;
            _ospfType = null;

        } else if (proto.isBgp()) {
            _metric = ctx.mkIntConst(_name + "_metric");
            _localPref = (modelLp ? ctx.mkIntConst(_name + "_localPref") : null);
            _adminDist = (modelAd ? ctx.mkIntConst(_name + "_adminDist") : null);
            _med = (opts.getKeepMed() ? ctx.mkIntConst(_name + "_med") : null);
            _bgpInternal = (modelIbgp ? ctx.mkBoolConst(_name + "_bgpInternal") : null);
            _igpMetric = ((_isBest && modelIbgp) || (isAbstract && !_isExport) ? ctx.mkIntConst(_name + "_igpMetric") : null);
            _ospfArea = null;
            _ospfType = null;

        } else if (proto.isOspf()) {
            _metric = ctx.mkIntConst(_name + "_metric");
            _localPref = (modelLp ? ctx.mkIntConst(_name + "_localPref") : null);
            _adminDist = (modelAd ? ctx.mkIntConst(_name + "_adminDist") : null);
            _med = null;
            _bgpInternal = null;

            if (opts.getKeepOspfType()) {
                _ospfType = new SymbolicOspfType(enc, _name + "_ospfType");
            }
        }


        boolean bestAndNeedId = (_isBestOverall || _isBest && proto.isBgp()) && opts.getNeedRouterId().contains(router);
        if (bestAndNeedId && _enc.isMainSlice()) {
            _routerId = ctx.mkIntConst(_name + "_routerID");
        } else {
            _routerId = null;
        }

        _prefixLength = ctx.mkIntConst(_name + "_prefixLength");
        _permitted = ctx.mkBoolConst(_name + "_permitted");

        _communities = new HashMap<>();
        boolean usesBgp = (proto.isBgp() || (hasBgp && proto.isBest()));
        if ( usesBgp ) {
            for (CommunityVar cvar : enc.getAllCommunities()) {
                String s = cvar.getValue();
                if (cvar.getType() == CommunityVar.Type.OTHER) {
                    s = s + "_OTHER";
                }
                BoolExpr var = ctx.mkBoolConst(_name + "_community_" + s);
                _communities.put(cvar, var);
            }
        }

        // client id
        if (usesBgp && opts.getNeedOriginatorIds()) {
            _clientId = new SymbolicOriginatorId(enc, _name + "_clientId");
        }

        addExprs(enc);
    }

    // TODO: depends on configuration
    /*
     * Check if a particular protocol on a router is configured
     * to use multipath routing or not.
     */
    private boolean isMultipath(String router, Protocol proto, boolean isIbgp) {
        Configuration conf = _enc.getGraph().getConfigurations().get(router);
        if (proto.isConnected()) {
            return true;
        } else if (proto.isStatic()) {
            return true;
        } else if (proto.isOspf()) {
            return true;
        } else if (proto.isBgp()) {
            BgpProcess p = conf.getDefaultVrf().getBgpProcess();
            if (isIbgp) {
                return p.getMultipathIbgp();
            } else {
                return p.getMultipathEbgp();
            }
        } else {
            return true;
        }
    }

    private void addExprs(EncoderSlice enc) {
        Map<String, Expr> all = enc.getAllVariables();

        all.put(_permitted.toString(), _permitted);
        if (_adminDist != null) {
            all.put(_adminDist.toString(), _adminDist);
        }
        if (_med != null) {
            all.put(_med.toString(), _med);
        }
        if (_localPref != null) {
            all.put(_localPref.toString(), _localPref);
        }
        if (_metric != null) {
            all.put(_metric.toString(), _metric);
        }
        if (_prefixLength != null) {
            all.put(_prefixLength.toString(), _prefixLength);
        }
        if (_routerId != null) {
            all.put(_routerId.toString(), _routerId);
        }
        if (_bgpInternal != null) {
            all.put(_bgpInternal.toString(), _bgpInternal);
        }
        if (_ospfArea != null) {
            all.put(_ospfArea.getBitVec().toString(), _ospfArea.getBitVec());
        }
        if (_ospfType != null) {
            all.put(_ospfType.getBitVec().toString(), _ospfType.getBitVec());
        }
        if (_clientId != null) {
            all.put(_clientId.getBitVec().toString(), _clientId.getBitVec());
        }
        if (_igpMetric != null) {
            all.put(_igpMetric.toString(), _igpMetric);
        }
        _communities.forEach((name, var) -> {
            all.put(var.toString(), var);
        });
    }


    boolean getIsUsed() {
        return _isUsed;
    }

    String getName() {
        return _name;
    }

    boolean isBest() {
        return _isBest;
    }

    boolean isEnv() {
        return _isEnv;
    }

    BoolExpr getPermitted() {
        return _permitted;
    }

    ArithExpr getMetric() {
        return _metric;
    }

    ArithExpr getLocalPref() {
        return _localPref;
    }

    ArithExpr getAdminDist() {
        return _adminDist;
    }

    ArithExpr getMed() {
        return _med;
    }

    ArithExpr getRouterId() {
        return _routerId;
    }

    ArithExpr getPrefixLength() {
        return _prefixLength;
    }

    SymbolicEnum<Long> getOspfArea() {
        return _ospfArea;
    }

    SymbolicOspfType getOspfType() {
        return _ospfType;
    }

    Map<CommunityVar, BoolExpr> getCommunities() {
        return _communities;
    }

    SymbolicEnum<Protocol> getProtocolHistory() {
        return _protocolHistory;
    }

    public SymbolicOriginatorId getClientId() {
        return _clientId;
    }

    BoolExpr getBgpInternal() {
        return _bgpInternal;
    }

    public ArithExpr getIgpMetric() {
        return _igpMetric;
    }

    Protocol getProto() {
        return _proto;
    }

    public void setPermitted(BoolExpr _permitted) {
        this._permitted = _permitted;
    }

    public void setPrefixLength(ArithExpr _prefixLength) {
        this._prefixLength = _prefixLength;
    }

    public void setAdminDist(ArithExpr _adminDist) {
        this._adminDist = _adminDist;
    }

    public void setMetric(ArithExpr _metric) {
        this._metric = _metric;
    }

    public void setMed(ArithExpr _med) {
        this._med = _med;
    }

    public void setLocalPref(ArithExpr _localPref) {
        this._localPref = _localPref;
    }

    public void setBgpInternal(BoolExpr _bgpInternal) {
        this._bgpInternal = _bgpInternal;
    }

    public void setIgpMetric(ArithExpr _igpMetric) {
        this._igpMetric = _igpMetric;
    }

    public void setOspfArea(SymbolicEnum<Long> _ospfArea) {
        this._ospfArea = _ospfArea;
    }

    public void setOspfType(SymbolicOspfType _ospfType) {
        this._ospfType = _ospfType;
    }

    public void setRouterId(ArithExpr _routerId) {
        this._routerId = _routerId;
    }

    public void setProtocolHistory(SymbolicEnum<Protocol> _protocolHistory) {
        this._protocolHistory = _protocolHistory;
    }

    public void setClientId(SymbolicOriginatorId _clientId) {
        this._clientId = _clientId;
    }

    public void setCommunities(Map<CommunityVar, BoolExpr> _communities) {
        this._communities = _communities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SymbolicRecord that = (SymbolicRecord) o;

        return (this._name.equals(that._name));
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }
}


