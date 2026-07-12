export class SimpleStompClient {
  constructor(url, onConnect, onMessage) {
    this.url = url;
    this.onConnect = onConnect;
    this.onMessage = onMessage;
    this.ws = null;
    this.connected = false;
    this.subscriptions = [];
  }

  connect() {
    // Standardize URL to use ws:// or wss:// if http/https is used
    let wsUrl = this.url;
    if (wsUrl.startsWith('http://')) {
      wsUrl = wsUrl.replace('http://', 'ws://');
    } else if (wsUrl.startsWith('https://')) {
      wsUrl = wsUrl.replace('https://', 'wss://');
    }
    
    // Connect to endpoint
    this.ws = new WebSocket(wsUrl);
    
    this.ws.onopen = () => {
      const frame = "CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\u0000";
      this.ws.send(frame);
    };

    this.ws.onmessage = (event) => {
      const data = event.data;
      if (data.startsWith("CONNECTED")) {
        this.connected = true;
        if (this.onConnect) this.onConnect();
        // Re-subscribe to any queued subscriptions
        this.subscriptions.forEach(dest => this.sendSubscribeFrame(dest));
      } else if (data.startsWith("MESSAGE")) {
        // Parse headers & body
        const bodyStart = data.indexOf("\n\n");
        if (bodyStart !== -1) {
          const body = data.substring(bodyStart + 2).replace(/\u0000/g, "");
          try {
            const parsed = JSON.parse(body);
            if (this.onMessage) this.onMessage(parsed);
          } catch (e) {
            // raw string or invalid json
          }
        }
      }
    };

    this.ws.onerror = (err) => {
      console.warn("WebSocket error: ", err);
    };

    this.ws.onclose = () => {
      this.connected = false;
      console.log("WebSocket connection closed.");
    };
  }

  subscribe(destination) {
    if (!this.subscriptions.includes(destination)) {
      this.subscriptions.push(destination);
    }
    if (this.connected) {
      this.sendSubscribeFrame(destination);
    }
  }

  sendSubscribeFrame(destination) {
    const subId = "sub-" + Math.random().toString(36).substring(2, 11);
    const frame = `SUBSCRIBE\nid:${subId}\ndestination:${destination}\nack:auto\n\n\u0000`;
    this.ws.send(frame);
  }

  disconnect() {
    this.subscriptions = [];
    if (this.ws) {
      this.ws.close();
    }
  }
}
