import { Component, output, signal } from '@angular/core';
import { StatestoreService } from '../../statestore.service';

@Component({
  selector: 'app-floating-button',
  standalone: true,
  imports: [],
  templateUrl: './floating-button.component.html'
})
export class FloatingButtonComponent {
  constructor(private store: StatestoreService){}
  onClick(){
    this.store.publish({ type: 'showLogs' })
  } 
}
