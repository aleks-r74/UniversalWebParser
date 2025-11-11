import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { httpStatusToString, RestService } from '../rest.service';
import { LoginRequest } from '../scripts/models/loginRequest';
import { AuthService } from '../auth.service';
import { catchError, EMPTY, finalize } from 'rxjs';
import { Router } from '@angular/router';
import { StatestoreService } from '../statestore.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  host: { class: 'flex-1 flex items-center justify-center'}
})
export class LoginComponent {

  constructor(private fb: FormBuilder, 
              private restService: RestService,
              private authService: AuthService,
              private router: Router,
              private store: StatestoreService
            ) {}
  loading = false;

  loginForm = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  get username() { return this.loginForm.get('username'); }
  get password() { return this.loginForm.get('password'); }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.restService.login(this.loginForm.value as LoginRequest)
          .pipe(
            finalize(()=>this.loading=false),
            catchError((err: HttpErrorResponse)=>{
              let msgErr
              if(err.status==403)
                this.store.publish({type: "alert", alerttype: "error", message: "Invalid username or password"})
              else 
                this.store.publish({type: "alert", alerttype: "error", message: httpStatusToString(err.status)})
              return EMPTY
            })
          )
          .subscribe({
            next: dto=>{
              this.authService.setToken(dto.token)
              this.router.navigate(["scripts"])
            }
          })

  }
}
