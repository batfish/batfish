package org.batfish.smt.answers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.smt.VerificationResult;

import java.util.Map;

public class SmtManyAnswerElement implements AnswerElement {

    protected Map<String,VerificationResult> _result;

    public Map<String,VerificationResult> getResult() {
        return _result;
    }

    public void setResult(Map<String,VerificationResult> result) {
        _result = result;
    }

    @Override
    public String prettyPrint() {
        if (_result != null) {
            for (Map.Entry<String, VerificationResult> e : _result.entrySet()) {
                VerificationResult r = e.getValue();
                if (!r.getVerified()) {
                    return r.prettyPrint(e.getKey());
                }
            }
        }
        return "\nVerified";
    }

}
