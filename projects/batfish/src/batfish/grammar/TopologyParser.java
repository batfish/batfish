package batfish.grammar;

import java.util.List;

import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

import batfish.representation.Topology;

public abstract class TopologyParser extends Parser {

   public TopologyParser(TokenStream input) {
      super(input);
   }

   public TopologyParser(TokenStream input, RecognizerSharedState state) {
      super(input, state);
   }
   
   public abstract List<String> getErrors();

   public Topology topology() throws RecognitionException {
      return null;
   }
}
