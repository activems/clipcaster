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

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.actisec.clipcaster.CredHandler;
import com.actisec.clipcaster.util.EnvironmentUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by xiao on 11/11/14.
 */
public abstract class PackageSpecificClipParser extends AbstractClipParser {

    private List<String> mPackages;

    protected PackageSpecificClipParser(String ... packages) {
        mPackages = Arrays.asList(packages);
    }

    @Override
    ScrapedCredentials getCreds(Context context, String contents)
    {
        List<String> list = EnvironmentUtil.getRunningProcesses(context);
        for (int i = 0; i < list.size(); i++) {
            String s =  list.get(i);
            if(mPackages.contains(s)){
                Log.d(context.getApplicationInfo().name, "Found " + s + " at position " + i);
                return getCreds(context,contents,list,i);
            }
        }
        return null;
    }

    abstract ScrapedCredentials getCreds(Context context, String contents, List<String> matchedPackage, int orderOfTask);
}
