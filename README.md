clipcaster
==========
ClipCaster is a free and open source proof of concept app that shows how easily any installed app can read passwords when they're used from password management applications.

Screenshots
-----------
LastPass presents credential options when it detects a fillable page

![](https://raw.githubusercontent.com/activems/clipcaster/master/screenshots/ClipCasterFB_LP_Dialog_half.png)

Once credentials are chosen, we have them (see notification bar)

![](https://raw.githubusercontent.com/activems/clipcaster/master/screenshots/ClipCasterFB_creds_ticker_half.png)

We post them as a notification

![](https://raw.githubusercontent.com/activems/clipcaster/master/screenshots/ClipCasterFB_creds_notif_half.png)

What
----
ClipCaster is an implementation of the concept in "Hey, You, Get Off of My Clipboard - On How Usability Trumps Security in Android Password Managers" (http://fc13.ifca.ai/proc/4-2.pdf), specifically for the LastPass/Chrome combination.

Scope of ClipCaster
---------------------------
Finds credentials when using: 
    LastPass 'fill-in' feature (Android version at least 4.3)
    KeePassDroid

but most, if not all, other password managers are vulnerable to this technique. See 'Scope of Vulnerability' below.

We chose LastPass as A) we were using it personally and B) the fill-in feature we targeted doesn't tell the user it uses the clipboard internally.

How
---
To provide the filling in for chrome, LastPass copies to the clipboard a chunk of javascript which it then pastes to the address bar of Chrome. The user hits enter and it executes. Most of the javascript is finding out which fields are the username/password fields. Embedded in the javascript is the username and password, encoded in base64. 

Any installed application can be notified of clipboard changes. Once the javascript is received, it's a simple matter of finding the encoded data and decoding it using standard Android libraries.

Why
------
Hopefully this app will show people that by using these applications they are trading their security for usability. This decision is every individual person's to make, and it should be an informed one.

Disclosure
----------
As stated earlier, this is not an original exploit. Regardless, we disclosed our application to LastPass and they responded with the following:

> This is a well-known issue, but is no worse than users manually copying and pasting credentials. App fill just automates the copy/paste process to provide a much better UX. Unfortunately, until Android introduces a secure clipboard feature, or allows direct integration with apps and the browser, there isn't much we can do about this in our app. In the meanwhile, we would recommend that users concerned about this issue not use apps they do not trust, not install apps from untrusted sources, or use the LastPass keyboard/input method to fill logins directly, which does not use the clipboard.


License
-------
All source code written for the ClipCaster app is licensed under the FreeBSD license, as written below.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of the FreeBSD Project.
