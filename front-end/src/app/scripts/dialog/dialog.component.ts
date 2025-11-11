import { AfterViewInit, Component, computed, effect, ElementRef, Signal, signal, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ScriptContent } from '../models/scriptContent';
import { BehaviorSubject, catchError, defer, distinctUntilChanged, EMPTY, filter, finalize, iif, map, Observable, of, shareReplay, startWith, Subject, Subscription, switchMap, takeUntil, tap, timeout, timeoutWith } from 'rxjs';
import { StatestoreService } from '../../statestore.service';
import { httpStatusToString, RestService } from '../../rest.service';
import { toObservable } from '@angular/core/rxjs-interop';
import { DialogContent } from '../models/dialog';
import { isLog, Log } from '../models/networkMessage';
import { DatePipe } from '@angular/common';

const INFO_LIMIT = 16

@Component({
  selector: 'app-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './dialog.component.html'
})
export class DialogComponent implements AfterViewInit {

  @ViewChild('modal', { static: true }) modal?: ElementRef

  btnLoading = false
  contentLoading = false
  backendErrors?: string
  textContent: string[] = []

  @ViewChild('logContainer') logContainer!: ElementRef<HTMLDivElement>;
  logContent: Log[] = []
  autoScroll = true;

  content = new BehaviorSubject<DialogContent|undefined>(undefined)
  confirmCallback?: ()=>void

  constructor(private fb: FormBuilder,
    public store: StatestoreService,
    private rest: RestService) {
    effect(() => {
      let d = this.store.dialog()
      d.visible ? this.modal?.nativeElement.showModal() : this.modal?.nativeElement.close()
      if (d.content){
        this.content.next(d.content)
        this.confirmCallback = d.content?.confirmCallback
      }
              
    })   
      
  }

  ngAfterViewInit() { // DaisyUI uses ESC to close modal and we need to handle this case
    this.modal?.nativeElement.addEventListener('close', (event: any) => this.closeModal());
    this.content
    .pipe(
      filter(Boolean),
      switchMap((wrapper: DialogContent) => {
        this.contentLoading = wrapper.showLoading;
        this.logContent = []
        const obs$ = wrapper.showLoading
          ? wrapper.observable.pipe(timeout({ first: 5000 }))
          : wrapper.observable;

        return obs$.pipe(
          catchError(err => {
            this.store.dialog.set({ visible: false });
            this.store.publish({ type: 'alert', alerttype: 'error', message: httpStatusToString(err.status) });
            return EMPTY;
          }),
          finalize(() => {
            if (this.store.dialog().content === wrapper)
              this.contentLoading = false;
          })
        );
      })
    )
    .subscribe(c => this.updateContent(c));
    
  }

  form: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(20)]],
    group: ['', [Validators.required, Validators.maxLength(20)]],
    source: ['', [Validators.required]],
    id: ['']
  });

  closeModal() {
    this.store.dialog.update(d => ({ visible: false}))
    this.textContent = []
    this.logContent = []
    this.confirmCallback = undefined
    this.form.reset()
  }

  onSave() {
    let scriptContent: ScriptContent = {
      name: this.form.get('name')?.value,
      group: this.form.get('group')?.value,
      source: this.form.get('source')?.value,
      scriptId: this.form.get('id')?.value,
    }

    this.btnLoading = true
    let observable: Observable<ScriptContent>
    if (scriptContent.scriptId) {
      observable = this.rest.updateScript(scriptContent)
    } else {
      observable = this.rest.createScript(scriptContent)
    }
    observable.subscribe({
      next: () => { this.store.dialog.set({ visible: false }); this.store.publish({ type: "loadScripts" }) },
      error: err => { 
        this.btnLoading = false
        this.store.publish({type: "alert", alerttype: "error", message: httpStatusToString(err.status)})
       },
      complete: () => {
        this.store.publish({ type: 'alert', alerttype: 'success' });
        this.btnLoading = false}
    })

  }

  updateContent(c: any): void {
    if (typeof (c) == 'string') {
      this.textContent.push(...c.split('\n'))
    } else if(isLog(c)){
      this.logContent.push(c)
      if(this.autoScroll) this.scrollToBottom();
    } else {
      this.form.get('name')?.setValue(c.name)
      this.form.get('group')?.setValue(c.group)
      this.form.get('source')?.setValue(c.source)
      this.form.get('id')?.setValue(c.scriptId)
    }
  }

   onScroll() {
    let el = this.logContainer?.nativeElement;
    if(!el) return
    this.autoScroll = el.scrollHeight - el.scrollTop - el.clientHeight <= 50;
  }

  private scrollToBottom() {
    const el = this.logContainer?.nativeElement;
    if(!el) return
    el.scrollTop = el.scrollHeight;
  }

  onConfirm() {
    this.confirmCallback && this.confirmCallback()
    this.closeModal()
  }

}

