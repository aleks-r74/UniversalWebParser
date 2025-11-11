import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "./auth.service";

export const httpInterceptor: HttpInterceptorFn = (req, next) => {
  let authService = inject(AuthService)
    let jwt = authService.getToken()
    if(jwt){
        let cloned = req.clone({ headers: req.headers.set("Authorization", `Bearer ${jwt}`)})
        return next(cloned);
    }
  return next(req);
};