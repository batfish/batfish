package batfish.grammar.juniper.firewall;

import java.util.List;

import batfish.representation.LineAction;
import batfish.representation.juniper.ExtendedAccessList;
import batfish.representation.juniper.ExtendedAccessListTerm;
import batfish.util.SubRange;

public class FlatFilterFStanza {

   private boolean _isFrom;
   private FromTFFType _fType;
   private ThenTFFType _tType;

   private ExtendedAccessList _filter;
   private String _tName;
   private List<String> _destinationAddress;
   private List<String> _sourceAddress;
   private List<SubRange> _ports;
   private List<Integer> _protocols;
   private LineAction _ala;

   public FlatFilterFStanza(String fname, String tname) {
      //System.out.println("create flat filter");
      _filter = new ExtendedAccessList(fname);
      _tName = tname;
      _ala = null;
   }

   public void processFromStanza(FromTFFStanza fs) {
      //System.out.println("process from");
      _isFrom = true;
      _fType = fs.getType();
      switch (fs.getType()) {
      case DESTINATION_ADDRESS:
         DestinationAddressFromTFFStanza dfs = (DestinationAddressFromTFFStanza) fs;
         _destinationAddress = dfs.getAddress();
         break;

      case SOURCE_ADDRESS:
         SourceAddressFromTFFStanza sfs = (SourceAddressFromTFFStanza) fs;
         _sourceAddress = sfs.getAddress();
         break;

      case DESTINATION_PORT:
         DestinationPortFromTFFStanza dpfs = (DestinationPortFromTFFStanza) fs;
         _ports = dpfs.getPorts();
         break;

      case PROTOCOL:
         ProtocolFromTFFStanza prfs = (ProtocolFromTFFStanza) fs;
         _protocols = prfs.getProtocols();
         break;
      case SOURCE_PORT:
         throw new Error("not implemented");

      default:
         throw new Error("bad firewall term from stanza type");
      }
   }

   public void processThenStanza(ThenTFFStanza ts) {
      //System.out.println("process then");
      _isFrom = false;
      _tType = ts.getType();
      switch (ts.getType()) {
      case ACCEPT:
         _ala = LineAction.ACCEPT;
         break;

      case DISCARD:
         _ala = LineAction.REJECT;
         break;

      case NEXT_TERM:
      case NULL:
         break;

      default:
         throw new Error("bad firewall term then stanza type");
      }
   }

   public void processTerm() {
      ExtendedAccessListTerm term = new ExtendedAccessListTerm(_tName);

      if (_protocols != null) {
         for (Integer p : _protocols) {
            term.addProtocol(p);
         }
      }

      if (_destinationAddress != null) {
         for (String d : _destinationAddress) {
            term.addDestinationAddress(d);
         }
      }

      if (_sourceAddress != null) {
         for (String s : _sourceAddress) {
            term.addSourceAddress(s);
         }
      }

      if (_ports != null) {
         for (SubRange r : _ports) {
            term.addSrcPortRange(r);
         }
      }
      
      term.setLineAction(_ala);

      _filter.addTerm(term);

   }

   public String getTermName() {
      return _tName;
   }

   public List<String> getDestinationAddress() {
      return _destinationAddress;
   }

   public List<String> getSourceAddress() {
      return _sourceAddress;
   }

   public List<SubRange> getPorts() {
      return _ports;
   }

   public List<Integer> getProtocols() {
      return _protocols;
   }

   public LineAction getLineAction() {
      return _ala;
   }

   public boolean getIsFrom() {
      return _isFrom;
   }

   public FromTFFType getFType() {
      return _fType;
   }

   public ThenTFFType getTType() {
      return _tType;
   }

   public ExtendedAccessList getFilter() {
      return _filter;
   }

}