import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import type { UserRole } from "@/types/user";
import type { Department } from "@/types/admin";
import { ROLE_LABELS, type StatusFilter } from "./userManagementConstants";

interface UserFiltersBarProps {
    search: string;
    roleFilter: UserRole | "";
    statusFilter: StatusFilter;
    departmentFilter: string;
    departments: Department[];
    onSearchChange: (value: string) => void;
    onRoleFilterChange: (value: UserRole | "") => void;
    onStatusFilterChange: (value: StatusFilter) => void;
    onDepartmentFilterChange: (value: string) => void;
}

export function UserFiltersBar({
    search,
    roleFilter,
    statusFilter,
    departmentFilter,
    departments,
    onSearchChange,
    onRoleFilterChange,
    onStatusFilterChange,
    onDepartmentFilterChange,
}: UserFiltersBarProps) {
    return (
        <div className="flex flex-col gap-3 rounded-lg border border-border bg-card p-4 sm:flex-row">
            <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                    placeholder="Search by name or email…"
                    className="pl-9"
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                />
            </div>
            <select
                aria-label="Filter by role"
                className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                value={roleFilter}
                onChange={(e) => onRoleFilterChange(e.target.value as UserRole | "")}
            >
                <option value="">All Roles</option>
                {Object.entries(ROLE_LABELS).map(([value, label]) => (
                    <option key={value} value={value}>{label}</option>
                ))}
            </select>
            <select
                aria-label="Filter by status"
                className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                value={statusFilter}
                onChange={(e) => onStatusFilterChange(e.target.value as StatusFilter)}
            >
                <option value="">All Status</option>
                <option value="active">Active</option>
                <option value="locked">Locked</option>
                <option value="inactive">Inactive</option>
            </select>
            <select
                aria-label="Filter by department"
                className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                value={departmentFilter}
                onChange={(e) => onDepartmentFilterChange(e.target.value)}
            >
                <option value="">All Departments</option>
                {departments.map((d) => (
                    <option key={d.id} value={d.id}>{d.name}</option>
                ))}
            </select>
        </div>
    );
}
