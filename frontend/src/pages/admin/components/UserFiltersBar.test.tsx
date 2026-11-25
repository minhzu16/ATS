import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { UserFiltersBar } from "./UserFiltersBar";
import type { Department } from "@/types/admin";

afterEach(() => {
    cleanup();
});

const departments: Department[] = [
    { id: "dept-1", name: "Engineering", description: "Tech", employeeCount: 10, openPositions: 2, status: true },
    { id: "dept-2", name: "Marketing", description: "Growth", employeeCount: 5, openPositions: 1, status: true },
];

describe("UserFiltersBar", () => {
    it("renders search input and all three filter dropdowns", () => {
        render(
            <UserFiltersBar
                search=""
                roleFilter=""
                statusFilter=""
                departmentFilter=""
                departments={departments}
                onSearchChange={vi.fn()}
                onRoleFilterChange={vi.fn()}
                onStatusFilterChange={vi.fn()}
                onDepartmentFilterChange={vi.fn()}
            />
        );

        expect(screen.getByPlaceholderText("Search by name or email…")).toBeTruthy();
        expect(screen.getByLabelText("Filter by role")).toBeTruthy();
        expect(screen.getByLabelText("Filter by status")).toBeTruthy();
        expect(screen.getByLabelText("Filter by department")).toBeTruthy();
    });

    it("calls onSearchChange when typing into search input", () => {
        const onSearchChange = vi.fn();
        render(
            <UserFiltersBar
                search=""
                roleFilter=""
                statusFilter=""
                departmentFilter=""
                departments={departments}
                onSearchChange={onSearchChange}
                onRoleFilterChange={vi.fn()}
                onStatusFilterChange={vi.fn()}
                onDepartmentFilterChange={vi.fn()}
            />
        );

        fireEvent.change(screen.getByPlaceholderText("Search by name or email…"), {
            target: { value: "alice" },
        });
        expect(onSearchChange).toHaveBeenCalledWith("alice");
    });

    it("calls onRoleFilterChange and onStatusFilterChange on selection", () => {
        const onRoleFilterChange = vi.fn();
        const onStatusFilterChange = vi.fn();
        render(
            <UserFiltersBar
                search=""
                roleFilter=""
                statusFilter=""
                departmentFilter=""
                departments={departments}
                onSearchChange={vi.fn()}
                onRoleFilterChange={onRoleFilterChange}
                onStatusFilterChange={onStatusFilterChange}
                onDepartmentFilterChange={vi.fn()}
            />
        );

        fireEvent.change(screen.getByLabelText("Filter by role"), { target: { value: "HR" } });
        expect(onRoleFilterChange).toHaveBeenCalledWith("HR");

        fireEvent.change(screen.getByLabelText("Filter by status"), { target: { value: "locked" } });
        expect(onStatusFilterChange).toHaveBeenCalledWith("locked");
    });

    it("renders department options and triggers onDepartmentFilterChange", () => {
        const onDepartmentFilterChange = vi.fn();
        render(
            <UserFiltersBar
                search=""
                roleFilter=""
                statusFilter=""
                departmentFilter=""
                departments={departments}
                onSearchChange={vi.fn()}
                onRoleFilterChange={vi.fn()}
                onStatusFilterChange={vi.fn()}
                onDepartmentFilterChange={onDepartmentFilterChange}
            />
        );

        expect(screen.getByText("Engineering")).toBeTruthy();
        fireEvent.change(screen.getByLabelText("Filter by department"), { target: { value: "dept-1" } });
        expect(onDepartmentFilterChange).toHaveBeenCalledWith("dept-1");
    });
});
