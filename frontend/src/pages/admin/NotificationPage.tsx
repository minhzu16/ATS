import { useState, useEffect } from "react";
import { Bell, CheckCheck, RefreshCw, Inbox, Database } from "lucide-react";
import { Pagination } from "@/components/ui/pagination";
import { Button } from "@/components/ui/button";
import { adminService } from "@/services/adminService";
import type { Notification } from "@/types/admin";
import { toast } from "sonner";
import { SendNotificationDialog } from "./components/SendNotificationDialog";
import { NotificationList } from "./components/NotificationList";

export default function NotificationPage() {
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [page, setPage] = useState(0);
    const [pageSize] = useState(10);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isSending, setIsSending] = useState(false);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [reloadKey, setReloadKey] = useState(0);

    const [title, setTitle] = useState("");
    const [message, setMessage] = useState("");
    const [targetType, setTargetType] = useState<"broadcast" | "role">("broadcast");
    const [selectedRole, setSelectedRole] = useState("RECRUITER");
    const [notifType, setNotifType] = useState("SYSTEM_ALERT");
    const [activeTab, setActiveTab] = useState<"personal" | "system">("personal");

    useEffect(() => {
        let ignore = false;
        const fetchPromise = activeTab === "personal"
            ? adminService.getNotifications(page, pageSize)
            : adminService.getAllNotificationsAdmin(page, pageSize);

        fetchPromise
            .then((data) => {
                if (!ignore) {
                    setNotifications(data.content);
                    setTotalElements(data.totalElements);
                    setTotalPages(data.totalPages);
                    setPage(data.number);
                    setIsLoading(false);
                }
            })
            .catch(() => {
                if (!ignore) {
                    setIsLoading(false);
                }
            });

        return () => {
            ignore = true;
        };
    }, [page, pageSize, activeTab, reloadKey]);

    const reload = () => {
        setIsLoading(true);
        setReloadKey((k) => k + 1);
    };

    const handleTabChange = (tab: "personal" | "system") => {
        setIsLoading(true);
        setActiveTab(tab);
        setPage(0);
    };

    const handleMarkAsRead = async (id: string) => {
        try {
            await adminService.markAsRead(id);
            setNotifications((prev) => prev.map((n) => n.id === id ? { ...n, read: true } : n));
        } catch (error) {
            console.error("Failed to mark as read", error);
        }
    };

    const handleMarkAllRead = async () => {
        try {
            await adminService.markAllAsRead();
            setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
            toast.success("All notifications marked as read");
        } catch (error) {
            console.error("Failed to mark all as read", error);
            toast.error("Failed to mark all as read");
        }
    };

    const handleDelete = async (id: string) => {
        try {
            await adminService.deleteNotification(id);
            setNotifications((prev) => prev.filter((n) => n.id !== id));
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
                await adminService.broadcastNotification({ title, message, type: notifType });
            } else {
                await adminService.sendToRole({ role: selectedRole, title, message, type: notifType });
            }
            toast.success("Notification sent successfully");
            setIsDialogOpen(false);
            setTitle("");
            setMessage("");
            reload();
        } catch (error) {
            console.error("Failed to send notification", error);
            toast.error("Failed to send notification");
        } finally {
            setIsSending(false);
        }
    };

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
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
                    <SendNotificationDialog
                        isOpen={isDialogOpen}
                        isSending={isSending}
                        targetType={targetType}
                        selectedRole={selectedRole}
                        notifType={notifType}
                        title={title}
                        message={message}
                        onOpenChange={setIsDialogOpen}
                        onTargetTypeChange={setTargetType}
                        onSelectedRoleChange={setSelectedRole}
                        onNotifTypeChange={setNotifType}
                        onTitleChange={setTitle}
                        onMessageChange={setMessage}
                        onSubmit={handleSendNotification}
                    />
                    <Button variant="outline" size="sm" onClick={reload} disabled={isLoading} className="rounded-full">
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

            <div className="glass-morphism rounded-2xl border border-border/50 bg-card/60 backdrop-blur-md overflow-hidden">
                <NotificationList
                    notifications={notifications}
                    isLoading={isLoading}
                    activeTab={activeTab}
                    onReload={reload}
                    onMarkAsRead={handleMarkAsRead}
                    onDelete={handleDelete}
                />
                <div className="px-4 pb-4">
                    <Pagination
                        currentPage={page}
                        totalPages={totalPages}
                        totalElements={totalElements}
                        pageSize={pageSize}
                        onPageChange={(p) => {
                            setIsLoading(true);
                            setPage(p);
                        }}
                    />
                </div>
            </div>

            {!isLoading && notifications.length > 0 && (
                <p className="text-center text-[10px] text-muted-foreground uppercase tracking-widest font-medium opacity-50">
                    Showing {activeTab === "personal" ? "latest system notifications for your account" : "all notifications dispatched by the system"}
                </p>
            )}
        </div>
    );
}
