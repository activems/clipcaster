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

package com.actisec.clipcaster.clipcaster;

import android.content.Context;

import org.jetbrains.annotations.Nullable;

/**
 * Created by xiao on 11/11/14.
 */
public interface ClipParser {
    void onClip(Context context, CredHandler handler, String contents);

    /**
     * Scraped credentials.
     *
     * Either user and/or pass is not null, OR
     * unknown is not null
     */
    static class Credentials {
        @Nullable
        public String user;
        @Nullable
        public String pass;
        @Nullable
        public String unknown;
        @Nullable
        public String sourcePackage;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Credentials{").append('\n');
            sb.append("user='").append(user).append('\'')
                    .append('\n');
            sb.append(", pass='").append(pass).append('\'')
                    .append('\n');
            sb.append(", unknown='").append(unknown).append('\'')
                    .append('\n');
            sb.append(", sourcePackage='").append(sourcePackage).append('\'')
                    .append('\n');
            sb.append('}');
            return sb.toString();
        }

        public Credentials(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }


        public Credentials(String user, String pass, String unknown, String sourcePackage) {
            this.user = user;
            this.pass = pass;
            this.unknown = unknown;
            this.sourcePackage = sourcePackage;
        }

    }
}
