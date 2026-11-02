const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const path = require('node:path');
const context = {};
vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../templates/spa-router.js'), 'utf8'), context);
for (const uri of ['/booklist', '/detail/42', '/oauth/success', '/']) {
  const request = context.handler({ request: { uri, querystring: { token: { value: 'fixture' } } } });
  assert.equal(request.uri, '/index.html');
  assert.equal(request.querystring.token.value, 'fixture');
}
for (const uri of ['/assets/main.js', '/library.svg', '/api/v1/books', '/api/v1/health']) {
  assert.equal(context.handler({ request: { uri } }).uri, uri);
}
console.log('SPA routes, OAuth query preservation, assets and API paths passed');
