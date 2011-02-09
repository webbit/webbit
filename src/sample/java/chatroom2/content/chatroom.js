// Socket reference.
var chatServer;

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
        chatServer.login(username);
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
    chatServer = new WebbitSocket('/chatsocket', {
        onopen: function() {
            logText('* Connected!'); login();
        },
        onclose: function() {
            logText('* Disconnected');
        },
        say: function(username, message) {
            logText("[" + username +"] " + message);
        },
        join: function(username) {
            logText("* User '" + username +"' joined.");
        },
        leave: function(username) {
            logText("* User '" + username +"' left.");
        },
    });

    // wire up text input event
    var entry = document.getElementById('entry');
    entry.onkeypress = function(e) {
        if (e.keyCode == 13) { // enter key pressed
            var text = entry.value;
            if (text) {
                chatServer.say(text);
            }
            entry.value = '';
        }
    };
}

// Connect on load.
window.onload = connect;
