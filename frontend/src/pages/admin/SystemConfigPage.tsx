import { useState, useEffect, useCallback } from "react";
import {
    Settings, Save, RefreshCw, AlertTriangle, Key
} from "lucide-react";
import { Pagination } from "@/components/ui/pagination";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { adminService } from "@/services/adminService";
import type { SystemConfig } from "@/types/admin";

export default function SystemConfigPage() {
    const [configs, setConfigs] = useState<SystemConfig[]>([]);
    const [page, setPage] = useState(0);
    const [pageSize] = useState(50);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState<string | null>(null);

    const loadConfigs = useCallback(async (p: number) => {
        setIsLoading(true);
        try {
            const data = await adminService.getConfigs(p, pageSize);
            setConfigs(data.content);
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
        loadConfigs(page);
    }, [loadConfigs, page]);

    const handleSave = async (key: string, value: string) => {
        setIsSaving(key);
        try {
            await adminService.updateConfig({ configKey: key, value });
            await loadConfigs(page);
        } catch {
            // Silently handle error
        } finally {
            setIsSaving(null);
        }
    };

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary shadow-sm border border-primary/20">
                        <Settings className="h-6 w-6" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold tracking-tight text-foreground">System Configuration</h1>
                        <p className="text-sm text-muted-foreground">Adjust system-wide settings and parameters</p>
                    </div>
                </div>
                <Button variant="outline" size="sm" onClick={() => loadConfigs(page)} disabled={isLoading} className="rounded-full bg-background/50">
                    <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />
                    Refresh
                </Button>
            </div>

            {/* Config List */}
            <div className="glass-morphism rounded-2xl border border-border/50 bg-card/60 backdrop-blur-md overflow-hidden">
                <div className="divide-y divide-border/50">
                    {isLoading ? (
                        Array(3).fill(0).map((_, i) => (
                            <div key={i} className="h-24 p-6 animate-pulse bg-muted/20" />
                        ))
                    ) : configs.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-20 text-muted-foreground">
                            <Settings className="h-16 w-16 opacity-10 mb-4" />
                            <p className="text-lg font-medium">No configuration keys found</p>
                            <p className="text-sm opacity-60">System default values will be used</p>
                        </div>
                    ) : (
                        configs.map((config) => (
                            <div key={config.configKey} className="p-6 transition-all hover:bg-muted/30">
                                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                                    <div className="flex items-start gap-4 flex-1">
                                        <div className="mt-1 h-8 w-8 rounded-lg bg-muted flex items-center justify-center text-muted-foreground border border-border">
                                            <Key className="h-4 w-4" />
                                        </div>
                                        <div>
                                            <h3 className="font-semibold text-foreground tracking-tight">{config.configKey}</h3>
                                            <p className="text-xs text-muted-foreground mt-1">
                                                Last updated: {config.updatedAt ? new Date(config.updatedAt).toLocaleDateString() : "Never"}
                                            </p>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3 w-full md:w-auto">
                                        <Input
                                            className="h-10 rounded-xl bg-background/50 border-border/50 focus:ring-2 focus:ring-primary/20 transition-all font-mono text-sm max-w-none md:max-w-xs"
                                            value={config.value}
                                            onChange={(e) => {
                                                const val = e.target.value;
                                                setConfigs(prev => prev.map(c => c.configKey === config.configKey ? { ...c, value: val } : c));
                                            }}
                                        />
                                        <Button
                                            size="sm"
                                            className="rounded-xl px-4 shadow-sm"
                                            disabled={isSaving === config.configKey}
                                            onClick={() => handleSave(config.configKey, config.value)}
                                        >
                                            {isSaving === config.configKey ? (
                                                <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                                            ) : (
                                                <Save className="mr-2 h-4 w-4" />
                                            )}
                                            Save
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>
                <div className="mt-8 flex justify-center pb-6">
                    <Pagination 
                        currentPage={page} 
                        totalPages={totalPages} 
                        totalElements={totalElements} 
                        pageSize={pageSize} 
                        onPageChange={setPage}
                    />
                </div>
            </div>

            {/* Warning Area */}
            <div className="rounded-2xl border border-amber-500/20 bg-amber-500/5 p-4 flex gap-4 items-start">
                <AlertTriangle className="h-5 w-5 text-amber-500 mt-0.5" />
                <div>
                    <h4 className="text-sm font-semibold text-amber-700">Caution: Admin Access Only</h4>
                    <p className="text-xs text-amber-600/80 mt-1">Changes made to system configuration may affect application performance or security. Consult documentation before modifying core parameters.</p>
                </div>
            </div>
        </div>
    );
}
