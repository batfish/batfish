package org.batfish.smt.utils;


import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PathRegexes {

    private Pattern _dstRegex;

    private Pattern _notDstRegex;

    private Pattern _ifaceRegex;

    private Pattern _notIfaceRegex;

    private Pattern _srcRegex;

    private Pattern _notSrcRegex;

    public PathRegexes(HeaderLocationQuestion q) {
        try {
            _dstRegex = (q.getFinalNodeRegex() == null ? null : Pattern.compile(q.getFinalNodeRegex()));
            _notDstRegex = (q.getNotFinalNodeRegex() == null ? null : Pattern.compile(q.getNotFinalNodeRegex()));
            _ifaceRegex = (q.getFinalIfaceRegex() == null ? null : Pattern.compile(q.getFinalIfaceRegex()));
            _notIfaceRegex = (q.getNotFinalIfaceRegex() == null ? null : Pattern.compile(q.getNotFinalIfaceRegex()));
            _srcRegex = (q.getIngressNodeRegex() == null ? null : Pattern.compile(q.getIngressNodeRegex()));
            _notSrcRegex = (q.getNotIngressNodeRegex() == null ? null : Pattern.compile(q.getNotIngressNodeRegex()));
        } catch (PatternSyntaxException e) {
            throw new BatfishException(String.format("One of the supplied regexes  is not a " +
                    "valid java regex."), e);
        }
    }

    public Pattern getDstRegex() {
        return _dstRegex;
    }

    public Pattern getNotDstRegex() {
        return _notDstRegex;
    }

    public Pattern getIfaceRegex() {
        return _ifaceRegex;
    }

    public Pattern getNotIfaceRegex() {
        return _notIfaceRegex;
    }

    public Pattern getSrcRegex() {
        return _srcRegex;
    }

    public Pattern getNotSrcRegex() {
        return _notSrcRegex;
    }
}
