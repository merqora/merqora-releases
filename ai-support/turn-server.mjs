import { createServer } from 'turn-server';

const server = createServer({
  auth: {
    mechanism: 'long-term',
    realm: 'mercora.local',
    credentials: { mercora: '***REMOVED***' }
  },
  relay: {
    ip: '0.0.0.0',
    externalIp: '192.168.1.16'
  }
});

server.on('listening', (info) => {
  console.log(`âœ“ TURN server on ${info.address}:${info.port}/${info.transport}`);
});

server.on('error', (err) => {
  console.error('TURN error:', err);
});

server.listen({ port: 3479 });
console.log('TURN server starting...');
