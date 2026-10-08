import { useState, useEffect, useCallback } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import api from "@/lib/api";
import { LogOut, User, Bell, CheckCheck, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useAuth } from "@/hooks/useAuth";

interface NotificationItem {
    id: string;
    title: string;
    message: string;
    type: string;
    read: boolean;
    createdAt: string;
}

const PAGE_TITLES: Record<string, string> = {
    "/dashboard": "Dashboard",
    "/jobs": "Jobs",
    "/candidates": "Candidates",
    "/interviews": "Interviews",
    "/profile": "User Profile",
};

export default function Header() {
    const { user, logout } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const [profileOpen, setProfileOpen] = useState(false);
    const [notifOpen, setNotifOpen] = useState(false);
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState<NotificationItem[]>([]);

    const fetchUnreadCount = useCallback(() => {
        if (!user) return;
        api.get<number>("/notifications/unread-count")
            .then((res) => setUnreadCount(res.data))
            .catch(() => {});
    }, [user]);

    const fetchNotifications = useCallback(() => {
        if (!user) return;
        api.get<{ content: NotificationItem[] }>("/notifications", {
            params: { page: 0, size: 5 }
        })
            .then((res) => setNotifications(res.data.content))
            .catch(() => {});
    }, [user]);

    // Initial load + polling every 30 seconds
    useEffect(() => {
        fetchUnreadCount();
        fetchNotifications();
        const interval = setInterval(() => {
            fetchUnreadCount();
            fetchNotifications();
        }, 30000);
        return () => clearInterval(interval);
    }, [fetchUnreadCount, fetchNotifications]);

    // Refresh when notification dropdown opens
    useEffect(() => {
        if (notifOpen) {
            fetchUnreadCount();
            fetchNotifications();
        }
    }, [notifOpen, fetchUnreadCount, fetchNotifications]);

    const handleMarkAsRead = async (id: string) => {
        try {
            await api.patch(`/notifications/${id}/read`);
            setNotifications(prev =>
                prev.map(n => n.id === id ? { ...n, read: true } : n)
            );
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch {
            // silently handle
        }
    };

    const handleMarkAllRead = async () => {
        try {
            await api.patch("/notifications/read-all");
            setNotifications(prev => prev.map(n => ({ ...n, read: true })));
            setUnreadCount(0);
        } catch {
            // silently handle
        }
    };

    const pageTitle = PAGE_TITLES[location.pathname] ?? "Enterprise ATS";
    const displayName =
        user?.fullName?.trim() || user?.email?.split("@")[0] || "User";
    const initials = displayName
        .split(" ")
        .filter(Boolean)
        .map((n) => n[0])
        .slice(0, 2)
        .join("")
        .toUpperCase();

    const handleLogout = async () => {
        await logout();
        navigate("/login");
    };

    const timeAgo = (dateStr: string) => {
        const diff = Date.now() - new Date(dateStr).getTime();
        const mins = Math.floor(diff / 60000);
        if (mins < 1) return "just now";
        if (mins < 60) return `${mins}m ago`;
        const hrs = Math.floor(mins / 60);
        if (hrs < 24) return `${hrs}h ago`;
        const days = Math.floor(hrs / 24);
        return `${days}d ago`;
    };

    return (
        <header className="flex h-16 shrink-0 items-center justify-between border-b border-border bg-background px-6">
            <h1 className="text-xl font-semibold text-foreground">{pageTitle}</h1>

            {user && (
                <div className="flex items-center gap-2">
                    {/* Notification Bell */}
                    <DropdownMenu open={notifOpen} onOpenChange={setNotifOpen}>
                        <DropdownMenuTrigger asChild>
                            <Button
                                variant="ghost"
                                size="icon"
                                className="relative h-9 w-9 rounded-full hover:bg-accent"
                            >
                                <Bell className="h-4 w-4" />
                                {unreadCount > 0 && (
                                    <span className="absolute -top-0.5 -right-0.5 flex h-4 w-4 rounded-full bg-destructive text-[9px] text-destructive-foreground items-center justify-center font-bold ring-2 ring-background">
                                        {unreadCount > 9 ? "9+" : unreadCount}
                                    </span>
                                )}
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-80 max-h-[420px] overflow-y-auto">
                            <DropdownMenuLabel className="flex items-center justify-between">
                                <span className="text-sm font-semibold">Notifications</span>
                                {unreadCount > 0 && (
                                    <button
                                        onClick={(e) => { e.preventDefault(); handleMarkAllRead(); }}
                                        className="text-xs text-primary hover:underline flex items-center gap-1"
                                    >
                                        <CheckCheck className="h-3 w-3" />
                                        Mark all read
                                    </button>
                                )}
                            </DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            {notifications.length === 0 ? (
                                <div className="py-8 text-center text-sm text-muted-foreground">
                                    <Bell className="h-8 w-8 mx-auto mb-2 opacity-20" />
                                    No notifications yet
                                </div>
                            ) : (
                                <>
                                    {notifications.map((n) => (
                                        <DropdownMenuItem
                                            key={n.id}
                                            className={`flex flex-col items-start gap-1 px-3 py-3 cursor-pointer ${!n.read ? 'bg-primary/5' : ''}`}
                                            onClick={() => { if (!n.read) handleMarkAsRead(n.id); }}
                                        >
                                            <div className="flex items-center justify-between w-full gap-2">
                                                <span className={`text-sm font-medium truncate ${!n.read ? 'text-foreground' : 'text-muted-foreground'}`}>
                                                    {!n.read && <span className="inline-block w-1.5 h-1.5 rounded-full bg-primary mr-1.5 align-middle" />}
                                                    {n.title}
                                                </span>
                                                <span className="text-[10px] text-muted-foreground whitespace-nowrap">
                                                    {timeAgo(n.createdAt)}
                                                </span>
                                            </div>
                                            <p className="text-xs text-muted-foreground line-clamp-2 w-full">
                                                {n.message}
                                            </p>
                                        </DropdownMenuItem>
                                    ))}
                                    <DropdownMenuSeparator />
                                    <DropdownMenuItem
                                        className="text-center justify-center text-primary text-xs font-medium py-2 cursor-pointer"
                                        onClick={() => { setNotifOpen(false); navigate("/admin/notifications"); }}
                                    >
                                        <ExternalLink className="h-3 w-3 mr-1" />
                                        View all notifications
                                    </DropdownMenuItem>
                                </>
                            )}
                        </DropdownMenuContent>
                    </DropdownMenu>

                    {/* User Profile */}
                    <DropdownMenu open={profileOpen} onOpenChange={setProfileOpen}>
                        <DropdownMenuTrigger asChild>
                            <Button
                                variant="ghost"
                                className="flex items-center gap-2 rounded-full p-1 pr-3 hover:bg-accent"
                            >
                                {user?.avatarUrl ? (
                                    <img src={user.avatarUrl} alt={displayName} className="h-8 w-8 rounded-full object-cover" />
                                ) : (
                                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-muted text-sm font-bold text-muted-foreground">
                                        {initials}
                                    </div>
                                )}
                                <span className="text-sm font-medium text-foreground">
                                    {displayName}
                                </span>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-48">
                            <DropdownMenuLabel className="text-xs text-muted-foreground">
                                {user.email}
                            </DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                                onClick={() => {
                                    setProfileOpen(false);
                                    navigate("/profile");
                                }}
                            >
                                <User className="mr-2 h-4 w-4" />
                                Profile
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                                onClick={handleLogout}
                                className="text-destructive focus:text-destructive"
                            >
                                <LogOut className="mr-2 h-4 w-4" />
                                Logout
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            )}
        </header>
    );
}
