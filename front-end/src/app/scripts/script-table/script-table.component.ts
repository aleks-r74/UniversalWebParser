import { Component, effect} from '@angular/core';
import { SmartDatePipe } from "../smart-date.pipe";
import { DatePipe, NgClass } from '@angular/common';
import { ScriptRow } from '../models/scriptRow';
import { httpStatusToString, RestService } from '../../rest.service';
import { StatestoreService } from '../../statestore.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-script-table',
  standalone: true,
  imports: [SmartDatePipe, NgClass, DatePipe],
  templateUrl: './script-table.component.html',
})
export class ScriptTableComponent {

  scriptRows : ScriptRow[] = []
  selectedScript : number | null = null
  constructor(private store: StatestoreService, private rest: RestService){
    effect(()=>{
      this.scriptRows = this.store.scriptRows()
    })
    store.publish({type: 'loadScripts'})
  }
  
  filtering = false

  showOnlyGroup(group: string) {
    this.filtering = true
    this.scriptRows=this.store.scriptRows()?.filter((row)=>row.group==group)
  }

  resetFiltering() {
    this.filtering = false
    this.scriptRows = this.store.scriptRows()
  }

  onScriptClick(scriptId: number, event: Event) {
    event.stopPropagation()
    if(this.selectedScript === scriptId) return
    this.selectedScript = scriptId
    this.store.clearResults()
    this.store.publish({type: 'selectScript', payload: scriptId})
  }
 
  onScriptUnselected() {
    this.selectedScript=null
    this.store.clearResults()
  }

  onEditClick(scriptId: number) {
    this.store.publish({type: 'editScript', payload: scriptId})
  }

  onForceStop(scriptId: number, event: Event) { 
    event.stopPropagation()
    const btn = event.currentTarget as HTMLButtonElement;
    const originalHTML = btn.innerHTML;
    btn.innerHTML = '<span class="loading loading-ring loading-xs"></span>';

    this.rest.killScript(scriptId).subscribe({
      complete: ()=> btn.innerHTML = originalHTML,
      error: (e: HttpErrorResponse)=> {
        btn.innerHTML = originalHTML
        this.store.publish({type: "alert", alerttype: "error", message: httpStatusToString(e.status)})
      }
    })
  }

  toggleOnOff(scriptId: number, event: Event) {
    event.stopPropagation()
    const row = this.scriptRows?.find(r => r.id === scriptId);
    if (!row) return;
    let oldState = row.isEnabled
    this.rest.setScriptState(scriptId,!row.isEnabled)
    .subscribe({
      error: e => {
        this.store.publish({type: "alert", alerttype: "error", message: httpStatusToString(e.status)})
        row.isEnabled = oldState
      }
    })
    row.isEnabled = null; // show spinner
  }

  onAddNew() {
    this.store.publish({type: 'addScript'})
  }

  onCompErrClick(scriptId: number) {
    this.store.publish({ type: 'showCompilationError', payload: scriptId })
  }

  onDelete(scriptId: number, event: Event) {
    const btn = event.currentTarget as HTMLButtonElement;
    const originalHTML = btn.innerHTML;

    this.store.publish({ 
      type: 'deleteScript',   
      payload: scriptId, 
      callback: ()=>{
        btn.innerHTML = '<span class="loading loading-ring loading-xs"></span>';
        // call delete
        this.rest.deleteScript(scriptId).subscribe({
            error: (e: HttpErrorResponse)=> {
              btn.innerHTML = originalHTML
              this.store.publish({type: "alert", alerttype: "error", message: httpStatusToString(e.status)})
            },
            complete: ()=> {
              this.store.deleteScript(scriptId)
              if(this.selectedScript == scriptId){
                this.store.clearResults()
                this.selectedScript = null
              }
              this.store.publish({type: "alert", alerttype: "success"})
            }
        })
      } 
    })
  }
}
