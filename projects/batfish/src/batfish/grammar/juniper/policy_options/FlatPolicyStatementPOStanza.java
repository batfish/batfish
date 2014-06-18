package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import batfish.representation.juniper.RouteFilter;
import batfish.representation.juniper.PolicyStatement;
import batfish.representation.juniper.PolicyStatementClause;
import batfish.representation.juniper.PolicyStatementMatchIpPrefixListLine;
import batfish.representation.juniper.PolicyStatementMatchLine;

public class FlatPolicyStatementPOStanza extends POStanza {
	private PolicyStatement _map;
	private List<RouteFilter> _filters;
	private int _seq;
	private boolean _isIPv6;
	
	private boolean _isFrom;
   private FromType _fType;
   private ThenType _tType;

	public FlatPolicyStatementPOStanza(String n) {
		_map = new PolicyStatement(n);
		_filters = new ArrayList<RouteFilter>();
		_seq = 1;
		_isIPv6 = false;
	}

	public void addTerm(FlatTermPSPOStanza ts) {
		_isIPv6 = ts.isIPv6();
		if (!ts.isIPv6()) {
		   _isFrom = ts.isFrom();
		   _fType = ts.getFType();
		   _tType = ts.getTType();
		   List<PolicyStatementMatchLine> tmpml = ts.getMatchList();
		   
			if(!(ts.getRouteFilterLines().isEmpty())){
		   // construct the route filter
			RouteFilter rf = new RouteFilter(_map.getMapName() + "."
					+ ts.getTermName());
			rf.addLines(ts.getRouteFilterLines());
			_filters.add(rf);
			// add the route filter matching line			
			tmpml.add(new PolicyStatementMatchIpPrefixListLine(Collections.singletonList(rf
					.getName())));
			}

			PolicyStatementClause rmc = new PolicyStatementClause(ts.getAction(),
					_map.getMapName(), _seq, tmpml, ts.getSetList());
			rmc.setClauseName(ts.getTermName());
			_map.addClause(rmc);
			_seq++;
		}
	}

	public PolicyStatement getMap() {
		return _map;
	}

	public List<RouteFilter> getRouteFilters() {
		return _filters;
	}

	public boolean isIPv6() {
		return _isIPv6;
	}
	
	public boolean isFrom(){
      return _isFrom;
   }
   
   public FromType getFType(){
      return _fType;      
   }
   
   public ThenType getTType(){
      return _tType;
   }


	@Override
	public POType getType() {
		return POType.POLICY_STATEMENT;
	}

}
