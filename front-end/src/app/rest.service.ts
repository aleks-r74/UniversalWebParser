import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { first, map, Observable, of } from 'rxjs';
import { ScriptRow } from './scripts/models/scriptRow';
import { ScriptContent } from './scripts/models/scriptContent';
import { LoginRequest } from './scripts/models/loginRequest';
import { TokenDto } from './scripts/models/tokenDto';
import { host } from '../../host';

@Injectable({
  providedIn: 'root'
})
export class RestService {
  basePath = `${host}/`
  constructor(private http: HttpClient) { }

  login(loginRequest: LoginRequest){
    return this.http.post<TokenDto>(`${this.basePath}auth/login`, loginRequest)
  }
  
  getScript(id: number): Observable<ScriptContent>{
    return this.http.get<ScriptContent>(`${this.basePath}api/scripts/${id}`)
      .pipe(
        first()
      )
  }

  getScripts(): Observable<ScriptRow[]>{
    return this.http.get<ScriptRow[]>(`${this.basePath}api/scripts`)
      .pipe(first())
  }


  getResult(resultId: number): Observable<string>{
    return this.http.get<any>(`${this.basePath}api/results/${resultId}`)
      .pipe(map(r=>r.result))
  }

  getResults(scriptId: number): Observable<any>{
    return this.http.get<any>(`${this.basePath}api/scripts/${scriptId}/results`)
      
  }

  getCompilationLogs(scriptId: number): Observable<string>{
    return this.http.get(`${this.basePath}api/scripts/${scriptId}/clogs`, {responseType: 'text' })
  }

  createScript(script: ScriptContent){
    return this.http.post<ScriptContent>(`${this.basePath}api/scripts`, script)
  }

  updateScript(script: ScriptContent){
    return this.http.patch<ScriptContent>(`${this.basePath}api/scripts/${script?.scriptId}`, script)
  }

  setScriptState(scriptId: number, isActive: boolean){
    return this.http.post(`${this.basePath}api/scripts/${scriptId}/actions`, {action: isActive ? "ENABLE" : "DISABLE"})
  }

  killScript(scriptId: number){
    return this.http.post(`${this.basePath}api/scripts/${scriptId}/actions`, {action: "FORCE_STOP"})
  }

  deleteScript(scriptId: number){
    return this.http.delete(`${this.basePath}api/scripts/${scriptId}`)
  }
}

export function httpStatusToString(status: number): string | undefined {
  switch(status){
    case 403 : return "Forbidden"
    case 401 : return "Unauthorized"
    default: return "Unexpected Network Error"
  }
}