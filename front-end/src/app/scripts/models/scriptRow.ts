export interface ScriptRow {
    id: number,
    name: String,
    group: string,
    compiles: boolean,
    runs: number,
    fails: number,
    nextRun: string,
    isEnabled: boolean | null,
    isEnqueued: boolean
}

export function isScriptRow(data: any): data is ScriptRow {
  return (
    data &&
    typeof data.id === "number" &&
    typeof data.isEnqueued === "boolean"
  );
}