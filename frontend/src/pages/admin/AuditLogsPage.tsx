import { useState, useEffect, useCallback } from "react";
import {
    Activity, Search, RefreshCw, Clock, ChevronRight, Download, FileSpreadsheet, FileText
} from "lucide-react";
import { Pagination } from "@/components/ui/pagination";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { adminService } from "@/services/adminService";
import type { AuditLog } from "@/types/admin";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export default function AuditLogsPage() {
    const [logs, setLogs] = useState<AuditLog[]>([]);
    const [page, setPage] = useState(0);
    const [pageSize] = useState(15);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [search, setSearch] = useState("");
    const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);
    const [isExporting, setIsExporting] = useState(false);

    const loadLogs = useCallback(async (p: number) => {
        setIsLoading(true);
        try {
            const data = await adminService.getAuditLogs(p, pageSize);
            setLogs(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
            setPage(data.number);
        } catch {
            // Silently handle error
        } finally {
            setIsLoading(false);
        }
    }, [pageSize]);

    useEffect(() => {
        loadLogs(page);
    }, [loadLogs, page]);

    const filtered = logs.filter((log) =>
        log.action.toLowerCase().includes(search.toLowerCase()) ||
        (log.userEmail?.toLowerCase() || "").includes(search.toLowerCase()) ||
        (log.userFullName?.toLowerCase() || "").includes(search.toLowerCase()) ||
        log.entityType.toLowerCase().includes(search.toLowerCase())
    );

    const handleExport = async (format: "csv" | "excel") => {
        setIsExporting(true);
        try {
            const blob = await adminService.exportAuditLogs(format);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `audit_logs.${format === "csv" ? "csv" : "xlsx"}`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch {
            // handle error if needed
        } finally {
            setIsExporting(false);
        }
    };

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary shadow-sm border border-primary/20">
                        <Activity className="h-6 w-6" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold tracking-tight text-foreground">Audit Logs</h1>
                        <p className="text-sm text-muted-foreground">Monitor system-wide activity and changes</p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="outline" size="sm" className="rounded-full" disabled={isExporting}>
                                {isExporting ? <RefreshCw className="mr-2 h-4 w-4 animate-spin" /> : <Download className="mr-2 h-4 w-4" />}
                                Export
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-40 border-border/50 bg-card/95 backdrop-blur-xl">
                            <DropdownMenuItem className="cursor-pointer" onClick={() => handleExport("csv")}>
                                <FileText className="mr-2 h-4 w-4" />
                                Export as CSV
                            </DropdownMenuItem>
                            <DropdownMenuItem className="cursor-pointer" onClick={() => handleExport("excel")}>
                                <FileSpreadsheet className="mr-2 h-4 w-4 text-green-600" />
                                Export as Excel
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>

                    <Button variant="outline" size="sm" onClick={() => loadLogs(page)} disabled={isLoading} className="rounded-full">
                        <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />
                        Refresh
                    </Button>
                </div>
            </div>

            {/* Content Table */}
            <div className="glass-morphism rounded-2xl border border-border/50 bg-card/60 backdrop-blur-md overflow-hidden">
                <div className="p-4 border-b border-border/50 bg-muted/20">
                    <div className="relative max-w-sm">
                        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                        <Input
                            placeholder="Search logs..."
                            className="pl-10 h-10 rounded-xl bg-background/50 border-border/50 focus:ring-2 focus:ring-primary/20 transition-all"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                    </div>
                </div>

                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-border/50">
                        <thead className="bg-muted/50">
                            <tr>
                                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">Timestamp</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">Action</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">Performed By</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">Entity</th>
                                <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-muted-foreground">Details</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-border/50 bg-transparent">
                            {isLoading ? (
                                Array(5).fill(0).map((_, i) => (
                                    <tr key={i} className="animate-pulse h-16">
                                        <td colSpan={5} className="px-6 py-4 bg-muted/5" />
                                    </tr>
                                ))
                            ) : filtered.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="px-6 py-20 text-center text-muted-foreground">No logs found.</td>
                                </tr>
                            ) : (
                                filtered.map((log) => (
                                    <tr key={log.id} className="transition-all hover:bg-primary/5 cursor-default group">
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-foreground flex items-center gap-2">
                                            <Clock className="h-4 w-4 text-muted-foreground/60" />
                                            {new Date(log.createdAt).toLocaleString()}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="inline-flex rounded-full bg-blue-500/10 text-blue-600 px-2.5 py-0.5 text-xs font-semibold border border-blue-500/20">
                                                {log.action}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-foreground">
                                            <div className="flex items-center gap-2">
                                                <div className="h-6 w-6 rounded-full bg-muted flex items-center justify-center text-[10px] font-bold">
                                                    {(log.userFullName || log.userEmail || "Sys").slice(0, 2).toUpperCase()}
                                                </div>
                                                {log.userFullName || log.userEmail || "System"}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-muted-foreground font-mono">
                                            {log.entityType} {log.entityId ? `(${log.entityId.slice(0, 8)})` : ""}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <Button 
                                                variant="ghost" 
                                                size="sm" 
                                                className="h-8 w-8 rounded-full group-hover:bg-primary/10 group-hover:text-primary transition-all"
                                                onClick={() => setSelectedLog(log)}
                                            >
                                                <ChevronRight className="h-4 w-4" />
                                            </Button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            <div className="mt-8 flex justify-center pb-8 border-t border-border/10 pt-4">
                <Pagination 
                    currentPage={page} 
                    totalPages={totalPages} 
                    totalElements={totalElements} 
                    pageSize={pageSize} 
                    onPageChange={setPage}
                />
            </div>
            
            <Dialog open={!!selectedLog} onOpenChange={() => setSelectedLog(null)}>
                <DialogContent className="sm:max-w-[600px] border-border/50 bg-card/95 backdrop-blur-xl">
                    <DialogHeader>
                        <DialogTitle className="text-xl">Audit Log Details</DialogTitle>
                    </DialogHeader>
                    {selectedLog && (
                        <div className="space-y-4 py-4">
                            <div className="grid grid-cols-2 gap-4 text-sm">
                                <div>
                                    <span className="text-muted-foreground block mb-1">Action</span>
                                    <span className="font-medium bg-primary/10 text-primary px-2 py-1 rounded-md">
                                        {selectedLog.action}
                                    </span>
                                </div>
                                <div>
                                    <span className="text-muted-foreground block mb-1">Entity</span>
                                    <span className="font-mono bg-muted px-2 py-1 rounded-md">
                                        {selectedLog.entityType}
                                    </span>
                                </div>
                                <div>
                                    <span className="text-muted-foreground block mb-1">User</span>
                                    <span className="font-medium">{selectedLog.userFullName || selectedLog.userEmail || "System"}</span>
                                </div>
                                <div>
                                    <span className="text-muted-foreground block mb-1">IP Address</span>
                                    <span className="font-mono text-muted-foreground">{selectedLog.ipAddress || "N/A"}</span>
                                </div>
                                <div className="col-span-2">
                                    <span className="text-muted-foreground block mb-1">Entity ID</span>
                                    <span className="font-mono text-muted-foreground text-xs break-all">{selectedLog.entityId || "N/A"}</span>
                                </div>
                            </div>
                            
                            {selectedLog.oldValue && selectedLog.oldValue !== "null" && (
                                <div className="space-y-1">
                                    <span className="text-sm font-medium text-destructive">Previous Value</span>
                                    <pre className="bg-muted border border-border/50 p-3 rounded-xl text-xs overflow-x-auto whitespace-pre-wrap font-mono text-muted-foreground">
                                        {formatJson(selectedLog.oldValue)}
                                    </pre>
                                </div>
                            )}
                            
                            {selectedLog.newValue && selectedLog.newValue !== "null" && (
                                <div className="space-y-1">
                                    <span className="text-sm font-medium text-green-500">New Value</span>
                                    <pre className="bg-muted border border-border/50 p-3 rounded-xl text-xs overflow-x-auto whitespace-pre-wrap font-mono text-muted-foreground">
                                        {formatJson(selectedLog.newValue)}
                                    </pre>
                                </div>
                            )}
                        </div>
                    )}
                </DialogContent>
            </Dialog>

            <p className="text-right text-[10px] text-muted-foreground opacity-60">System monitoring active and encrypted.</p>
        </div>
    );
}

function formatJson(str: string): string {
    try {
        const obj = JSON.parse(str);
        return JSON.stringify(obj, null, 2);
    } catch {
        return str;
    }
}
