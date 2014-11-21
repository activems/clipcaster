script: (function() {
    (function() {
        lpcurruser = '';
        lpcurrpass = '';
        var lploc = '1';
        var lponlyfill = null;

        function lastpass_fixfacebook() {
            if (document.location.href.indexOf("/plugins") != -1) {
                return;
            }
            if (document.getElementById("email")) {
                document.getElementById("email").focus();
            }
            if (document.getElementById("pass_placeholder")) {
                var ph = document.getElementById("pass_placeholder");
                if (ph.style.visibility != "hidden" && ph.style.display != "none") {
                    try {
                        ph.focus();
                    } catch (err) {}
                }
            }
        }
        lastpass_fixfacebook();
        setTimeout("lastpass_fixfacebook();", 500);;
        lpcurruser = '';
        lpcurrpass = '';
    })();
    var l_fs, l_bf = null,
        l_err = false;
    var l_bni = 0,
        l_bnp = 0;
    var l_bte = null,
        l_bpe = null,
        l_cpe = null;
    var l_w;
    var l_d;
    try {
        l_w = window.top;
        l_d = l_w.document;
    } catch (l_e) {
        l_w = window;
        l_d = document;
    }
    var l_iv = function(el, sf) {
        while (el && (!sf || el.tagName != 'FORM')) {
            if (el.hasOwnProperty('style') && (el.style['display'] == 'none' || el.style['visibility'] == 'hidden')) return false;
            else el = el.parentNode;
        }
        return true;
    };
    var l_cpp = /(?:existing|old|curr).*pass/i;
    for (var l_k = -1; l_k < l_w.frames.length; l_k++) {
        if (l_k == -1) {
            l_fs = l_d.getElementsByTagName('form');
        } else {
            try {
                l_w[l_k].document.domain
            } catch (e) {
                console.log(e);
                l_err = true;
                continue;
            }
            l_fs = l_w[l_k].document.getElementsByTagName('form');
        }
        for (var l_i = 0; l_i < l_fs.length; l_i++) {
            if (!l_iv(l_fs[l_i])) continue;
            var l_fe = l_fs[l_i].elements;
            var l_ni = 0,
                l_np = 0;
            var l_te = null,
                l_pe = null;
            for (var l_j = 0; l_j < l_fe.length; l_j++) {
                var l_e = l_fe[l_j];
                if ((l_e.type == 'text' || l_e.type == 'email' || l_e.type == 'tel') && l_iv(l_e, true)) {
                    if (l_ni == 0) {
                        l_te = l_e;
                    }
                    l_ni++;
                }
                if (l_e.type == 'password' && l_iv(l_e, true)) {
                    if (l_np == 0) {
                        l_pe = l_e;
                    }
                    l_np++;
                    if (l_cpp.test(l_e.name) || l_cpp.test(l_e.id)) {
                        l_cpe = l_e;
                    }
                }
            }
            if (l_np == 1) {
                if (!l_bf || (l_ni == 1 && l_bni != 1)) {
                    l_bni = l_ni;
                    l_bnp = l_np;
                    l_bf = l_fs[l_i];
                    l_bte = l_te;
                    l_bpe = l_pe;
                }
            } else if (l_np > 1 && l_cpe) {
                l_bf = l_fs[l_i];
                l_bpe = l_cpe;
            }
        }
    }
    var l_sfv = function(el, v) {
        try {
            var c = true;
            if (el.type == 'select-one' && el.value == v) {
                c = false;
            }
            el.value = v;
            if (c) {
                var evt = el.ownerDocument.createEvent('Events');
                evt.initEvent('change', true, true);
                el.dispatchEvent(evt);
                evt = el.ownerDocument.createEvent('Events');
                evt.initEvent('input', true, true);
                el.dispatchEvent(evt);
            }
        } catch (e) {}
    };
    if (l_bf) {
        var do_fill = true;
        if (do_fill) {
            console.log('fill login form=' + (l_bf.id || l_bf.name));
            if (l_bte) {
                l_sfv(l_bte, decodeURIComponent(escape(atob('ai5kb2VAYWN0aXNlYy5jb20='))));
            }
            l_sfv(l_bpe, decodeURIComponent(escape(atob('czRmZXBhc3N3MHJk'))));
        }
    } else {
        console.log('no form');
    }(function() {
        lpcurruser = '';
        lpcurrpass = '';
        var lploc = '1';
        var lponlyfill = null;

        function lastpass_fixfacebook() {
            if (document.location.href.indexOf("/plugins") != -1) {
                return;
            }
            if (document.getElementById("email")) {
                document.getElementById("email").focus();
            }
            if (document.getElementById("pass_placeholder")) {
                var ph = document.getElementById("pass_placeholder");
                if (ph.style.visibility != "hidden" && ph.style.display != "none") {
                    try {
                        ph.focus();
                    } catch (err) {}
                }
            }
        }
        lastpass_fixfacebook();
        setTimeout("lastpass_fixfacebook();", 500);;
        lpcurruser = '';
        lpcurrpass = '';
    })();
    (function() {
        lpcurruser = '';
        lpcurrpass = '';
        var lploc = '2';
        var lponlyfill = null;

        function lastpass_fixfacebook() {
            if (document.location.href.indexOf("/plugins") != -1) {
                return;
            }
            if (document.getElementById("email")) {
                document.getElementById("email").focus();
            }
            if (document.getElementById("pass_placeholder")) {
                var ph = document.getElementById("pass_placeholder");
                if (ph.style.visibility != "hidden" && ph.style.display != "none") {
                    try {
                        ph.focus();
                    } catch (err) {}
                }
            }
        }
        lastpass_fixfacebook();
        setTimeout("lastpass_fixfacebook();", 500);;
        lpcurruser = '';
        lpcurrpass = '';
    })();
    (function() {
        lpcurruser = '';
        lpcurrpass = '';
        var lploc = '3';
        var lponlyfill = 1;

        function lastpass_fixfacebook() {
            if (document.location.href.indexOf("/plugins") != -1) {
                return;
            }
            if (document.getElementById("email")) {
                document.getElementById("email").focus();
            }
            if (document.getElementById("pass_placeholder")) {
                var ph = document.getElementById("pass_placeholder");
                if (ph.style.visibility != "hidden" && ph.style.display != "none") {
                    try {
                        ph.focus();
                    } catch (err) {}
                }
            }
        }
        lastpass_fixfacebook();
        setTimeout("lastpass_fixfacebook();", 500);;
        lpcurruser = '';
        lpcurrpass = '';
    })();
})(); ////////////////////////////////////////////////////////////////////////////////////////////////////
