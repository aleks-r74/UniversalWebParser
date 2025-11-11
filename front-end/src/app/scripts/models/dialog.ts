import { Observable } from "rxjs";
import { ScriptContent } from "./scriptContent";
import { Signal } from "@angular/core";
import { Log } from "./networkMessage";

export interface Dialog{
    visible: boolean,
    type?: "INFO" | "SCRIPT" | "CONFIRM",
    title?: string,
    content?: DialogContent
}
export interface DialogContent{
    observable: Observable<string|ScriptContent|Log>,
    showLoading: boolean
    confirmCallback?: ()=>void
}