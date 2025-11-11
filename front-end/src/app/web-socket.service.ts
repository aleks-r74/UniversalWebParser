import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { StatestoreService } from './statestore.service';
import { distinctUntilChanged } from 'rxjs';
import { Log, NetworkMessage } from './scripts/models/networkMessage';
import { isScriptRow } from './scripts/models/scriptRow';
import { isResultRow } from './scripts/models/resultRow';
import { host } from '../../host';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private isLoggedIn = false;
  ws: WebSocket | null = null;
  private reconnectTimer: any = null;

  wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  wsHost = host.length > 0? host.substring(window.location.protocol.length+2) : window.location.host;
  wsUrl = `${this.wsProtocol}://${this.wsHost}/queue/private`;
  
  constructor(private authService: AuthService, private store: StatestoreService) {
    this.authService.isLoggedIn$.subscribe(val => {
      this.isLoggedIn = val;
      if (!val) this.closeSocket();
    });

    this.authService.token$
      .pipe(distinctUntilChanged())
      .subscribe(token => {
        if (!token) {
          this.closeSocket();
          return;
        }
        if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
          console.log('Already connected');
          return;
        }
        this.createSocket(token);
      });
  }

  private createSocket(token: string) {
    this.clearReconnectTimer();
    this.closeSocket();
    this.ws = new WebSocket(this.wsUrl, token);
    this.attachHandlers(this.ws);
  }

  private attachHandlers(ws: WebSocket) {
    ws.onopen = () => {
      console.log('Connected');
    };

    ws.onmessage = (event) => {
      const message = JSON.parse(event.data) as NetworkMessage;
      this.onNetworkMessage(message);
    };

    ws.onerror = () => { console.log("Error"); this.scheduleReconnect() }
    ws.onclose = () => { console.log("Disconnected"); this.scheduleReconnect()}
  }

  private scheduleReconnect() {
    if (!this.isLoggedIn || this.reconnectTimer) return;
    console.log("Reconnecting")
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      const token = this.authService.getToken();
      if (this.isLoggedIn && token) this.createSocket(token);
    }, 1000);
  }

  private clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  private closeSocket() {
    this.clearReconnectTimer();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  private onNetworkMessage(message: NetworkMessage) { 
    switch (message.type) { 
      case "LOG": { 
        let log: Log = message as Log 
        this.store.logs$.next(log) 
        break; 
      } 
      case "UPDATE": { 
        if (isScriptRow(message.payload)) 
          this.store.updateScriptRow(message.payload) 
        else if (isResultRow(message.payload)) 
          this.store.updateResultRow(message.payload) 
        break; 
      } 
    } 
  }
}
