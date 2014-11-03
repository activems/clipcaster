clipcaster
==========

A LastPass clipboard password sniffer. 

An implementation of the concept in "Hey, You, Get Off of My Clipboard - On How Usability Trumps Security in Android Password Managers" (http://fc13.ifca.ai/proc/4-2.pdf), specifically for the LastPass/Chrome combination

What
----

On Android devices >4.3, LastPass offers to fill in credentials from within a third-party browser, such as Chrome. It does this in such a way that any installed application can read them, no permissions required.

We parse the contents of the clipboard and show the user the username/password that was entered.


How
---

To provide the filling in for chrome, LastPass copies to the clipboard a chunk of javascript. Embedded in this javascript is the username and password, encoded in base64. 

Any installed application can be notified of clipboard changes. Once the javascript is received, it's a simple matter of finding the encoded data and decoding it using standard Android libraries.

Scope
-----

This doesn't affect just the LastPass app. Any password manager functionality that copies credentials to the clipboard is open to this interception. We chose LastPass as A) we were using it personally and B) the fill-in feature we targeted doesn't tell the user it uses the clipboard internally.

Why
---

Consumers have a right to know what aspects of their security they're giving up when they enable these features. LastPass doesn't mention, or at the very least doesn't make as obvious it should, that any installed application can read the username and password when they fill-in their credentials via third-party browsers.

Disclosure
----------

As stated earlier, this is not an original exploit. Regardless, we disclosed our application to LastPass and they responded with the following:

> This is a well-known issue, but is no worse than users manually copying and pasting credentials. App fill just automates the copy/paste process to provide a much better UX. Unfortunately, until Android introduces a secure clipboard feature, or allows direct integration with apps and the browser, there isn't much we can do about this in our app. In the meanwhile, we would recommend that users concerned about this issue not use apps they do not trust, not install apps from untrusted sources, or use the LastPass keyboard/input method to fill logins directly, which does not use the clipboard.


Screenshots
-----------

![](https://raw.githubusercontent.com/activems/clipcaster/master/screenshots/ClipCasterFB_LP_Dialog_half.png) ![](https://raw.githubusercontent.com/activems/clipcaster/master/screenshots/ClipCasterFB_creds_ticker_half.png) ![](https://raw.githubusercontent.com/activems/clipcaster/master/screenshots/ClipCasterFB_creds_notif_half.png) ![](https://raw.githubusercontent.com/activems/clipcaster/master/screenshots/ClipCasterAbout_half.png)
