/*
 * Copyright (c) 2014 Xiao Bao Clark
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package com.actisec.clipcaster.util;

import android.content.Context;
import android.os.Handler;

import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
* Created by xiao on 19/12/14.
*/
public class JavaScript {
    public final String source;
    public final char[] sourceChars;


    public JavaScript(String source) {
        this.source = source;
        sourceChars = source.toCharArray();
    }

    public int findClosing(int openPos, char opening, char closing) {
        int closePos = openPos;
        int counter = 1;
        while (counter > 0) {
            if(closePos >= sourceChars.length){
                return -1;
            }

            char c = sourceChars[++closePos];
            if (c == opening) {
                counter++;
            } else if (c == closing) {
                counter--;
            }
        }
        return closePos;
    }

    public int findClosingParen(int openPos){
        return findClosing(openPos, '(', ')');
    }
    public int findClosingBrace(int openPos){
        return findClosing(openPos, '{', '}');
    }

    public String getFunction(String functionName) {
        int funcNameIdx = source.indexOf(functionName);
        int funcParamStartIdx = source.indexOf('(',funcNameIdx + 1);
        int funcParamEndIdx = findClosingParen(funcParamStartIdx);
        int funcBodyStartIdx = source.indexOf('{',funcParamEndIdx + 1);
        int funcBodyEndIdx = findClosingBrace(funcBodyStartIdx);

        return source.substring(funcNameIdx,funcBodyEndIdx + 1);
    }

    public String appendParameter(String functionName, String parameterName){
        int currentNameIdx = source.indexOf(functionName);

        int funcParamStartIdx = source.indexOf('(',currentNameIdx + 1);
        int funcParamEndIdx = findClosingParen(funcParamStartIdx);

        int nextBracketIdx = source.indexOf('{',funcParamEndIdx + 1);
        if(nextBracketIdx != -1 && source.substring(funcParamEndIdx + 1,nextBracketIdx).matches("\\s*")){
            // It's a definition
            return source.substring(0,funcParamEndIdx) + "," + parameterName  + source.substring(funcParamEndIdx);
        }
        return null;
    }

    public String[] getSignatureParams(String functionName){
        int currentNameIdx = source.indexOf(functionName);

        int funcParamStartIdx = source.indexOf('(',currentNameIdx + 1);
        int funcParamEndIdx = findClosingParen(funcParamStartIdx);

        int nextBracketIdx = source.indexOf('{',funcParamEndIdx + 1);
        if(nextBracketIdx != -1 && source.substring(funcParamEndIdx + 1,nextBracketIdx).matches("\\s*")){
            // It's a definition
            return source.substring(funcParamStartIdx + 1,funcParamEndIdx).split("\\s*,\\s*");
        }

        return null;
    }

    public String[] getParams(String functionName, int ordinal){

        int currentNameIdx = -1;
        for(int currentOrdinal = 0;currentOrdinal <= ordinal; currentOrdinal ++){
            currentNameIdx = source.indexOf(functionName, ++currentNameIdx);

            int funcParamStartIdx = source.indexOf('(',currentNameIdx + 1);
            int funcParamEndIdx = findClosingParen(funcParamStartIdx);

            int nextBracketIdx = source.indexOf('{',funcParamEndIdx + 1);
            if(nextBracketIdx != -1 && source.substring(funcParamEndIdx + 1,nextBracketIdx).matches("\\s*")){
                // It's a definition, not a call site
                --currentOrdinal;
                continue;
            }

            if(currentOrdinal == ordinal){
                return source.substring(funcParamStartIdx + 1,funcParamEndIdx).split("\\s*,\\s*");
            }
        }

        return null;
    }


    public static void evaluate(Context context, String script, final JsCallback resultCallback) {
        new JsEvaluator(context).evaluate(script, resultCallback);
    }


    public static void callFunction(Context context, String s, JsCallback jsCallback, String functionName, Object... params) {
        new JsEvaluator(context).callFunction(s, jsCallback, functionName, params);
    }

}
