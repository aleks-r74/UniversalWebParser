import { Component, computed, effect, Input, output, signal } from '@angular/core';
import { SmartDatePipe } from "../smart-date.pipe";
import { ResultRow } from '../models/resultRow';
import { StatestoreService } from '../../statestore.service';
import { DatePipe } from '@angular/common';
import { RestService } from '../../rest.service';

@Component({
  selector: 'app-result-table',
  standalone: true,
  imports: [SmartDatePipe, DatePipe],
  templateUrl: './result-table.component.html',
})
export class ResultTableComponent {

  constructor(public store: StatestoreService,
              private restService: RestService
  ) {
    effect(() => {
      this.filteredResults = this.store.resultRows()
      if (this.filteredBy) this.filterBy(this.filteredBy)
      if (this.sortedBy) this.sortBy(this.sortedBy)
    })
  }

  filteredResults: ResultRow[] = []
  filteredFlag = false
  sortedBy: keyof ResultRow | null = null
  filteredBy: string | null = null

  filterBy(status: string) {
    this.filteredBy = status
    this.filteredFlag = true
    this.filteredResults = this.store.resultRows().filter(r => r.status == status)
  }
  resetFilters() {
    this.filteredFlag = false
    this.filteredBy = null
    this.sortedBy = null
    this.filteredResults = this.store.resultRows()
  }

  sortBy(field: keyof ResultRow) {
    this.sortedBy = field
    this.filteredResults = [...this.filteredResults]
      .sort((a, b) => {
        const valueA = a[field];
        const valueB = b[field];
        if (typeof valueA === 'string' && typeof valueB === 'string') {
          return -1 * valueA.localeCompare(valueB);
        }
        if (typeof valueA === 'boolean' && typeof valueB === 'boolean') {
          return valueA ? 1 : -1;
        }
        return 0;
      });

  }

  onShowResultClick(row: ResultRow) {
    //this.store.publish({ type: 'showResult', payload: row.resultId })
    this.restService.getResult(row.resultId).subscribe(
      {
        next: data=>{
          const blob = new Blob([data], { type: "application/json" });
          const url = URL.createObjectURL(blob);
          window.open(url); 
        }
      }
    )
  }

}
