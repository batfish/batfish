package batfish.grammar.cisco.interfaces;

import batfish.representation.SwitchportMode;
import batfish.representation.cisco.Interface;

public class SwitchportModeIFStanza implements IFStanza {

   private SwitchportMode _mode;

   public SwitchportModeIFStanza(SwitchportMode mode) {
      _mode = mode;
   }

   public SwitchportMode getMode() {
      return _mode;
   }

   @Override
   public void process(Interface i) {
      i.setSwitchportMode(_mode);
   }

}
