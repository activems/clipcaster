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

package com.actisec.clipcaster;

import android.content.Context;
import android.content.pm.PackageManager;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xiao on 19/12/14.
 */
public class TestUtilsStatic {
    public static class TestUtils {
        private final Context mContext;

        public TestUtils(Context context) throws Exception {
            if(!context.getPackageName().endsWith("test")) {
                mContext = context.createPackageContext("com.actisec.clipcaster.test",
                        Context.CONTEXT_IGNORE_SECURITY);
            } else {
                mContext = context;
            }

        }

        public Source readSource(int resourceId) throws IOException {
            return TestUtilsStatic.readSource(mContext,resourceId);
        }
        public String readString(int resourceId) throws IOException {
            return TestUtilsStatic.readString(mContext,resourceId);
        }
    }

    private TestUtilsStatic () {}
    public static Source readSource(Context context, int resourceId) throws IOException {
        final String rawString = readString(context, resourceId);
        return new ObjectMapper().readValue(rawString, Source.class);
    }

    public static String readString(Context context, int resourceId) throws IOException {
        InputStream is = context.getResources().openRawResource(resourceId);
        return IOUtils.toString(is);
    }

}
