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
* A class that can partially dissect a JavaScript program
 *
*/
public class JavaScript {
    /**
     * The JavaScript program
     */
    public final String source;
    /**
     * The JavaScript program as a char[] array
     */
    public final char[] sourceChars;


    /**
     * Constructor
     *
     * @param source The JavaScript program to be dissected
     */
    public JavaScript(String source) {
        this.source = source;
        sourceChars = source.toCharArray();
    }

    /**
     * Finds the position of a 'closing' character that matches the 'opening'
     * character.
     *
     * For example, if the program passed into the constructor was
     *
     * {@code foo(bar(),2)}
     *
     * the call
     *
     * {@code findClosing(3, '(', ')'); }
     *
     * would return {@code 11}, the index of the ')' after '2'.
     *
     * @param openPos The index of the opening character to match. The character at this index
     *                must be {@code opening}
     * @param opening The opening character of the matching pair
     * @param closing The closing character of the matching pai
     * @return the index of the matching character, or -1 if no matching character could be found
     */
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

    /**
     * Finds the index of a matching ')', given the position of a '('
     * @param openPos The index of the opening parenthesis to match. The character at this
     *                index must be '('
     * @return the index of the matching parenthesis, or -1 if no match is found
     */
    public int findClosingParen(int openPos){
        return findClosing(openPos, '(', ')');
    }
    /**
     * Finds the index of a matching '}', given the position of a '{'
     * @param openPos The index of the opening brace to match. The character at this
     *                index must be '{'
     * @return the index of the matching brace, or -1 if no match is found
     */
    public int findClosingBrace(int openPos){
        return findClosing(openPos, '{', '}');
    }

    /**
     * Gets a name function.
     *
     * For example, if the program passed into the constructor was
     *
     * {@code
     *      var foo = function(arg1){
     *          return bar(arg1);
     *      };
     *      foo("lorumipsum");
     * }
     *
     * the call
     *
     * {@code getFunction("foo"); }
     *
     * would return
     * {@code
     *      foo = function(arg1){
     *          return bar(arg1);
     *      }
     * }
     *
     *
     * @param functionName The name of the variable holding the function
     * @return The full function, including the function name and closing brace of
     *          the function body
     */
    public String getFunction(String functionName) {
        int funcNameIdx = source.indexOf(functionName);
        int funcParamStartIdx = source.indexOf('(',funcNameIdx + 1);
        int funcParamEndIdx = findClosingParen(funcParamStartIdx);
        int funcBodyStartIdx = source.indexOf('{',funcParamEndIdx + 1);
        int funcBodyEndIdx = findClosingBrace(funcBodyStartIdx);

        return source.substring(funcNameIdx,funcBodyEndIdx + 1);
    }

    /**
     * Creates a copy of the JavaScript program, adds a parameter to a given
     * named function, and returns the modified program
     * @param functionName The name of the variable holding the function to be modified
     * @param parameterName The name of the parameter to add to the function signature
     * @return The modified JavaScript program, or null if the function could not be found
     */
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

    /**
     * For a given named function gets the names of all the parameters declared in
     * the function signatured
     * @param functionName The name of the variable holding the function
     * @return The names of the parameters declared in function 'functionName', or null
     *          if the function cannot be found.
     */
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

    /**
     * Returns the arguments passed into a call to a named function.
     *
     * @param functionName The name of the variable holding the function
     * @param ordinal Determines which call to 'functionName' will be analysed
     * @return The arguments passed into the 'ordinal'th call to 'functionName'.
     *          Returns null if the function cannot be found, or the number of
     *          calls is less than 'ordinal'
     */
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


    /**
     * A convenience method for asynchronously running a given JavaScript program
     * @param context An Android Context
     * @param script The JavaScript program to be run
     * @param resultCallback The object that will be notified when the script has been evaluated
     */
    public static void evaluate(Context context, String script, final JsCallback resultCallback) {
        new JsEvaluator(context).evaluate(script, resultCallback);
    }

}
