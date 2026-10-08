import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
    Search, Plus, Shield, Lock, Unlock, Trash2, Pencil,
    Users, RefreshCw, AlertTriangle, Download, FileSpreadsheet, FileText,
    ChevronLeft, ChevronRight
} from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { userService } from "@/services/userService";
import { adminService } from "@/services/adminService";
import type { UserRecord, UserRole } from "@/types/user";
import type { Department } from "@/types/admin";

const ROLE_LABELS: Record<UserRole, string> = {
    SYSTEM_ADMIN: "Admin",
    HR: "HR",
    HR_MANAGER: "HR Manager",
    INTERVIEWER: "Interviewer",
};

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

type StatusFilter = "" | "active" | "locked" | "inactive";

export default function AdminUsersPage() {
    const navigate = useNavigate();
    const [users, setUsers] = useState<UserRecord[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState<UserRole | "">("");
    const [statusFilter, setStatusFilter] = useState<StatusFilter>("");
    const [departmentFilter, setDepartmentFilter] = useState("");
    const [departments, setDepartments] = useState<Department[]>([]);
    
    // Pagination
    const [page, setPage] = useState(0);
    const [size] = useState(10);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [actionLoading, setActionLoading] = useState<string | null>(null);
    const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);
    const [isExporting, setIsExporting] = useState(false);

    const loadUsers = useCallback(async () => {
        setIsLoading(true);
        setError("");
        try {
            const data = await userService.getUsers(page, size, departmentFilter || undefined);
            setUsers(data.content);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
        } catch {
            setError("Failed to load users. Please try again.");
        } finally {
            setIsLoading(false);
        }
    }, [page, size, departmentFilter]);

    useEffect(() => {
        adminService.getDepartments(0, 100)
            .then((data) => setDepartments(data.content))
            .catch(() => {});
    }, []);

    useEffect(() => {
        loadUsers();
    }, [loadUsers]);

    const handleLockToggle = async (user: UserRecord) => {
        setActionLoading(user.id);
        try {
            const updated = user.accountLocked
                ? await userService.unlockUser(user.id)
                : await userService.lockUser(user.id);
            setUsers((prev) => prev.map((u) => u.id === updated.id ? updated : u));
        } catch {
            // silently ignore — user can retry
        } finally {
            setActionLoading(null);
        }
    };

    const handleDelete = async (id: string) => {
        setActionLoading(id);
        try {
            await userService.deleteUser(id);
            setUsers((prev) => prev.filter((u) => u.id !== id));
        } catch {
            // silently ignore
        } finally {
            setActionLoading(null);
            setDeleteConfirmId(null);
        }
    };

    // Client-side filter
    const filtered = users.filter((u) => {
        const matchSearch =
            !search ||
            u.fullName.toLowerCase().includes(search.toLowerCase()) ||
            u.email.toLowerCase().includes(search.toLowerCase());
        const matchRole = !roleFilter || u.role === roleFilter;
        const matchStatus =
            !statusFilter ||
            (statusFilter === "active" && u.active && !u.accountLocked) ||
            (statusFilter === "locked" && u.accountLocked) ||
            (statusFilter === "inactive" && !u.active && !u.accountLocked);
        return matchSearch && matchRole && matchStatus;
    });

    const handleExport = async (format: "csv" | "excel") => {
        setIsExporting(true);
        try {
            const blob = await adminService.exportUsers(format);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `users.${format === "csv" ? "csv" : "xlsx"}`;
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
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                        <Users className="h-5 w-5" />
                    </div>
                    <div>
                        <h1 className="text-xl font-semibold text-foreground">User Management</h1>
                        <p className="text-sm text-muted-foreground">
                            {users.length} user{users.length !== 1 ? "s" : ""} total
                        </p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="outline" size="sm" disabled={isExporting}>
                                {isExporting ? <RefreshCw className="mr-1.5 h-4 w-4 animate-spin" /> : <Download className="mr-1.5 h-4 w-4" />}
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

                    <Button variant="outline" size="sm" onClick={loadUsers} disabled={isLoading}>
                        <RefreshCw className={`mr-1.5 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />
                        Refresh
                    </Button>
                    <Button size="sm" onClick={() => navigate("/admin/users/create")}>
                        <Plus className="mr-1.5 h-4 w-4" />
                        Add User
                    </Button>
                </div>
            </div>

            {/* Filters */}
            <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 sm:flex-row">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <Input
                        placeholder="Search by name or email…"
                        className="pl-9"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
                <select
                    className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={roleFilter}
                    onChange={(e) => setRoleFilter(e.target.value as UserRole | "")}
                >
                    <option value="">All Roles</option>
                    {Object.entries(ROLE_LABELS).map(([value, label]) => (
                        <option key={value} value={value}>{label}</option>
                    ))}
                </select>
                <select
                    className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
                >
                    <option value="">All Status</option>
                    <option value="active">Active</option>
                    <option value="locked">Locked</option>
                    <option value="inactive">Inactive</option>
                </select>
                <select
                    className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={departmentFilter}
                    onChange={(e) => {
                        setDepartmentFilter(e.target.value);
                        setPage(0); // reset to first page on filter
                    }}
                >
                    <option value="">All Departments</option>
                    {departments.map((d) => (
                        <option key={d.id} value={d.id}>{d.name}</option>
                    ))}
                </select>
            </div>

            {/* Error */}
            {error && (
                <div className="flex items-center gap-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3">
                    <AlertTriangle className="h-4 w-4 text-destructive" />
                    <p className="text-sm text-destructive">{error}</p>
                </div>
            )}

            {/* Table */}
            <div className="overflow-hidden rounded-lg border border-border bg-card shadow-sm">
                {isLoading ? (
                    <div className="flex items-center justify-center py-20 text-muted-foreground">
                        <RefreshCw className="mr-2 h-5 w-5 animate-spin" />
                        Loading users…
                    </div>
                ) : filtered.length === 0 ? (
                    <div className="flex flex-col items-center justify-center gap-2 py-20 text-muted-foreground">
                        <Users className="h-10 w-10 opacity-30" />
                        <p className="text-sm">No users found.</p>
                    </div>
                ) : (
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
                                {filtered.map((user) => {
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
                                                    {/* Edit */}
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        className="h-8 w-8 p-0"
                                                        onClick={() => navigate(`/admin/users/${user.id}/edit`)}
                                                        disabled={isActing}
                                                        title="Edit user"
                                                    >
                                                        <Pencil className="h-3.5 w-3.5" />
                                                    </Button>

                                                    {/* Lock / Unlock */}
                                                    <Button
                                                        variant="ghost"
                                                        size="sm"
                                                        className="h-8 w-8 p-0"
                                                        onClick={() => handleLockToggle(user)}
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

                                                    {/* Delete */}
                                                    {deleteConfirmId === user.id ? (
                                                        <div className="flex items-center gap-1">
                                                            <Button
                                                                variant="destructive"
                                                                size="sm"
                                                                className="h-7 px-2 text-xs text-white"
                                                                onClick={() => handleDelete(user.id)}
                                                                disabled={isActing}
                                                            >
                                                                Confirm
                                                            </Button>
                                                            <Button
                                                                variant="ghost"
                                                                size="sm"
                                                                className="h-7 px-2 text-xs"
                                                                onClick={() => setDeleteConfirmId(null)}
                                                            >
                                                                Cancel
                                                            </Button>
                                                        </div>
                                                    ) : (
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            className="h-8 w-8 p-0 text-muted-foreground hover:text-destructive"
                                                            onClick={() => setDeleteConfirmId(user.id)}
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
                )}
            </div>

            {/* Summary & Pagination */}
            {!isLoading && (
                <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
                    <p className="text-sm text-muted-foreground">
                        Showing <span className="font-medium">{users.length}</span> of <span className="font-medium">{totalElements}</span> users
                    </p>
                    <div className="flex items-center gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={page === 0}
                            onClick={() => setPage((p) => Math.max(0, p - 1))}
                        >
                            <ChevronLeft className="h-4 w-4" />
                            Previous
                        </Button>
                        <span className="text-sm text-muted-foreground px-2">
                            Page {page + 1} of {Math.max(1, totalPages)}
                        </span>
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={page >= totalPages - 1}
                            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                        >
                            Next
                            <ChevronRight className="h-4 w-4" />
                        </Button>
                    </div>
                </div>
            )}
        </div>
    );
}
