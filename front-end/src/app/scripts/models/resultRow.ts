export interface ResultRow {
    scriptId: number,
    resultId: number,
    status: string,
    timestamp: string,
    delivered: boolean
}

export function isResultRow(data: any){
    return data && typeof data.resultId === "number" && typeof data.delivered === "boolean"
}