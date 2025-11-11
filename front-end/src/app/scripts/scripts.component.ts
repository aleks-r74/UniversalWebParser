import { Component, effect, OnInit} from '@angular/core';
import { ScriptTableComponent } from './script-table/script-table.component';
import { ResultTableComponent } from './result-table/result-table.component';
import { AsyncPipe, DatePipe } from '@angular/common';
import { ResultRow } from './models/resultRow';
import { DialogComponent } from "./dialog/dialog.component";
import { FloatingButtonComponent } from "./floating-button/floating-button.component";
import { RestService } from '../rest.service';
import { ScriptRow } from './models/scriptRow';
import { StatestoreService } from '../statestore.service';

@Component({
  selector: 'app-scripts',
  standalone: true,
  imports: [ScriptTableComponent, ResultTableComponent, DialogComponent, FloatingButtonComponent, AsyncPipe],
  providers: [DatePipe],
  templateUrl: './scripts.component.html',
  host: { class: 'flex-1 flex flex-col min-h-0'}
})
export class ScriptsComponent {
  
}
