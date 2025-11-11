import { Injectable, signal } from '@angular/core';
import { catchError, concat, concatMap, EMPTY, filter, finalize, map, of, ReplaySubject, Subject, switchMap, tap, timer } from 'rxjs';
import { httpStatusToString, RestService } from './rest.service';
import { ScriptRow } from './scripts/models/scriptRow';
import { ResultRow } from './scripts/models/resultRow';
import { Dialog } from './scripts/models/dialog';
import { Alert } from './scripts/models/alert';
import { Log } from './scripts/models/networkMessage';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})

export class StatestoreService {
  private _events = new Subject<StoreEvent>();
  events$ = this._events.asObservable()

  public logs$ = new ReplaySubject<string|Log>(50)

  constructor(private rest: RestService) {
      // load scripts
      this.events$
      .pipe(
        filter((e): e is { type: 'loadScripts' } => e.type === 'loadScripts'),
        switchMap(() => 
          this.rest.getScripts()
            .pipe(
              catchError((err: HttpErrorResponse )=> {
              this.publish({type: "alert", alerttype: "error", message: httpStatusToString(err.status)})
              return of([]);
              }),
              tap(scripts=> this._scriptRows.set(scripts))
            )
        )
      )
      .subscribe()

      // on script selected (load results)
      this.events$
      .pipe(
        filter((e): e is { type: 'selectScript', payload: number } => e.type === 'selectScript'),
        map(e=>e.payload),
        tap(()=>this.resultRowsLoading=true),
        switchMap(id =>
          this.rest.getResults(id)
            .pipe(
              catchError(err => {
              this.publish({type: "alert", alerttype: "error", message: httpStatusToString(err.status)})
              return of([]);
              }),
              tap(res=> this._resultRows.set(res)),
              finalize(()=>this.resultRowsLoading=false)
            )
        ),
      )
      .subscribe()
    
      // edit script
      this.events$
      .pipe(
        filter((e): e is { type: 'editScript', payload: number } => e.type === 'editScript'),
        map(e => {this.dialog.set({
           visible: true,
           type: "SCRIPT", 
           title: `Edit Script #${e.payload}`, 
           content: {observable: this.rest.getScript(e.payload), showLoading: true}
          })
        })
      )
      .subscribe()

      // add script
      this.events$
      .pipe(
        filter((e): e is { type: 'addScript' } => e.type === 'addScript'),
        map(e => {this.dialog.set({
           visible: true,
           type: "SCRIPT", 
           title: `Add New Script`, 
          })
        })
      )
      .subscribe()

      // show compilation errors
      this.events$
      .pipe(
        filter((e): e is { type: 'showCompilationError', payload: number } => e.type === 'showCompilationError'),
        map(e => {this.dialog.set({
           visible: true,
           type: "INFO", 
           title: `Compilation Errors for Script ${e.payload}`, 
           content: {observable: this.rest.getCompilationLogs(e.payload), showLoading: true}
          })
        })
      )
      .subscribe()

      // show result
      this.events$
      .pipe(
        filter((e): e is { type: 'showResult', payload: number } => e.type === 'showResult'),
        map(e => {this.dialog.set({
           visible: true,
           type: "INFO", 
           title: `Result ID ${e.payload}`, 
           content: {observable: this.rest.getResult(e.payload), showLoading: true}
          })
        })
      )
      .subscribe()

      // show logs
      this.events$
      .pipe(
        filter((e): e is { type: 'showLogs' } => e.type === 'showLogs'),
        map(e => {this.dialog.set({
           visible: true,
           type: "INFO", 
           title: `Live Logs`, 
           content: {observable: this.logs$, showLoading: false}
          })
        })
      )
      .subscribe()

      // delete script
      this.events$
      .pipe(
        filter((e): e is { type: 'deleteScript',   payload: number , callback: ()=>void} => e.type === 'deleteScript'),
        map(e => {this.dialog.set({
           visible: true,
           type: "CONFIRM", 
           title: `Are you sure?`, 
           content: {
            observable: of(`This will delte Script ${e.payload} and all its results!`), 
            confirmCallback: e.callback,
            showLoading: false
           }
          })
        })
      )
      .subscribe()

      // alert
      this.events$
      .pipe(
          filter((e): e is { type: 'alert'; alerttype: 'success' | 'error'; message?: string } => e.type === 'alert'),
          concatMap(e =>
            concat(
              of(e).pipe(tap(ev => this._alert.set({ visible: true, type: ev.alerttype , message: e.message}))),
              timer(3000).pipe(tap(() => this._alert.set({ visible: false, type: e.alerttype })))
            )
          )
        )
      .subscribe()
    }

    // Script-table Component
    private _scriptRows = signal<ScriptRow[]>([])
    public scriptRows = this._scriptRows.asReadonly()

    // Result-table Component
    private _resultRows = signal<ResultRow[]>([])
    public resultRows = this._resultRows.asReadonly()
    public resultRowsLoading = false

    // dialog
    public dialog = signal<Dialog>({visible: false, type: "INFO", title: ""})

    // alert
    private _alert = signal<Alert>({visible: false, type: "success"})
    public alert = this._alert.asReadonly()

  publish(event: StoreEvent) {
    this._events.next(event);
  }

  updateScriptRow(scriptRow: ScriptRow){
    this._scriptRows.update((rows: ScriptRow[]) => {
      let index = rows.findIndex((value)=>value.id==scriptRow.id)
      if(index>=0) rows[index] = scriptRow
      return [...rows]
    })
  }

  updateResultRow(resultRow: ResultRow){
    this._resultRows.update((rows: ResultRow[])=>{
      let index = rows.findIndex(value=>value.resultId==resultRow.resultId)
      if(index>=0) rows[index] = resultRow
      else if(rows.some((element)=>element.scriptId == resultRow.scriptId)){
        rows.unshift(resultRow)
      }
      return [...rows]
    })
  }

  deleteScript(scriptId: number){
    this._scriptRows.update(old=>old.filter(r=>r.id!=scriptId))
  }

  clearResults(){
    this._resultRows.set([])
  }

}

export type StoreEvent =
    { type: 'selectScript',   payload: number }
  | { type: 'editScript',     payload: number }
  | { type: 'deleteScript',   payload: number , callback: ()=>void}
  | { type: 'addScript' }
  | { type: 'loadScripts' }
  | { type: 'showLogs' }
  | { type: "alert", alerttype: "success"|"error", message?: string}
  | { type: 'showCompilationError', payload: number }
  | { type: 'showResult', payload: number }







