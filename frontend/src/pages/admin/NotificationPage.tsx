import { useState, useEffect, useCallback } from "react";
import {
    Bell, CheckCheck, RefreshCw, Clock, Inbox, Info, AlertTriangle, CheckCircle2, MoreVertical, Database
} from "lucide-react";
import { Pagination } from "@/components/ui/pagination";
import { Button } from "@/components/ui/button";
import { adminService } from "@/services/adminService";
import type { Notification } from "@/types/admin";
import { Badge } from "@/components/ui/badge";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "sonner";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Trash2 } from "lucide-react";

export default function NotificationPage() {
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [page, setPage] = useState(0);
    const [pageSize] = useState(10);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isSending, setIsSending] = useState(false);
    const [isDialogOpen, setIsDialogOpen] = useState(false);

    // Form state
    const [title, setTitle] = useState("");
    const [message, setMessage] = useState("");
    const [targetType, setTargetType] = useState<"broadcast" | "role">("broadcast");
    const [selectedRole, setSelectedRole] = useState("RECRUITER");
    const [notifType, setNotifType] = useState("SYSTEM_ALERT");

    const [activeTab, setActiveTab] = useState<"personal" | "system">("personal");

    const loadNotifications = useCallback(async (p: number, tab?: "personal" | "system") => {
        const currentTab = tab || activeTab;
        setIsLoading(true);
        try {
            const data = currentTab === "personal" 
                ? await adminService.getNotifications(p, pageSize)
                : await adminService.getAllNotificationsAdmin(p, pageSize);
            setNotifications(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
            setPage(data.number);
        } catch {
            // Silently fail
        } finally {
            setIsLoading(false);
        }
    }, [pageSize, activeTab]);

    const handleTabChange = (tab: "personal" | "system") => {
        setActiveTab(tab);
        setPage(0);
        loadNotifications(0, tab);
    };

    useEffect(() => {
        loadNotifications(page);
    }, [loadNotifications, page]);

    const handleMarkAsRead = async (id: string) => {
        try {
            await adminService.markAsRead(id);
            setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
        } catch (error) {
            console.error("Failed to mark as read", error);
        }
    };

    const handleMarkAllRead = async () => {
        try {
            await adminService.markAllAsRead();
            setNotifications(prev => prev.map(n => ({ ...n, read: true })));
            toast.success("All notifications marked as read");
        } catch (error) {
            console.error("Failed to mark all as read", error);
            toast.error("Failed to mark all as read");
        }
    };

    const handleDelete = async (id: string) => {
        try {
            await adminService.deleteNotification(id);
            setNotifications(prev => prev.filter(n => n.id !== id));
            toast.success("Notification deleted");
        } catch (error) {
            console.error("Failed to delete notification", error);
            toast.error("Failed to delete notification");
        }
    };

    const handleSendNotification = async () => {
        if (!title || !message) {
            toast.error("Please fill in all fields");
            return;
        }

        setIsSending(true);
        try {
            if (targetType === "broadcast") {
                await adminService.broadcastNotification({
                    title,
                    message,
                    type: notifType
                });
            } else {
                await adminService.sendToRole({
                    role: selectedRole,
                    title,
                    message,
                    type: notifType
                });
            }
            toast.success("Notification sent successfully");
            setIsDialogOpen(false);
            setTitle("");
            setMessage("");
            loadNotifications(page);
        } catch (error) {
            console.error("Failed to send notification", error);
            toast.error("Failed to send notification");
        } finally {
            setIsSending(false);
        }
    };

    const getTypeIcon = (type: Notification["type"]) => {
        switch (type) {
            case "INTERVIEW_PENDING": return <Clock className="h-4 w-4 text-blue-500" />;
            case "OFFER_APPROVED": return <CheckCircle2 className="h-4 w-4 text-emerald-500" />;
            case "OFFER_REJECTED": return <AlertTriangle className="h-4 w-4 text-destructive" />;
            default: return <Info className="h-4 w-4 text-primary" />;
        }
    };

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary shadow-sm border border-primary/20">
                        <Bell className="h-6 w-6" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold tracking-tight text-foreground">Notification Center</h1>
                        <p className="text-sm text-muted-foreground">Stay updated with system events and tasks</p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                        <DialogTrigger asChild>
                            <Button size="sm" className="rounded-full shadow-lg shadow-primary/20">
                                <Bell className="mr-2 h-4 w-4" />
                                Send Notification
                            </Button>
                        </DialogTrigger>
                        <DialogContent className="sm:max-w-[425px] glass-morphism border-primary/20">
                            <DialogHeader>
                                <DialogTitle className="text-xl flex items-center gap-2">
                                    <Bell className="h-5 w-5 text-primary" />
                                    Send Notification
                                </DialogTitle>
                                <DialogDescription>
                                    Broadcast a message to all users or specific roles.
                                </DialogDescription>
                            </DialogHeader>
                            <div className="grid gap-6 py-4">
                                <div className="space-y-2">
                                    <Label htmlFor="target">Recipient Target</Label>
                                    <Select 
                                        value={targetType} 
                                        onValueChange={(v: any) => setTargetType(v)}
                                    >
                                        <SelectTrigger className="rounded-xl border-primary/20 bg-background/50">
                                            <SelectValue placeholder="Select target" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="broadcast">All Users (Broadcast)</SelectItem>
                                            <SelectItem value="role">Specific Role</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                {targetType === "role" && (
                                    <div className="space-y-2 animate-in slide-in-from-top-2 duration-300">
                                        <Label htmlFor="role">Target Role</Label>
                                        <Select value={selectedRole} onValueChange={setSelectedRole}>
                                            <SelectTrigger className="rounded-xl border-primary/20 bg-background/50">
                                                <SelectValue placeholder="Select role" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="SYSTEM_ADMIN">System Admin</SelectItem>
                                                <SelectItem value="HR_MANAGER">HR Manager</SelectItem>
                                                <SelectItem value="RECRUITER">Recruiter</SelectItem>
                                                <SelectItem value="INTERVIEWER">Interviewer</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                )}

                                <div className="space-y-2">
                                    <Label htmlFor="type">Notification Type</Label>
                                    <Select value={notifType} onValueChange={setNotifType}>
                                        <SelectTrigger className="rounded-xl border-primary/20 bg-background/50">
                                            <SelectValue placeholder="Select type" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="SYSTEM_ALERT">System Alert</SelectItem>
                                            <SelectItem value="ONBOARDING_ASSIGNED">Onboarding</SelectItem>
                                            <SelectItem value="INTERVIEW_PENDING">Interview</SelectItem>
                                            <SelectItem value="OFFER_APPROVED">Offer</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="title">Title</Label>
                                    <Input
                                        id="title"
                                        placeholder="Enter notification title"
                                        value={title}
                                        onChange={(e) => setTitle(e.target.value)}
                                        className="rounded-xl border-primary/20 bg-background/50"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="message">Message</Label>
                                    <Textarea
                                        id="message"
                                        placeholder="Enter notification message"
                                        value={message}
                                        onChange={(e) => setMessage(e.target.value)}
                                        className="min-h-[100px] rounded-xl border-primary/20 bg-background/50"
                                    />
                                </div>
                            </div>
                            <DialogFooter>
                                <Button 
                                    variant="outline" 
                                    onClick={() => setIsDialogOpen(false)}
                                    className="rounded-full"
                                >
                                    Cancel
                                </Button>
                                <Button 
                                    onClick={handleSendNotification} 
                                    disabled={isSending}
                                    className="rounded-full px-8 shadow-lg shadow-primary/20"
                                >
                                    {isSending && <RefreshCw className="mr-2 h-4 w-4 animate-spin" />}
                                    Send Now
                                </Button>
                            </DialogFooter>
                        </DialogContent>
                    </Dialog>
                    <Button variant="outline" size="sm" onClick={() => loadNotifications(page)} disabled={isLoading} className="rounded-full">
                        <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />
                        Refresh
                    </Button>
                    {activeTab === "personal" && (
                        <Button variant="outline" size="sm" onClick={handleMarkAllRead} className="rounded-full border-primary/20 text-primary hover:bg-primary/5">
                            <CheckCheck className="mr-2 h-4 w-4" />
                            Mark all as read
                        </Button>
                    )}
                </div>
            </div>

            {/* Tabs */}
            <div className="flex bg-muted/50 p-1.5 rounded-xl w-fit mb-6 border border-border/50">
                <Button 
                    variant={activeTab === "personal" ? "secondary" : "ghost"} 
                    size="sm" 
                    onClick={() => handleTabChange("personal")}
                    className={`rounded-lg transition-all ${activeTab === "personal" ? "shadow-sm bg-background" : ""}`}
                >
                    <Inbox className="mr-2 h-4 w-4" />
                    My Inbox
                </Button>
                <Button 
                    variant={activeTab === "system" ? "secondary" : "ghost"} 
                    size="sm" 
                    onClick={() => handleTabChange("system")}
                    className={`rounded-lg transition-all ${activeTab === "system" ? "shadow-sm bg-background" : ""}`}
                >
                    <Database className="mr-2 h-4 w-4" />
                    System Sent Log
                </Button>
            </div>

            {/* List */}
            <div className="glass-morphism rounded-2xl border border-border/50 bg-card/60 backdrop-blur-md overflow-hidden">
                <div className="divide-y divide-border/50">
                    {isLoading ? (
                        Array(5).fill(0).map((_, i) => (
                            <div key={i} className="p-6 animate-pulse flex gap-4">
                                <div className="h-10 w-10 rounded-full bg-muted" />
                                <div className="flex-1 space-y-2">
                                    <div className="h-4 w-1/4 bg-muted rounded" />
                                    <div className="h-3 w-3/4 bg-muted rounded" />
                                </div>
                            </div>
                        ))
                    ) : notifications.length === 0 ? (
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
                            <Button variant="ghost" className="mt-6 rounded-full opacity-40 hover:opacity-100" onClick={() => loadNotifications(page)}>
                                <RefreshCw className="mr-2 h-4 w-4" /> Check again
                            </Button>
                        </div>
                    ) : (
                        notifications.map((n) => (
                            <div 
                                key={n.id} 
                                className={`p-6 transition-all border-l-4 ${n.read ? 'border-transparent bg-transparent' : 'border-primary bg-primary/5'} hover:bg-muted/30 group`}
                            >
                                <div className="flex gap-4">
                                    <div className={`mt-1 flex h-10 w-10 items-center justify-center rounded-full border ${n.read ? 'border-border bg-muted/50' : 'border-primary/20 bg-primary/10 shadow-sm shadow-primary/20'}`}>
                                        {getTypeIcon(n.type)}
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center justify-between gap-2">
                                            <h3 className={`font-semibold tracking-tight truncate ${n.read ? 'text-foreground/70' : 'text-foreground'}`}>
                                                {n.title}
                                            </h3>
                                            <span className="text-[10px] text-muted-foreground font-medium whitespace-nowrap bg-muted/50 px-2 py-0.5 rounded-full border border-border/50">
                                                {new Date(n.createdAt).toLocaleString()}
                                            </span>
                                        </div>
                                        <p className={`mt-1 text-sm leading-relaxed ${n.read ? 'text-muted-foreground' : 'text-foreground/80'}`}>
                                            {n.message}
                                        </p>
                                        {!n.read && activeTab === "personal" && (
                                            <div className="mt-4 flex items-center gap-3">
                                                <Button 
                                                    size="sm" 
                                                    variant="ghost" 
                                                    className="h-8 rounded-lg text-xs font-semibold hover:bg-primary/10 hover:text-primary px-3"
                                                    onClick={() => handleMarkAsRead(n.id)}
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
                                                onClick={() => handleDelete(n.id)}
                                            >
                                                <Trash2 className="mr-2 h-4 w-4" />
                                                Delete
                                            </DropdownMenuItem>
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </div>
                            </div>
                        ))
                    )}
                </div>
                <div className="px-4 pb-4">
                    <Pagination 
                        currentPage={page} 
                        totalPages={totalPages} 
                        totalElements={totalElements} 
                        pageSize={pageSize} 
                        onPageChange={setPage}
                    />
                </div>
            </div>
            
            {/* Footer Tip */}
            {!isLoading && notifications.length > 0 && (
                <p className="text-center text-[10px] text-muted-foreground uppercase tracking-widest font-medium opacity-50">
                    Showing {activeTab === "personal" ? "latest system notifications for your account" : "all notifications dispatched by the system"}
                </p>
            )}
        </div>
    );
}
