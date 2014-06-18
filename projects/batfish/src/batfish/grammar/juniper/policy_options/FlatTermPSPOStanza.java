package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import batfish.representation.LineAction;
import batfish.representation.juniper.RouteFilterLine;
import batfish.representation.juniper.RouteFilterSubRangeLine;
import batfish.representation.juniper.RouteFilterThroughLine;
import batfish.representation.juniper.PolicyStatementMatchIpPrefixListLine;
import batfish.representation.juniper.PolicyStatementMatchLine;
import batfish.representation.juniper.PolicyStatementMatchNeighborLine;
import batfish.representation.juniper.PolicyStatementMatchProtocolLine;
import batfish.representation.juniper.PolicyStatementSetLine;
import batfish.representation.juniper.PolicyStatementSetLocalPreferenceLine;
import batfish.representation.juniper.PolicyStatementSetMetricLine;
import batfish.representation.juniper.PolicyStatementSetNextHopLine;

public class FlatTermPSPOStanza {
   private boolean _isFrom;
   private FromType _fType;
   private ThenType _tType;
   
   private String _name;
   private LineAction _ala;
   private List<PolicyStatementSetLine> _setList;
   private List<PolicyStatementMatchLine> _matchList;
   private List<RouteFilterLine> _lines;
   private boolean _isIPv6;

   public FlatTermPSPOStanza(String n) {
      _name = n;
      _ala = null;
      _setList = new ArrayList<PolicyStatementSetLine>();
      _matchList = new ArrayList<PolicyStatementMatchLine>();
      _lines = new ArrayList<RouteFilterLine>();
      _isIPv6 = false;
   }

   public void processFromStanza(FromTPSStanza fs) {
      if (!_isIPv6) {
         _isFrom = true;
         _fType = fs.getType();
         switch (fs.getType()) {
         case IPV6:
            _isIPv6 = true;
            break;

         case NEIGHBOR:
            NeighborFromTPSStanza nfs = (NeighborFromTPSStanza) fs;
            PolicyStatementMatchLine ml1 = new PolicyStatementMatchNeighborLine(
                  nfs.getNeighborIP());
            _matchList.add(ml1);
            break;

         case NULL:
            break;

         case PREFIX_LIST:
            PrefixListFromTPSStanza plfs = (PrefixListFromTPSStanza) fs;
            PolicyStatementMatchLine ml2 = new PolicyStatementMatchIpPrefixListLine(
                  Collections.singletonList(plfs.getListName()));
            _matchList.add(ml2);
            break;

         case PROTOCOL:
            ProtocolFromTPSStanza pfs = (ProtocolFromTPSStanza) fs;
            PolicyStatementMatchLine ml3 = new PolicyStatementMatchProtocolLine(
                  pfs.getProtocol());
            _matchList.add(ml3);
            break;

         case ROUTE_FILTER:
            RouteFilterFromTPSStanza rffs = (RouteFilterFromTPSStanza) fs;
            if (rffs.getSecondPrefix() == null) {
               RouteFilterLine rfl = new RouteFilterSubRangeLine(
                     rffs.getPrefix(), rffs.getPrefixLength(),
                     rffs.getLengthRange());

               _lines.add(rfl);
            }
            else {
               RouteFilterLine rfl = new RouteFilterThroughLine(
                     rffs.getPrefix(), rffs.getPrefixLength(),
                     rffs.getSecondPrefix(), rffs.getSecondPrefixLength());
               _lines.add(rfl);
            }
            break;
         case COMMUNITY:
         case AS_PATH:
            throw new Error("not implemented");
         
         default:
            System.out.println("bad from stanza type");
            break;
         }
      }

   }

   public void processThenStanza(ThenTPSStanza ts) {
      if (!_isIPv6) {
         _isFrom = false;
         _tType = ts.getType();
         switch (ts.getType()) {
         case ACCEPT:
            _ala = LineAction.ACCEPT;
            break;

         case LOCAL_PREF:
            LocalPreferenceThenTPSStanza lpts = (LocalPreferenceThenTPSStanza) ts;
            PolicyStatementSetLine lsl = new PolicyStatementSetLocalPreferenceLine(
                  lpts.getLocalPref());
            _setList.add(lsl);
            break;

         case METRIC:
            MetricThenTPSStanza mts = (MetricThenTPSStanza) ts;
            PolicyStatementSetLine msl = new PolicyStatementSetMetricLine(mts.getMetric());
            _setList.add(msl);
            break;

         case NEXT_HOP:
            NextHopThenTPSStanza nhts = (NextHopThenTPSStanza) ts;
            PolicyStatementSetLine nsl = new PolicyStatementSetNextHopLine(
                  Collections.singletonList(nhts.getNextHop()));
            _setList.add(nsl);
            break;

         case NULL:
            break;

         case REJECT:
            _ala = LineAction.REJECT;
            break;
         case COMMUNITY_ADD:       
         case COMMUNITY_DELETE:
         case COMMUNITY_SET:
            throw new Error("not implemented");

         default:
            throw new Error("bad then stanza type");
         }
      }

   }

   public LineAction getAction() {
      return _ala;
   }

   public List<PolicyStatementSetLine> getSetList() {
      return _setList;
   }

   public List<PolicyStatementMatchLine> getMatchList() {
      return _matchList;
   }

   public List<RouteFilterLine> getRouteFilterLines() {
      return _lines;
   }

   public String getTermName() {
      return _name;
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

}
