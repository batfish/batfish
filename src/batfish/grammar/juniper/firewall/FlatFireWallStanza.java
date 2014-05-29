package batfish.grammar.juniper.firewall;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.representation.juniper.ExtendedAccessList;

public class FlatFireWallStanza extends JStanza {
   private List<ExtendedAccessList> _filters;
   private boolean _isFrom;
   private FromTFFType _fType;
   private ThenTFFType _tType;

   public FlatFireWallStanza() {
      _filters = new ArrayList<ExtendedAccessList>();
   }

   public void processStanza(FlatFilterFStanza fs) {
      _filters.add(fs.getFilter());
      _isFrom = fs.getIsFrom();
      _fType = fs.getFType();
      _tType = fs.getTType();
   }

   public List<ExtendedAccessList> getFilters() {
      return _filters;
   }
   
   public boolean getIsFrom(){
      return _isFrom;
   }
   
   public FromTFFType getFType(){
      return _fType;
   }
   
   public ThenTFFType getTType(){
      return _tType;
   }

   @Override
   public JStanzaType getType() {
      return JStanzaType.FIREWALL;
   }

}
