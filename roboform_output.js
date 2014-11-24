(function() {
    try {
        var login = 'j.doe@actisec.com';
        var pass = 's4fep4ssword';
        var inputs = document.getElementsByTagName('input');
        var inp = 0;
        for (inp in inputs) {
            if (inputs[inp].type == 'password') {
                inputs[inp].value = pass;
                break;
            }
        }
        var li = false;
        var stored_ind = inp;
        if (inp > 0) {
            for (inp; inp >= 0; inp--)
                if (inputs[inp].type == 'text' || inputs[inp].type == 'email' || inputs[inp].type == 'number' || inputs[inp].type == 'tel') {
                    inputs[inp].value = login;
                    li = true;
                }
        }
        if (!li) {
            for (stored_ind; stored_ind < inputs.length; stored_ind)
                if (inputs[stored_ind].type == 'text' || inputs[stored_ind].type == 'email' || inputs[stored_ind].type == 'number' || inputs[stored_ind].type == 'tel') {
                    inputs[stored_ind].value = login;
                    li = true;
                }
        }
    } catch (e) {
        alert(e);
    };
})();