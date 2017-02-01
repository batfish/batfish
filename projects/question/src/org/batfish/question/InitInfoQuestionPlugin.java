package org.batfish.question;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class InitInfoQuestionPlugin extends QuestionPlugin {

   public static class InitInfoAnswerElement implements AnswerElement {

      private SortedMap<String, ParseStatus> _parseStatus;

      private SortedMap<String, Warnings> _warnings;

      @JsonCreator
      public InitInfoAnswerElement() {
         _parseStatus = new TreeMap<>();
         _warnings = new TreeMap<>();
      }

      public SortedMap<String, ParseStatus> getParseStatus() {
         return _parseStatus;
      }

      public SortedMap<String, Warnings> getWarnings() {
         return _warnings;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         final StringBuilder sb = new StringBuilder();
         int pedanticCount = 0;
         int redFlagCount = 0;
         int unimplementedCount = 0;
         int failedCount = 0;
         int passedCount = 0;
         int unrecognizedCount = 0;
         if (!_warnings.isEmpty()) {
            sb.append("DETAILED WARNINGS\n");
            for (String name : _warnings.keySet()) {
               sb.append("  " + name + ":\n");
               for (Warning warning : _warnings.get(name)
                     .getRedFlagWarnings()) {
                  sb.append("    RedFlag " + warning.getTag() + " : "
                        + warning.getText() + "\n");
                  redFlagCount++;
               }
               for (Warning warning : _warnings.get(name)
                     .getUnimplementedWarnings()) {
                  sb.append("    Unimplemented " + warning.getTag() + " : "
                        + warning.getText() + "\n");
                  unimplementedCount++;
               }
               for (Warning warning : _warnings.get(name)
                     .getPedanticWarnings()) {
                  sb.append("    Pedantic " + warning.getTag() + " : "
                        + warning.getText() + "\n");
                  pedanticCount++;
               }
            }

         }
         sb.append("PARSING SUMMARY\n");
         for (Entry<String, ParseStatus> e : _parseStatus.entrySet()) {
            String hostname = e.getKey();
            ParseStatus status = e.getValue();
            switch (status) {
            case FAILED:
               sb.append(" " + hostname + ": failed to parse\n");
               failedCount++;
               break;

            case UNRECOGNIZED:
               sb.append("  " + hostname
                     + ": contained at least one unrecognized line\n");
               unrecognizedCount++;
               break;

            case PASSED:
               passedCount++;
               break;

            default:
               break;
            }
         }
         sb.append("STATISTICS\n");
         sb.append("  Total warnings:\n");
         if (redFlagCount > 0) {
            sb.append("    Red Flag: " + redFlagCount + "\n");
         }
         if (unimplementedCount > 0) {
            sb.append("    Unimplemented: " + unimplementedCount + "\n");
         }
         if (pedanticCount > 0) {
            sb.append("    Pedantic: " + pedanticCount + "\n");
         }
         sb.append("  Parsing results:\n");
         if (passedCount > 0) {
            sb.append("    Parsed successfully: " + passedCount + "\n");
         }
         if (unrecognizedCount > 0) {
            sb.append("    Contained unrecognized line(s): " + unrecognizedCount
                  + "\n");
         }
         if (failedCount > 0) {
            sb.append("    Failed to parse: " + failedCount + "\n");
         }
         return sb.toString();
      }

      public void setParseStatus(SortedMap<String, ParseStatus> parseStatus) {
         _parseStatus = parseStatus;
      }

      public void setWarnings(SortedMap<String, Warnings> warnings) {
         _warnings = warnings;
      }

   }

   public static class InitInfoAnswerer extends Answerer {

      public InitInfoAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public InitInfoAnswerElement answer() {
         InitInfoQuestion question = (InitInfoQuestion) _question;
         _batfish.checkConfigurations();
         InitInfoAnswerElement answerElement = new InitInfoAnswerElement();
         ParseVendorConfigurationAnswerElement parseAnswer = _batfish
               .getParseVendorConfigurationAnswerElement();
         ConvertConfigurationAnswerElement convertAnswer = _batfish
               .getConvertConfigurationAnswerElement();
         if (!question._summary) {
            SortedMap<String, Warnings> warnings = answerElement._warnings;
            warnings.putAll(parseAnswer.getWarnings());
            convertAnswer.getWarnings().forEach((hostname, convertWarnings) -> {
               Warnings combined = warnings.get(hostname);
               if (combined == null) {
                  warnings.put(hostname, convertWarnings);
               }
               else {
                  combined.getPedanticWarnings()
                        .addAll(convertWarnings.getPedanticWarnings());
                  combined.getRedFlagWarnings()
                        .addAll(convertWarnings.getRedFlagWarnings());
                  combined.getUnimplementedWarnings()
                        .addAll(convertWarnings.getUnimplementedWarnings());
               }
            });
         }
         answerElement._parseStatus = parseAnswer.getParseStatus();
         for (String failed : convertAnswer.getFailed()) {
            answerElement._parseStatus.put(failed, ParseStatus.FAILED);
         }
         return answerElement;
      }
   }

   // <question_page_comment>
   /**
    * Outputs results of test-rig initialization.
    *
    * @type InitInfo onefile
    *
    * @example bf_answer("initinfo", summary=True") Get summary information
    *          about test-rig initialization
    */
   public static class InitInfoQuestion extends Question {

      private static final String SUMMARY_VAR = "summary";

      private boolean _summary;

      public InitInfoQuestion() {
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return BfConsts.Q_INIT_INFO;
      }

      @JsonProperty(SUMMARY_VAR)
      public boolean getSummary() {
         return _summary;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public String prettyPrint() throws JsonProcessingException {
         return getName() + " summary=" + _summary;
      }

      @Override
      public void setJsonParameters(JSONObject parameters) {
         super.setJsonParameters(parameters);
         Iterator<?> paramKeys = parameters.keys();
         while (paramKeys.hasNext()) {
            String paramKey = (String) paramKeys.next();
            if (isBaseParamKey(paramKey)) {
               continue;
            }
            try {
               switch (paramKey) {
               case SUMMARY_VAR:
                  setSummary(parameters.getBoolean(paramKey));
                  break;
               default:
                  throw new BatfishException("Unknown key in "
                        + getClass().getSimpleName() + ": " + paramKey);
               }
            }
            catch (JSONException e) {
               throw new BatfishException("JSONException in parameters", e);
            }
         }
      }

      @JsonProperty(SUMMARY_VAR)
      public void setSummary(boolean summary) {
         _summary = summary;
      }

   }

   @Override
   protected InitInfoAnswerer createAnswerer(Question question,
         IBatfish batfish) {
      return new InitInfoAnswerer(question, batfish);
   }

   @Override
   protected InitInfoQuestion createQuestion() {
      return new InitInfoQuestion();
   }

}
