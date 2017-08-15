package org.batfish.smt;


import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.*;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>A class representing a collection of modifications made to
 * a protocols message after passing through an import/export filter</p>
 *
 * @author Ryan Beckett
 */
class Modifications {

    private EncoderSlice _encoderSlice;

    private Configuration _conf;

    private boolean _defaultAccept;

    private boolean _defaultAcceptLocal;

    private SetDefaultPolicy _defaultPolicy;

    private PrependAsPath _prependPath;

    private SetLocalPreference _setLp;

    private SetMetric _setMetric;

    private SetOspfMetricType _setOspfMetricType;

    private SetWeight _setWeight;

    private SetNextHop _setNextHop;

    private Set<CommunityVar> _positiveCommunities;

    private Set<CommunityVar> _negativeCommunities;

    Modifications(EncoderSlice encoderSlice, Configuration conf) {
        _encoderSlice = encoderSlice;
        _conf = conf;
        _defaultPolicy = null;
        _defaultAccept = false;
        _defaultAcceptLocal = false;
        _prependPath = null;
        _setLp = null;
        _setMetric = null;
        _setOspfMetricType = null;
        _setWeight = null;
        _setNextHop = null;
        _positiveCommunities = new HashSet<>();
        _negativeCommunities = new HashSet<>();
    }

    Modifications(Modifications other) {
        PrependAsPath a = other.getPrependPath();
        SetLocalPreference b = other.getSetLp();
        SetMetric c = other.getSetMetric();
        SetWeight d = other.getSetWeight();
        SetNextHop e = other.getSetNextHop();
        Set<CommunityVar> f = other.getPositiveCommunities();
        Set<CommunityVar> g = other.getNegativeCommunities();
        SetOspfMetricType h = other.getSetOspfMetricType();
        SetDefaultPolicy i = other.getSetDefaultPolicy();

        _encoderSlice = other._encoderSlice;
        _conf = other._conf;
        _defaultPolicy = (i == null ? null : new SetDefaultPolicy(i.getDefaultPolicy()));
        _defaultAccept = other._defaultAccept;
        _defaultAcceptLocal = other._defaultAcceptLocal;
        _prependPath = (a == null ? null : new PrependAsPath(a.getExpr()));
        _setLp = (b == null ? null : new SetLocalPreference(b.getLocalPreference()));
        _setMetric = (c == null ? null : new SetMetric(c.getMetric()));
        _setWeight = (d == null ? null : new SetWeight(d.getWeight()));
        _setNextHop = (e == null ? null : new SetNextHop(e.getExpr(), e.getDestinationVrf()));
        _positiveCommunities = (f == null ? null : new HashSet<>(f));
        _negativeCommunities = (g == null ? null : new HashSet<>(g));
        _setOspfMetricType = (h == null ? null : new SetOspfMetricType(h.getMetricType()));
    }

    private void addPositiveCommunities(Set<CommunityVar> cs) {
        for (CommunityVar c : cs) {
            _positiveCommunities.add(c);
            _negativeCommunities.remove(c);
        }
    }

    private void addNegativeCommunities(Set<CommunityVar> cs) {
        for (CommunityVar c : cs) {
            _positiveCommunities.remove(c);
            _negativeCommunities.add(c);
        }
    }

    void addModification(Statement stmt) {

        if (stmt instanceof Statements.StaticStatement) {
            Statements.StaticStatement ss = (Statements.StaticStatement) stmt;
            if (ss.getType() == Statements.SetDefaultActionAccept) {
                _defaultAccept = true;
            }
            if (ss.getType() == Statements.SetDefaultActionReject) {
                _defaultAccept = false;
            }
            if (ss.getType() == Statements.SetLocalDefaultActionAccept) {
                _defaultAcceptLocal = true;
            }
            if (ss.getType() == Statements.SetLocalDefaultActionReject) {
                _defaultAcceptLocal = false;
            }
        }

        if (stmt instanceof SetDefaultPolicy) {
            _defaultPolicy = (SetDefaultPolicy) stmt;
        }

        if (stmt instanceof PrependAsPath) {
            _prependPath = (PrependAsPath) stmt;
        }

        if (stmt instanceof SetLocalPreference) {
            _setLp = (SetLocalPreference) stmt;
        }

        if (stmt instanceof SetMetric) {
            _setMetric = (SetMetric) stmt;
        }

        if (stmt instanceof SetOspfMetricType) {
            _setOspfMetricType = (SetOspfMetricType) stmt;
        }

        if (stmt instanceof SetWeight) {
            _setWeight = (SetWeight) stmt;
        }

        if (stmt instanceof SetNextHop) {
            _setNextHop = (SetNextHop) stmt;
        }

        if (stmt instanceof AddCommunity) {
            AddCommunity x = (AddCommunity) stmt;
            Set<CommunityVar> comms = _encoderSlice.findAllCommunities(_conf, x.getExpr());
            addPositiveCommunities(comms);
        }

        if (stmt instanceof SetCommunity) {
            SetCommunity x = (SetCommunity) stmt;
            Set<CommunityVar> comms = _encoderSlice.findAllCommunities(_conf, x.getExpr());
            addPositiveCommunities(comms);
        }

        if (stmt instanceof DeleteCommunity) {
            DeleteCommunity x = (DeleteCommunity) stmt;
            Set<CommunityVar> comms = _encoderSlice.findAllCommunities(_conf, x.getExpr());
            addNegativeCommunities(comms);
        }

        if (stmt instanceof RetainCommunity) {
            // TODO
        }

    }

    void resetDefaultPolicy() {
        _defaultPolicy = null;
    }

    PrependAsPath getPrependPath() {
        return _prependPath;
    }

    SetLocalPreference getSetLp() {
        return _setLp;
    }

    SetMetric getSetMetric() {
        return _setMetric;
    }

    SetOspfMetricType getSetOspfMetricType() {
        return _setOspfMetricType;
    }

    SetWeight getSetWeight() {
        return _setWeight;
    }

    SetNextHop getSetNextHop() {
        return _setNextHop;
    }

    Set<CommunityVar> getPositiveCommunities() {
        return _positiveCommunities;
    }

    Set<CommunityVar> getNegativeCommunities() {
        return _negativeCommunities;
    }

    boolean getDefaultAccept() {
        return _defaultAccept;
    }

    boolean getDefaultAcceptLocal() {
        return _defaultAcceptLocal;
    }

    SetDefaultPolicy getSetDefaultPolicy() {
        return _defaultPolicy;
    }

    public void setDefaultAcceptLocal(boolean _defaultAcceptLocal) {
        this._defaultAcceptLocal = _defaultAcceptLocal;
    }
}
