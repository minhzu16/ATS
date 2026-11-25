import {
    RefreshCw, Clock, Inbox, Info, AlertTriangle, CheckCircle2, MoreVertical, Trash2
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import type { Notification } from "@/types/admin";

function getTypeIcon(type: Notification["type"]) {
    switch (type) {
        case "INTERVIEW_PENDING": return <Clock className="h-4 w-4 text-blue-500" />;
        case "OFFER_APPROVED": return <CheckCircle2 className="h-4 w-4 text-emerald-500" />;
        case "OFFER_REJECTED": return <AlertTriangle className="h-4 w-4 text-destructive" />;
        default: return <Info className="h-4 w-4 text-primary" />;
    }
}

interface NotificationListProps {
    notifications: Notification[];
    isLoading: boolean;
    activeTab: "personal" | "system";
    onReload: () => void;
    onMarkAsRead: (id: string) => void;
    onDelete: (id: string) => void;
}

export function NotificationList({
    notifications,
    isLoading,
    activeTab,
    onReload,
    onMarkAsRead,
    onDelete,
}: NotificationListProps) {
    if (isLoading) {
        return (
            <div className="divide-y divide-border/50">
                {Array(5).fill(0).map((_, i) => (
                    <div key={i} className="p-6 animate-pulse flex gap-4">
                        <div className="h-10 w-10 rounded-full bg-muted" />
                        <div className="flex-1 space-y-2">
                            <div className="h-4 w-1/4 bg-muted rounded" />
                            <div className="h-3 w-3/4 bg-muted rounded" />
                        </div>
                    </div>
                ))}
            </div>
        );
    }

    if (notifications.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center py-24 text-muted-foreground">
                <div className="relative mb-4">
                    <Inbox className="h-20 w-20 opacity-5" />
                    <Badge className="absolute -top-1 -right-1 h-6 w-6 rounded-full flex items-center justify-center p-0">0</Badge>
                </div>
                <p className="text-xl font-semibold opacity-40 italic">
                    {activeTab === "personal" ? "Your inbox is empty" : "No system notifications found"}
                </p>
                <p className="text-sm opacity-30 mt-1 uppercase tracking-tighter font-mono">
                    No new alerts found
                </p>
                <Button variant="ghost" className="mt-6 rounded-full opacity-40 hover:opacity-100" onClick={onReload}>
                    <RefreshCw className="mr-2 h-4 w-4" /> Check again
                </Button>
            </div>
        );
    }

    return (
        <div className="divide-y divide-border/50">
            {notifications.map((n) => (
                <div
                    key={n.id}
                    className={`p-6 transition-all border-l-4 ${n.read ? "border-transparent bg-transparent" : "border-primary bg-primary/5"} hover:bg-muted/30 group`}
                >
                    <div className="flex gap-4">
                        <div className={`mt-1 flex h-10 w-10 items-center justify-center rounded-full border ${n.read ? "border-border bg-muted/50" : "border-primary/20 bg-primary/10 shadow-sm shadow-primary/20"}`}>
                            {getTypeIcon(n.type)}
                        </div>
                        <div className="flex-1 min-w-0">
                            <div className="flex items-center justify-between gap-2">
                                <h3 className={`font-semibold tracking-tight truncate ${n.read ? "text-foreground/70" : "text-foreground"}`}>
                                    {n.title}
                                </h3>
                                <span className="text-[10px] text-muted-foreground font-medium whitespace-nowrap bg-muted/50 px-2 py-0.5 rounded-full border border-border/50">
                                    {new Date(n.createdAt).toLocaleString()}
                                </span>
                            </div>
                            <p className={`mt-1 text-sm leading-relaxed ${n.read ? "text-muted-foreground" : "text-foreground/80"}`}>
                                {n.message}
                            </p>
                            {!n.read && activeTab === "personal" && (
                                <div className="mt-4 flex items-center gap-3">
                                    <Button
                                        size="sm"
                                        variant="ghost"
                                        className="h-8 rounded-lg text-xs font-semibold hover:bg-primary/10 hover:text-primary px-3"
                                        onClick={() => onMarkAsRead(n.id)}
                                    >
                                        Mark as read
                                    </Button>
                                    <span className="w-1.5 h-1.5 rounded-full bg-primary animate-pulse" />
                                </div>
                            )}
                        </div>
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full opacity-0 group-hover:opacity-100 transition-opacity">
                                    <MoreVertical className="h-4 w-4" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="glass-morphism border-primary/20">
                                <DropdownMenuItem
                                    className="text-destructive focus:text-destructive focus:bg-destructive/10"
                                    onClick={() => onDelete(n.id)}
                                >
                                    <Trash2 className="mr-2 h-4 w-4" />
                                    Delete
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>
            ))}
        </div>
    );
}
