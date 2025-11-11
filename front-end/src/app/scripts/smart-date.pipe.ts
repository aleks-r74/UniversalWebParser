import { DatePipe } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'smartDate',
  standalone: true
})
export class SmartDatePipe implements PipeTransform {
  constructor(private datePipe: DatePipe) {}

   transform(value: string | Date | number | null | undefined, timezone?: string): string {
     if (value === null || value === undefined || value === '') {
      return '';
    }

    const date = this.parseToDate(value);
    if (!date || isNaN(date.getTime())) {
      return String(value);
    }

    const now = new Date();
    if (this.isSameLocalDay(date, now)) {
      return this.datePipe.transform(date, 'shortTime', timezone) || '';
    } else {
      return this.datePipe.transform(date, 'MMM dd', timezone) || '';
    }
  }

  private parseToDate(value: string | Date | number): Date | null {
    if (value instanceof Date) {
      return value;
    }
    if (typeof value === 'number') {
      return new Date(value);
    }
    if (typeof value === 'string') {
      let d = new Date(value);
      if (!isNaN(d.getTime())) return d;
    }

    return null;
  }

  private isSameLocalDay(a: Date, b: Date): boolean {
    return (
      a.getFullYear() === b.getFullYear() &&
      a.getMonth() === b.getMonth() &&
      a.getDate() === b.getDate()
    );
  }

}
