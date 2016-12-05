"""
Created by Ari Fogel

Based heavily on CausedException by user Alfe on code.activestate.com
CausedException Original Source: http://code.activestate.com/recipes/578252-python-exception-chains-or-trees/?in=user-4182236

The MIT License (MIT)
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
"""
#!/usr/bin/env python

import traceback
import re
import sys
from util import sublist

class BatfishException(Exception):
    def __init__(self, *args, **kwargs):
        if len(args) == 1 and not kwargs and isinstance(args[0], Exception):
            # we shall just wrap a non-caused exception
            self.stack = (
                traceback.format_stack()[:-2] +
                traceback.format_tb(sys.exc_info()[2]))
            # reverse the stack
            self.stack.reverse()
            # ^^^ let's hope the information is still there; caller must take
            #     care of this.
            self.wrapped = args[0]
            self.cause = ()
            super(BatfishException, self).__init__(repr(args[0]))
            # ^^^ to display what it is wrapping, in case it gets printed or similar
            return
        self.wrapped = None
        self.stack = traceback.format_stack()[:-1]  # cut off current frame
        
        # reverse the stack
        self.stack.reverse()
        try:
            cause = kwargs['cause']
            del kwargs['cause']
        except:
            cause = ()
        self.cause = cause if isinstance(cause, tuple) else (cause,)
        super(BatfishException, self).__init__(*args, **kwargs)

    def causeTree(self, indentation='  ', alreadyMentionedTree=[]):
        exc = self if self.wrapped is None else self.wrapped
        for line in traceback.format_exception_only(exc.__class__, exc):
            yield line
        yield "Traceback (most recent call last):\n"
        for i, line in enumerate(self.stack):
            remaining = self.stack[i:]
            yield line
            if sublist(remaining, alreadyMentionedTree):
                remainingSize = len(remaining) - 1
                if remainingSize > 0:
                    yield "  ... (%d frame%s repeated)\n" % (
                        remainingSize, "" if remainingSize == 1 else "s")
                break
        if self.cause:
            yield ("caused by: %d exception%s\n" %
                (len(self.cause), "" if len(self.cause) == 1 else "s"))
            for causePart in self.cause:
                for line in causePart.causeTree(indentation, self.stack):
                    yield re.sub(r'([^\n]*\n)', indentation + r'\1', line)

    def write(self, stream=None, indentation='  '):
        stream = sys.stderr if stream is None else stream
        if stream != sys.stdout:
            sys.stdout.flush() 
        for line in self.causeTree(indentation):
            stream.write(line)
