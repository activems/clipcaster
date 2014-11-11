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

import android.test.AndroidTestCase;

public class LastPassParserTest extends AndroidTestCase {

    private LastPassParser mParser = new LastPassParser();
    private String formatUserPassToJscript(String encodedUser, String encodedPass) {
        return "(atob('" + encodedUser + "'))))." + "(atob('" + encodedPass + "'))))";
    }
    private ClipParser.ScrapedCredentials innerCredTest(String content) throws Throwable{
        final ClipParser.ScrapedCredentials creds = mParser.getCreds(getContext(), content);
        assertNotNull(creds);
        assertNotNull(creds.user);
        assertNotNull(creds.pass);
        assertFalse(creds.user.isEmpty());
        assertFalse(creds.pass.isEmpty());
        return creds;
    }


    public void testCredGetJustBase64() throws Throwable{
        innerCredTest(formatUserPassToJscript("czRmM3A0c3N3MHJk", "dW9hZmlyZWRyb2lk"));
    }

    public void testCredGetJustBase64WithEquals() throws Throwable{
        innerCredTest(formatUserPassToJscript("bGVhc3VyZS4=", "c3VyZS4="));
    }

    public void testCredGetFullSample() throws Throwable{
        ClipParser.ScrapedCredentials credentials =  innerCredTest(FULL_SAMPLE);
        assertEquals(credentials.pass,"p4ssw0rd");
        assertEquals(credentials.user,"user@example.com");

    }

    private static final String FULL_SAMPLE = "script:(function(){var l_fs, l_bf=null, l_err=false;var l_bni=0, l_bnp=0;var l_bte=null, l_bpe=null, l_cpe=null;var l_w; var l_d; try { l_w=window.top; l_d=l_w.document;} catch (l_e){ l_w=window; l_d=document;}var l_iv=function(el, sf){ while (el&&(!sf||el.tagName != 'FORM')){ if (el.hasOwnProperty('style')&&(el.style['display']=='none'||el.style['visibility']=='hidden')) return false; else el=el.parentNode;} return true;};var l_cpp=/(?:existing|old|curr).*pass/i;for(var l_k=-1; l_k < l_w.frames.length; l_k++){ if(l_k==-1){ l_fs=l_d.getElementsByTagName('form');}else{ try{ l_w[l_k].document.domain } catch(e){console.log(e); l_err=true; continue;} l_fs=l_w[l_k].document.getElementsByTagName('form');} for (var l_i=0; l_i < l_fs.length; l_i++){ if (!l_iv(l_fs[l_i])) continue; var l_fe=l_fs[l_i].elements; var l_ni=0, l_np=0; var l_te=null, l_pe=null; for (var l_j=0; l_j < l_fe.length; l_j++){ var l_e=l_fe[l_j]; if ((l_e.type=='text'||l_e.type=='email'||l_e.type=='tel')&&l_iv(l_e, true)){ if (l_ni==0){ l_te=l_e;} l_ni++;} if (l_e.type=='password'&&l_iv(l_e, true)){ if (l_np==0){ l_pe=l_e;} l_np++; if (l_cpp.test(l_e.name)||l_cpp.test(l_e.id)){ l_cpe=l_e;} } } if (l_np==1){ if (!l_bf||(l_ni==1&&l_bni != 1)){ l_bni=l_ni; l_bnp=l_np; l_bf=l_fs[l_i]; l_bte=l_te; l_bpe=l_pe;} } else if (l_np > 1&&l_cpe){ l_bf=l_fs[l_i]; l_bpe=l_cpe;} }}var l_sfv=function(el, v){ try { var c=true; if (el.type=='select-one'&&el.value==v){ c=false;} el.value=v; if (c){ var evt=el.ownerDocument.createEvent('Events'); evt.initEvent('change', true, true); el.dispatchEvent(evt); evt=el.ownerDocument.createEvent('Events'); evt.initEvent('input', true, true); el.dispatchEvent(evt);} } catch(e){}};if (l_bf){ var do_fill=true; if (do_fill){ console.log('fill login form=' + (l_bf.id||l_bf.name)); if (l_bte){ l_sfv(l_bte, decodeURIComponent(escape(atob('dXNlckBleGFtcGxlLmNvbQ=='))));} l_sfv(l_bpe, decodeURIComponent(escape(atob('cDRzc3cwcmQ='))));}} else { console.log('no form');}})();////////////////////////////////////////////////////////////////////////////////////////////////////";
}