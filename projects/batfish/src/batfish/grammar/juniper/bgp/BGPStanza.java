package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.grammar.juniper.protocols.PStanza;
import batfish.grammar.juniper.protocols.PType;
import batfish.representation.juniper.BGPGroup;

public class BGPStanza extends PStanza {
   
	private List<BGPGroup> _groupList;
	private List<String> _activatedNeighbors;
	private List<BGStanza> _bgStanzas;

   /* ------------------------------ Constructor ----------------------------*/
	public BGPStanza() {
      _bgStanzas = new ArrayList<BGStanza> ();
   }

   /* ----------------------------- Other Methods ---------------------------*/
	public void AddBGStanza (BGStanza bs) {
	   _bgStanzas.add(bs);
	}
   
   /* ---------------------------- Getters/Setters --------------------------*/
	public List<String> get_activatedNeighbors () {
	   return _activatedNeighbors;
	}
	public List<BGPGroup> get_groupList () {
	   return _groupList;
	}
	
   /* --------------------------- Inherited Methods -------------------------*/ 
   @Override
   public void postProcessStanza() {
      super.postProcessStanza();
	   
      _groupList = new ArrayList<BGPGroup>();
      _activatedNeighbors = new ArrayList<String>();
	   
	   for (BGStanza bgs : _bgStanzas) { 
         bgs.postProcessStanza();

         if (bgs.get_stanzaStatus()==StanzaStatusType.ACTIVE) {
	      
            switch (bgs.getType()) {
 
      		case FAMILY:
      		   // TODO [Ask Ari]: WHat to do with this
      		   break;
      		
      		case GROUP:
      			BG_GroupStanza gbgs = (BG_GroupStanza) bgs;
   				_groupList.add(gbgs.get_group());
   				_activatedNeighbors.addAll(gbgs.get_activatedNeighbors());
      			break;
      			
      		
      		case NULL:
      			break;
      
      		default:
               throw new Error("bad ospf stanza type");
            }
         }
         else {
            // TODO [p0]: figure out!
         }
         this.addIgnoredStatements(bgs.get_ignoredStatements());
      }
   }

	@Override
	public PType getType() {
		return PType.BGP;
	}

}
