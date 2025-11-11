import { ResultRow } from "./resultRow";
import { ScriptRow } from "./scriptRow";

export interface NetworkMessage{
    type: "LOG" | "UPDATE",
    payload: any
};
export interface Log extends NetworkMessage {
    type: "LOG", 
    payload: string,
    time: string
}
export interface Update extends NetworkMessage {
    type: "UPDATE", 
    id: number, 
    payload: ScriptRow|ResultRow
}

export function isLog(data: any): data is Log {
  return (data && typeof data.type === "string" && data.type === "LOG");
}