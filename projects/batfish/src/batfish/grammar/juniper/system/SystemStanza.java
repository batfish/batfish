package batfish.grammar.juniper.system;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.grammar.juniper.StanzaStatusType;

public class SystemStanza extends JStanza {
   
	private String _hostName;
	private List<SysStanza> _sysStanzas;
   
   /* ------------------------------ Constructor ----------------------------*/
	public SystemStanza () {
      _sysStanzas = new ArrayList<SysStanza> ();
      set_postProcessTitle("System");
   }
	
   /* ----------------------------- Other Methods ---------------------------*/
	public void AddSysStanza (SysStanza s) {
      _sysStanzas.add(s);
   }
	
   /* ---------------------------- Getters/Setters --------------------------*/
	public String get_hostName() {
      return _hostName;
   }
	
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public void postProcessStanza() {
      
	   for (SysStanza ss : _sysStanzas) {
	      ss.postProcessStanza();

         if (ss.get_stanzaStatus() == StanzaStatusType.ACTIVE) {
      		switch (ss.getSysType()) {
      		case HOST_NAME:
      			Sys_HostNameStanza hss = (Sys_HostNameStanza) ss;
      			_hostName = hss.get_hostName();
      			break;
      
      		case NULL:
      			break;
      
      		default:
      		   throw new Error ("bad system stanza type");   	
      		}
         }
   		addIgnoredStatements(ss.get_ignoredStatements());
	   }
      set_alreadyAggregated(false);
      super.postProcessStanza();
	}

	@Override
	public JStanzaType getType() {
		return JStanzaType.SYSTEM;
	}

}
