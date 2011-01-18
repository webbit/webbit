// Socket reference.
var ws;

// Log text to main window.
function logText(msg) {
    var textArea = document.getElementById('chatlog');
    textArea.value = textArea.value + msg + '\n';
    textArea.scrollTop = textArea.scrollHeight; // scroll into view
}

// Perform login: Ask user for name, and send message to socket.
function login() {
    var defaultUsername = (window.localStorage && window.localStorage.username) || 'yourname';
    var username = prompt('Choose a username', defaultUsername);
    if (username) {
        if (window.localStorage) { // store in browser localStorage, so we remember next next
            window.localStorage.username = username;
        }
        ws.send('LOGIN|' + username);
        document.getElementById('entry').focus();
    } else {
        ws.close();
    }
}

// Connect to socket and setup events.
function connect() {
    // clear out any cached content
    document.getElementById('chatlog').value = '';

    // connect to socket
    logText('* Connecting...');
    ws = new WebSocket('ws://' + document.location.host + '/chatsocket');
    ws.onopen    = function(e) { logText('* Connected!'); login(); };
    ws.onclose   = function(e) { logText('* Disconnected'); };
    ws.onerror   = function(e) { logText('* Unexpected error'); };
    ws.onmessage = function(e) { logText(e.data); };

    // wire up text input event
    var entry = document.getElementById('entry');
    entry.onkeypress = function(e) {
        if (e.keyCode == 13) { // enter key pressed
            var text = entry.value;
            if (text) {
                ws.send('SAY|' + text);
            }
            entry.value = '';
        }
    };
}

// Connect on load.
window.onload = connect;
