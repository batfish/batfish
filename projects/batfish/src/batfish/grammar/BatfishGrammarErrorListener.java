package batfish.grammar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

public class BatfishGrammarErrorListener implements ANTLRErrorListener {
   private final List<String> _errors;
   private String _grammarName;

   public BatfishGrammarErrorListener(String grammarName) {
      _grammarName = grammarName;
      _errors = new ArrayList<String>();
   }

   public List<String> getErrors() {
      return _errors;
   }

   @Override
   public void reportAmbiguity(Parser arg0, DFA arg1, int arg2, int arg3,
         boolean arg4, BitSet arg5, ATNConfigSet arg6) {
   }

   @Override
   public void reportAttemptingFullContext(Parser arg0, DFA arg1, int arg2,
         int arg3, BitSet arg4, ATNConfigSet arg5) {
   }

   @Override
   public void reportContextSensitivity(Parser arg0, DFA arg1, int arg2,
         int arg3, int arg4, ATNConfigSet arg5) {
   }

   @Override
   public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
         int line, int charPositionInLine, String msg, RecognitionException e) {
      String error = _grammarName + ": line " + line + ":" + charPositionInLine + ": " + msg;
      _errors.add(error);
   }

}
