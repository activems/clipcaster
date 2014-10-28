clipcaster
==========

A LastPass clipboard password sniffer. 

An implementation of the concept in "Hey, You, Get Off of My Clipboard - On How Usability Trumps Security in Android Password Managers" (http://fc13.ifca.ai/proc/4-2.pdf), specifically for the LastPass/Chrome combination

What
----

On Android devices >4.3, LastPass offers to fill in credentials from within a third-party browser, such as Chrome. It does this in such a way that any installed application can read them, no permissions required.

We read this and show the user the username/password that was entered.

How
---

To provide the filling in for chrome, LastPass copies to the clipboard a chunk of javascript. Embedded in this javascript is the username and password, encoded in base64. 

Any installed application can be notified of clipboard changes. Once the javascript is received, it's a simple matter of finding the encoded data and decoding it using standard Android libraries.

Why
---

Consumers have a right to know what aspects of their security they're giving up when they enable these features. LastPass doesn't mention, or at the very least doesn't make as obvious it should, that any installed application can read the username and password when they fill-in their credentials via third-party browsers.
