import { Shield, Lock, Unlock, Trash2, Pencil, Users, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ROLE_LABELS } from "./userManagementConstants";
import type { UserRecord, UserRole } from "@/types/user";

const ROLE_COLORS: Record<UserRole, string> = {
    SYSTEM_ADMIN: "bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-300",
    HR: "bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300",
    HR_MANAGER: "bg-cyan-100 text-cyan-800 dark:bg-cyan-900/40 dark:text-cyan-300",
    INTERVIEWER: "bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-300",
};

function getInitials(name: string): string {
    return name
        .split(" ")
        .map((n) => n[0])
        .slice(0, 2)
        .join("")
        .toUpperCase();
}

function getAvatarColor(name: string): string {
    const colors = [
        "bg-violet-500", "bg-blue-500", "bg-emerald-500",
        "bg-rose-500", "bg-amber-500", "bg-teal-500",
    ];
    let hash = 0;
    for (let i = 0; i < name.length; i++) hash += name.charCodeAt(i);
    return colors[hash % colors.length];
}

interface UserTableProps {
    users: UserRecord[];
    isLoading: boolean;
    actionLoading: string | null;
    deleteConfirmId: string | null;
    onEdit: (id: string) => void;
    onLockToggle: (user: UserRecord) => void;
    onDeleteRequest: (id: string | null) => void;
    onDeleteConfirm: (id: string) => void;
}

export function UserTable({
    users,
    isLoading,
    actionLoading,
    deleteConfirmId,
    onEdit,
    onLockToggle,
    onDeleteRequest,
    onDeleteConfirm,
}: UserTableProps) {
    if (isLoading) {
        return (
            <div className="overflow-hidden rounded-lg border border-border bg-card shadow-sm">
                <div className="flex items-center justify-center py-20 text-muted-foreground">
                    <RefreshCw className="mr-2 h-5 w-5 animate-spin" />
                    Loading users…
                </div>
            </div>
        );
    }

    if (users.length === 0) {
        return (
            <div className="overflow-hidden rounded-lg border border-border bg-card shadow-sm">
                <div className="flex flex-col items-center justify-center gap-2 py-20 text-muted-foreground">
                    <Users className="h-10 w-10 opacity-30" />
                    <p className="text-sm">No users found.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="overflow-hidden rounded-lg border border-border bg-card shadow-sm">
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-border">
                    <thead className="bg-muted/50">
                        <tr>
                            <th className="py-3 pl-6 pr-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                User
                            </th>
                            <th className="px-3 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                Department
                            </th>
                            <th className="px-3 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                Role
                            </th>
                            <th className="px-3 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                Status
                            </th>
                            <th className="py-3 pl-3 pr-6 text-right text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                Actions
                            </th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-border">
                        {users.map((user) => {
                            const isActing = actionLoading === user.id;
                            const status = !user.active
                                ? { label: "Inactive", cls: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400" }
                                : user.accountLocked
                                    ? { label: "Locked", cls: "bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-400" }
                                    : { label: "Active", cls: "bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-400" };

                            return (
                                <tr key={user.id} className="transition-colors hover:bg-muted/30">
                                    <td className="whitespace-nowrap py-4 pl-6 pr-3">
                                        <div className="flex items-center gap-3">
                                            <span
                                                className={`inline-flex h-9 w-9 items-center justify-center rounded-full text-sm font-semibold text-white ${getAvatarColor(user.fullName)}`}
                                            >
                                                {getInitials(user.fullName)}
                                            </span>
                                            <div>
                                                <div className="font-medium text-foreground">{user.fullName}</div>
                                                <div className="text-xs text-muted-foreground">{user.email}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="whitespace-nowrap px-3 py-4 text-sm text-muted-foreground">
                                        {user.department || "—"}
                                    </td>
                                    <td className="whitespace-nowrap px-3 py-4">
                                        <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold ${ROLE_COLORS[user.role]}`}>
                                            <Shield className="h-3 w-3" />
                                            {ROLE_LABELS[user.role]}
                                        </span>
                                    </td>
                                    <td className="whitespace-nowrap px-3 py-4">
                                        <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${status.cls}`}>
                                            {status.label}
                                        </span>
                                    </td>
                                    <td className="whitespace-nowrap py-4 pl-3 pr-6 text-right">
                                        <div className="flex items-center justify-end gap-1">
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                className="h-8 w-8 p-0"
                                                onClick={() => onEdit(user.id)}
                                                disabled={isActing}
                                                title="Edit user"
                                            >
                                                <Pencil className="h-3.5 w-3.5" />
                                            </Button>

                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                className="h-8 w-8 p-0"
                                                onClick={() => onLockToggle(user)}
                                                disabled={isActing || !user.active}
                                                title={user.accountLocked ? "Unlock account" : "Lock account"}
                                            >
                                                {isActing ? (
                                                    <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                                                ) : user.accountLocked ? (
                                                    <Unlock className="h-3.5 w-3.5 text-amber-500" />
                                                ) : (
                                                    <Lock className="h-3.5 w-3.5" />
                                                )}
                                            </Button>

                                            {deleteConfirmId === user.id ? (
                                                <div className="flex items-center gap-1">
                                                    <Button
                                                        variant="destructive"
                                                        size="sm"
                                                        className="h-7 px-2 text-xs text-white"
                                                        onClick={() => onDeleteConfirm(user.id)}
                                                        disabled={isActing}
                                                    >
                                                        Confirm
                                                    </Button>
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        className="h-7 px-2 text-xs"
                                                        onClick={() => onDeleteRequest(null)}
                                                    >
                                                        Cancel
                                                    </Button>
                                                </div>
                                            ) : (
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    className="h-8 w-8 p-0 text-muted-foreground hover:text-destructive"
                                                    onClick={() => onDeleteRequest(user.id)}
                                                    disabled={isActing}
                                                    title="Delete user"
                                                >
                                                    <Trash2 className="h-3.5 w-3.5" />
                                                </Button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
