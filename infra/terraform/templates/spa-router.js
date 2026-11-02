// Attached only to the static frontend behavior. API errors retain their status.
function handler(event) {
    var request = event.request;
    var last = request.uri.substring(request.uri.lastIndexOf('/') + 1);
    if (request.uri.indexOf('/api/') === 0) return request;
    if (request.uri.endsWith('/') || last.indexOf('.') === -1) {
        request.uri = '/index.html';
    }
    return request;
}
