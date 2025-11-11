import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { ScriptsComponent } from './scripts/scripts.component';
import { authGuard } from './auth-guard.guard';
import { MainComponent } from './main/main.component';

export const routes: Routes = [
    {path: "", component: MainComponent, title: "Welcome to universal parser"},
    {path: "login", component: LoginComponent, title: "Please log In"},
    {path: "scripts", canActivate: [authGuard], component: ScriptsComponent, title: "Manage Scripts"},
    {path: "**", component: MainComponent, title: "Welcome to universal parser"}
];
