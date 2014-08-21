package batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import batfish.representation.LineAction;

public class ExtendedAccessList implements Serializable {

   private static final long serialVersionUID = 1L;

   private List<ExtendedAccessListLine> _lines;
   private List<ExtendedAccessListTerm> _terms;
   private String _id;

   public ExtendedAccessList(String id) {
      _id = id;
      _lines = new LinkedList<ExtendedAccessListLine>();
      _lines.add(new ExtendedAccessListLine(LineAction.REJECT, 0,
            "0.0.0.0", "255.255.255.255", "0.0.0.0", "255.255.255.255", null, null)); //TODO: Stanley, change these 'null's so destination port ranges work in Juniper
      _terms = new ArrayList<ExtendedAccessListTerm>();
   }

   public String getId() {
      return _id;
   }

   public void addTerm(ExtendedAccessListTerm t){
      _terms.add(t);
   }
   
   public void addLine(ExtendedAccessListLine all) {
      _lines.add(_lines.size()-1, all);
   }

   public List<ExtendedAccessListLine> getLines() {
      return _lines;
   }
   
   public List<ExtendedAccessListTerm> getTerms(){
      return _terms;
   }

   @Override
   public String toString() {
      String output = super.toString() + "\n" + "Identifier: " + _id;
      for (ExtendedAccessListLine line : _lines) {
         output += "\n" + line;
      }
      return output;
   }
}
