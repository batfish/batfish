package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.representation.juniper.Interface;

public class InterfacesStanza extends JStanza {
	private List<Interface> _interfaceList;

	public InterfacesStanza() {
		_interfaceList = new ArrayList<Interface>();
	}

	public void processStanza(InterfaceStanza is) {
		_interfaceList.addAll(is.getInterfaceList());
	}

	public List<Interface> getInterfaceList() {
		return _interfaceList;
	}

	@Override
	public JStanzaType getType() {
		return JStanzaType.INTERFACES;
	}

}