import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
    Plus, Users, RefreshCw, AlertTriangle, Download, FileSpreadsheet, FileText,
    ChevronLeft, ChevronRight
} from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { userService } from "@/services/userService";
import { adminService } from "@/services/adminService";
import type { UserRecord, UserRole } from "@/types/user";
import type { Department } from "@/types/admin";
import { UserFiltersBar } from "./components/UserFiltersBar";
import { type StatusFilter } from "./components/userManagementConstants";
import { UserTable } from "./components/UserTable";

export default function AdminUsersPage() {
    const navigate = useNavigate();
    const [users, setUsers] = useState<UserRecord[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [reloadKey, setReloadKey] = useState(0);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState<UserRole | "">("");
    const [statusFilter, setStatusFilter] = useState<StatusFilter>("");
    const [departmentFilter, setDepartmentFilter] = useState("");
    const [departments, setDepartments] = useState<Department[]>([]);

    const [page, setPage] = useState(0);
    const [size] = useState(10);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const [actionLoading, setActionLoading] = useState<string | null>(null);
    const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);
    const [isExporting, setIsExporting] = useState(false);

    useEffect(() => {
        let ignore = false;
        userService.getUsers(page, size, departmentFilter || undefined)
            .then((data) => {
                if (!ignore) {
                    setUsers(data.content);
                    setTotalPages(data.totalPages);
                    setTotalElements(data.totalElements);
                    setError("");
                    setIsLoading(false);
                }
            })
            .catch(() => {
                if (!ignore) {
                    setError("Failed to load users. Please try again.");
                    setIsLoading(false);
                }
            });
        return () => {
            ignore = true;
        };
    }, [page, size, departmentFilter, reloadKey]);

    const reload = () => {
        setIsLoading(true);
        setReloadKey((k) => k + 1);
    };

    useEffect(() => {
        adminService.getDepartments(0, 100)
            .then((data) => setDepartments(data.content))
            .catch(() => {});
    }, []);

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

                    <Button variant="outline" size="sm" onClick={reload} disabled={isLoading}>
                        <RefreshCw className={`mr-1.5 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />
                        Refresh
                    </Button>
                    <Button size="sm" onClick={() => navigate("/admin/users/create")}>
                        <Plus className="mr-1.5 h-4 w-4" />
                        Add User
                    </Button>
                </div>
            </div>

            <UserFiltersBar
                search={search}
                roleFilter={roleFilter}
                statusFilter={statusFilter}
                departmentFilter={departmentFilter}
                departments={departments}
                onSearchChange={setSearch}
                onRoleFilterChange={setRoleFilter}
                onStatusFilterChange={setStatusFilter}
                onDepartmentFilterChange={(value) => {
                    setIsLoading(true);
                    setDepartmentFilter(value);
                    setPage(0);
                }}
            />

            {error && (
                <div className="flex items-center gap-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3">
                    <AlertTriangle className="h-4 w-4 text-destructive" />
                    <p className="text-sm text-destructive">{error}</p>
                </div>
            )}

            <UserTable
                users={filtered}
                isLoading={isLoading}
                actionLoading={actionLoading}
                deleteConfirmId={deleteConfirmId}
                onEdit={(id) => navigate(`/admin/users/${id}/edit`)}
                onLockToggle={handleLockToggle}
                onDeleteRequest={setDeleteConfirmId}
                onDeleteConfirm={handleDelete}
            />

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
                            onClick={() => {
                                setIsLoading(true);
                                setPage((p) => Math.max(0, p - 1));
                            }}
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
                            onClick={() => {
                                setIsLoading(true);
                                setPage((p) => Math.min(totalPages - 1, p + 1));
                            }}
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
