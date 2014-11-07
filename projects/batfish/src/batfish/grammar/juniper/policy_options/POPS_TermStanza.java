package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.grammar.juniper.StanzaWithStatus;
import batfish.representation.juniper.PolicyStatementMatchAsPathAccessListLine;
import batfish.representation.juniper.PolicyStatementMatchCommunityListLine;
import batfish.representation.juniper.PolicyStatementMatchFamilyLine;
import batfish.representation.juniper.PolicyStatementMatchInterfaceListLine;
import batfish.representation.juniper.PolicyStatementMatchRibFromLine;
import batfish.representation.juniper.PolicyStatementMatchRibToLine;
import batfish.representation.juniper.PolicyStatementSetAsPathPrepend;
import batfish.representation.juniper.PolicyStatementSetCommunityAddLine;
import batfish.representation.juniper.PolicyStatementSetCommunityDeleteLine;
import batfish.representation.juniper.PolicyStatementSetCommunitySetLine;
import batfish.representation.juniper.PolicyStatementSetInstallNextHopLine;
import batfish.representation.juniper.PolicyStatementSetLocalPreferenceLine;
import batfish.representation.juniper.PolicyStatementSetMetricLine;
import batfish.representation.juniper.PolicyStatementSetNextHopLine;
import batfish.representation.juniper.PolicyStatement_LineAction;
import batfish.representation.juniper.PolicyStatement_MatchLine;
import batfish.representation.juniper.PolicyStatementMatchNeighborLine;
import batfish.representation.juniper.PolicyStatementMatchPrefixListFilterLine;
import batfish.representation.juniper.PolicyStatementMatchPrefixListLine;
import batfish.representation.juniper.PolicyStatementMatchProtocolListLine;
import batfish.representation.juniper.PolicyStatementMatchRouteFilterLine;
import batfish.representation.juniper.PolicyStatementMatchSourceAddressFilterLine;
import batfish.representation.juniper.PolicyStatement_SetLine;
import batfish.representation.juniper.PolicyStatement_Term;

public class POPS_TermStanza extends StanzaWithStatus {
   
   private String _name;
   boolean _isEmpty = true;
   private List<POPST_FromStanza> _fromTerms;
   private List<POPST_ThenStanza> _thenTerms;
   private List<POPST_ToStanza> _toTerms;
   
   private PolicyStatement_LineAction _lineAction;
   private List<PolicyStatement_MatchLine> _matchList;
   private List<PolicyStatement_SetLine> _setList;
   
   private PolicyStatement_Term _term;
   
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPS_TermStanza() {
      _name = "rule0"; // TODO [P1]: should this be the default name?
      
      _fromTerms = new ArrayList<POPST_FromStanza> ();
      _thenTerms = new ArrayList<POPST_ThenStanza> ();
      _toTerms = new ArrayList<POPST_ToStanza> ();
      
      set_postProcessTitle("Term " + _name);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addFromStanza (POPST_FromStanza f) {
      _fromTerms.add(f);
      _isEmpty = false;
   }
   public void addThenStanza (POPST_ThenStanza t) {
      _thenTerms.add(t);
      _isEmpty = false;
   }
   public void addToStanza (POPST_ToStanza t) {
      _toTerms.add(t);
      _isEmpty = false;
   }   
   private void processFromStanza(POPST_FromStanza fs) {
      
      if (fs.get_stanzaStatus()==StanzaStatusType.INACTIVE) {
         this.set_stanzaStatus(StanzaStatusType.INACTIVE);
         return;
      }
      if (fs.get_stanzaStatus()==StanzaStatusType.IGNORED) {
         this.set_stanzaStatus(StanzaStatusType.IGNORED);
         return;
      }

      switch (fs.getType()) {
      case AS_PATH:
         POPSTFr_AsPathStanza asfs = (POPSTFr_AsPathStanza) fs;
         for (String pathname : asfs.get_pathnames()) {
            PolicyStatement_MatchLine ml_aspath = new PolicyStatementMatchAsPathAccessListLine(pathname);
            _matchList.add(ml_aspath);
         }
         break;

      case COMMUNITY:
         POPSTFr_CommunityStanza cfs = (POPSTFr_CommunityStanza) fs;
         List<String> commNames = cfs.get_commNames();
         for (String x : commNames) {
            PolicyStatement_MatchLine ml_comm = new PolicyStatementMatchCommunityListLine(x);
            _matchList.add(ml_comm);
         }
         break;

      case FAMILY:
         POPSTFr_FamilyStanza ffs = (POPSTFr_FamilyStanza) fs;
         if (ffs.get_stanzaStatus()==StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else {
            PolicyStatement_MatchLine ml_family = new PolicyStatementMatchFamilyLine(ffs.get_famType());
            _matchList.add(ml_family);
         }
         break;

      case INTERFACE:
         POPSTFr_InterfaceStanza ifs = (POPSTFr_InterfaceStanza) fs;
         PolicyStatement_MatchLine ml_interface = new PolicyStatementMatchInterfaceListLine(ifs.get_interfaceNames());
         _matchList.add(ml_interface);
         break;

      case NEIGHBOR:
         POPSTFr_NeighborStanza nfs = (POPSTFr_NeighborStanza) fs;
         if (nfs.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else {               
            PolicyStatement_MatchLine ml_neighbor = new PolicyStatementMatchNeighborLine(nfs.get_ip());
            _matchList.add(ml_neighbor);
         }
         break;

      case PREFIX_LIST_FILTER:
         POPSTFr_PrefixListFilterStanza pfs = (POPSTFr_PrefixListFilterStanza) fs;
         if (pfs.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else { 
            PolicyStatement_MatchLine ml_plf = new PolicyStatementMatchPrefixListFilterLine(pfs.get_listName(), pfs.get_fms());
            _matchList.add(ml_plf);
         }
         break;

      case PREFIX_LIST:
         POPSTFr_PrefixListStanza plffs = (POPSTFr_PrefixListStanza) fs;
         if (plffs.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else {
            PolicyStatement_MatchLine ml_prefixlist = new PolicyStatementMatchPrefixListLine(plffs.get_listName());
            _matchList.add(ml_prefixlist);
         }
         break;

      case PROTOCOL:
         POPSTFr_ProtocolStanza prfs = (POPSTFr_ProtocolStanza) fs;
         if (prfs.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else {
            PolicyStatement_MatchLine ml_prtcl = new PolicyStatementMatchProtocolListLine(prfs.get_protocols());
            _matchList.add(ml_prtcl);
         }
         break;

      case RIB:
         POPSTFr_RibStanza rfs = (POPSTFr_RibStanza) fs;
         if (rfs.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else if (rfs.get_stanzaStatus() == StanzaStatusType.IGNORED) {
               this.set_stanzaStatus(StanzaStatusType.IGNORED);
         }
         else {
            PolicyStatement_MatchLine ml_rib = new PolicyStatementMatchRibFromLine(rfs.get_ribName());
            _matchList.add(ml_rib);
         }
         break;

      case ROUTE_FILTER:
         POPSTFr_RouteFilterStanza rffs = (POPSTFr_RouteFilterStanza) fs;
         if (rffs.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else {
            PolicyStatement_MatchLine ml_routf = new PolicyStatementMatchRouteFilterLine(rffs.get_prefix(), rffs.get_prefixLenght(), rffs.get_fms());
            _matchList.add(ml_routf);
         }
         break;

      case SOURCE_ADDRESS_FILTER:
         POPSTFr_SourceAddressFilterStanza sfs = (POPSTFr_SourceAddressFilterStanza) fs;
         if (sfs.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else {
            PolicyStatement_MatchLine ml_routf = new PolicyStatementMatchSourceAddressFilterLine(sfs.get_prefix(), sfs.get_prefixLenght(), sfs.get_fms());
            _matchList.add(ml_routf);
         }
         break;

      default:
         throw new Error("bad from stanza type");
      }
   }
   
   public void processToStanza(POPST_ToStanza ts) {

      if (ts.get_stanzaStatus()==StanzaStatusType.INACTIVE) {
         this.set_stanzaStatus(StanzaStatusType.INACTIVE);
         return;
      }
      if (ts.get_stanzaStatus()==StanzaStatusType.IGNORED) {
         this.set_stanzaStatus(StanzaStatusType.IGNORED);
         return;
      }

      switch (ts.getType()) {
      case RIB:
         POPSTTo_RibStanza rfs = (POPSTTo_RibStanza) ts;
         if (rfs.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else if (rfs.get_stanzaStatus() == StanzaStatusType.IGNORED) {
               this.set_stanzaStatus(StanzaStatusType.IGNORED);
         }
         else {
            PolicyStatement_MatchLine ml_rib = new PolicyStatementMatchRibToLine(rfs.get_ribName());
            _matchList.add(ml_rib);
         }
         break;

      default:
         throw new Error("bad to stanza type");
      }
   }
   
   public void processThenStanza(POPST_ThenStanza ts) {

      if (ts.get_stanzaStatus()==StanzaStatusType.INACTIVE) {
         this.set_stanzaStatus(StanzaStatusType.INACTIVE);
         return;
      }
      if (ts.get_stanzaStatus()==StanzaStatusType.IGNORED) {
         this.set_stanzaStatus(StanzaStatusType.IGNORED);
         return;
      }

      switch (ts.getType()) {
      
      case ACCEPT:
         _lineAction = PolicyStatement_LineAction.ACCEPT;
         break;
         
      case AS_PATH_PREPEND:
         POPSTTh_AsPathPrependStanza ats = (POPSTTh_AsPathPrependStanza) ts;
         PolicyStatement_SetLine appl = new PolicyStatementSetAsPathPrepend(ats.get_asNumToPrepend());
         _setList.add(appl);
         break;
         
      case COMMUNITY:
         POPSTTh_CommunityStanza cts = (POPSTTh_CommunityStanza) ts;

         switch (cts.get_commType()) {
         
         case COMM_ADD:
            PolicyStatement_SetLine cal = new PolicyStatementSetCommunityAddLine(cts.get_commNames());
            _setList.add(cal);
            break;
            
         case COMM_DELETE:
            PolicyStatement_SetLine cdl = new PolicyStatementSetCommunityDeleteLine(cts.get_commNames());
            _setList.add(cdl);
            break;
            
         case COMM_SET:
            PolicyStatement_SetLine csl = new PolicyStatementSetCommunitySetLine(cts.get_commNames());
            _setList.add(csl);
            break;
         }
         break;
         
      case INSTALL_NEXT_HOP:
         POPSTTh_InstallNextHopStanza its = (POPSTTh_InstallNextHopStanza) ts;
         PolicyStatement_SetLine inl = new PolicyStatementSetInstallNextHopLine(its.get_hopName());
         _setList.add(inl);
         break;
         
      case LOCAL_PREF: // TODO: [P1]: should be null
         POPSTTh_LocalPreferenceStanza lpts = (POPSTTh_LocalPreferenceStanza) ts;
         PolicyStatement_SetLine lpnl = new PolicyStatementSetLocalPreferenceLine(lpts.get_localPref());
         _setList.add(lpnl);
         break;
         
      case METRIC: 
         POPSTTh_MetricStanza mts = (POPSTTh_MetricStanza) ts;
         PolicyStatement_SetLine mnl = new PolicyStatementSetMetricLine(mts.get_metric());
         _setList.add(mnl);
         break;
         
      case NEXT_HOP:
         POPSTTh_NextHopStanza nts = (POPSTTh_NextHopStanza) ts;
         if (nts.get_stanzaStatus() == StanzaStatusType.IPV6) {
            this.set_stanzaStatus(StanzaStatusType.IPV6);
         }
         else {
            PolicyStatement_SetLine hnl = new PolicyStatementSetNextHopLine(nts.get_hopName(), nts.get_hopType());
            _setList.add(hnl);
         }
         break;
         
      case NEXT_POLICY:
         _lineAction = PolicyStatement_LineAction.NEXT_POLICY;
         break;
         
      case NEXT_TERM:
         _lineAction = PolicyStatement_LineAction.NEXT_TERM;
         break;
         
      case REJECT:
         _lineAction = PolicyStatement_LineAction.REJECT;
         break;
         
      case NULL: 
         this.addIgnoredStatements(ts.get_ignoredStatements());
         break;
      
      default:
         throw new Error("bad to stanza type");
      }
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_name(String n) {
      _name = n;
   }
   public PolicyStatement_Term get_term() {
      return _term;
   }
   public boolean get_isEmpty () {
      return _isEmpty;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza() {
      
      _matchList = new ArrayList<PolicyStatement_MatchLine>();

      _lineAction = null;
      _setList = new ArrayList<PolicyStatement_SetLine>();
      
      for (POPST_FromStanza f : _fromTerms) {
         processFromStanza(f);
      }      
      if (get_stanzaStatus()==StanzaStatusType.IPV6) {       // if we ran into an IPV6 address, cut short
         clearIgnoredStatements();
         addIgnoredStatement("term " + _name + " (IPV6 Clauses) {...}");
         set_alreadyAggregated(true);
         return;
      }
      if (get_stanzaStatus()==StanzaStatusType.IGNORED) {       // if we ran into an IGNORED clause, cut short
         clearIgnoredStatements();
         addIgnoredStatement("term " + _name + " (Ignored Clauses) {...}");
         set_alreadyAggregated(true);
         return;
      }
      for (POPST_ToStanza t : _toTerms) {
         processToStanza(t);
      }   
      if (get_stanzaStatus()==StanzaStatusType.IPV6) {       // if we ran into an IPV6 address, cut short
         clearIgnoredStatements();
         addIgnoredStatement("term " + _name + " (IPV6 Clauses) {...}");
         set_alreadyAggregated(true);
         return;
      }
      if (get_stanzaStatus()==StanzaStatusType.IGNORED) {       // if we ran into an IGNORED clause, cut short
         clearIgnoredStatements();
         addIgnoredStatement("term " + _name + " (Ignored Clauses) {...}");
         set_alreadyAggregated(true);
         return;
      }
      for (POPST_ThenStanza t : _thenTerms) {
         processThenStanza(t);
      }   
      if (get_stanzaStatus()==StanzaStatusType.IPV6) {       // if we ran into an IPV6 address, cut short
         clearIgnoredStatements();
         addIgnoredStatement("term " + _name + " (IPV6 Clauses) {...}");
         set_alreadyAggregated(true);
         return;
      }
      if (get_stanzaStatus()==StanzaStatusType.IGNORED) {       // if we ran into an IGNORED clause, cut short
         clearIgnoredStatements();
         addIgnoredStatement("term " + _name + " (Ignored Clauses) {...}");
         set_alreadyAggregated(true);
         return;
      }
      _term = new PolicyStatement_Term(_name, _matchList, _setList, _lineAction);
      set_alreadyAggregated(false);
      super.postProcessStanza();
   }
}
