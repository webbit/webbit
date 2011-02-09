function WebbitSocket(path, target) {
    var self = this;
    var ws = new WebSocket('ws://' + document.location.host + path);
    ws.onclose = function() {
        target.onclose && target.onclose();
    };
    ws.onmessage = function(e) {
        var msg = JSON.parse(e.data);
        if (msg.exports) {
            msg.exports.forEach(function(name) {
                self[name] = function() {
                    var outgoing = {
                        action: name,
                        args: Array.prototype.slice.call(arguments)
                    };
                    ws.send(JSON.stringify(outgoing));
                };
            })
            target.onopen && target.onopen();
        } else {
            var action = target[msg.action];
            if (typeof action === 'function') {
                action.apply(target, msg.args);
            } else {
                // TODO: ?
            }
        }
    };
}