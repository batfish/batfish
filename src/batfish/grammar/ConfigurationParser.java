package batfish.grammar;

import java.util.List;

import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

import batfish.representation.VendorConfiguration;

public abstract class ConfigurationParser extends Parser {

   public ConfigurationParser(TokenStream input) {
      super(input);
   }

   public ConfigurationParser(TokenStream input, RecognizerSharedState state) {
      super(input, state);
   }
   
   public abstract List<String> getErrors();

   public VendorConfiguration parse_configuration() throws RecognitionException {
      return null;
   }
}
