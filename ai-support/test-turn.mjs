import { connect, getPublicIP } from 'turn-server';

// Try to connect to our TURN server
connect('turn:192.168.1.16:3479?transport=udp', {
  username: 'rendly',
  password: '***REMOVED***',
  autoRefresh: false,
}, (err, socket) => {
  if (err) {
    console.error('TURN connect error:', err);
    process.exit(1);
  }
  console.log('Connected to TURN server!');
  
  socket.allocate({ lifetime: 600 });
  
  socket.on('allocate:success', (msg) => {
    const relay = msg.getAttribute(0x0016);
    console.log('Allocation success! Relay address:', relay.ip, relay.port);
    process.exit(0);
  });
  
  socket.on('allocate:error', (msg) => {
    console.error('Allocation error:', msg);
    process.exit(1);
  });
  
  socket.on('error', (err) => {
    console.error('TURN error:', err);
  });
  
  setTimeout(() => {
    console.error('Timeout waiting for allocation');
    process.exit(1);
  }, 10000);
});
