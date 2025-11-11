import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { WebSocketService } from './web-socket.service';
import { AlertComponent } from "./alert/alert.component";
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AlertComponent, RouterLink],
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit{
  loggedIn = false
  constructor(private wsService: WebSocketService,
              private authService: AuthService,
              private router: Router
  ){}
  ngOnInit(): void {
    this.authService.isLoggedIn$.subscribe(r=>this.loggedIn=r)
  }
  onLogout(){
    this.authService.setToken(null)
    this.router.navigate(["login"])
  }
}
