import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private tokenSubject: BehaviorSubject<string|null>
  private token: string|null = null
  public token$: Observable<string|null>
  public isLoggedIn$ 
  
  constructor() {
    this.token = localStorage.getItem("token")
    this.tokenSubject = new BehaviorSubject<string|null>(this.token);
    this.token$ = this.tokenSubject.asObservable()
    this.isLoggedIn$ = this.token$.pipe(map(t => !!t))
    
    window.addEventListener("storage",event=>{
      if(event.key==="token") this.setToken(event.newValue)
    })
  }

  getToken(): string|null {
    return this.token ? this.token?.trim() : null
  }

  setToken(token: string|null){
    if(this.token===token) return

    token = token ? token.trim() : null    
    if(token?.length==0) token = null

    this.token = token
    this.tokenSubject.next(token)

    if(token === null)
      localStorage.removeItem("token")
    else
      localStorage.setItem("token", token)
  }

}
