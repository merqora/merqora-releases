var turn = require('node-turn');
var server = new turn({
  authMech: 'long-term',
  realm: 'rendly.local',
  credentials: {
    rendly: '***REMOVED***'
  },
  listeningPort: 3479,
  listeningIps: ['0.0.0.0'],
  relayIps: ['192.168.1.16'],
  minPort: 49152,
  maxPort: 65535
});
server.start();
