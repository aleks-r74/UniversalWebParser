import { Component } from '@angular/core';
import { StatestoreService } from '../statestore.service';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [],
  templateUrl: './alert.component.html'
})
export class AlertComponent {
  constructor(public state: StatestoreService){}
}
