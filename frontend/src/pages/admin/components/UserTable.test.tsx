import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { UserTable } from "./UserTable";
import type { UserRecord } from "@/types/user";

afterEach(() => {
    cleanup();
});

const sampleUsers: UserRecord[] = [
    {
        id: "u-1",
        fullName: "Alice Nguyen",
        email: "alice@ats.com",
        role: "HR_MANAGER",
        department: "Engineering",
        active: true,
        accountLocked: false,
    },
    {
        id: "u-2",
        fullName: "Bob Tran",
        email: "bob@ats.com",
        role: "INTERVIEWER",
        department: "",
        active: true,
        accountLocked: true,
    },
];

describe("UserTable", () => {
    it("renders loading indicator when isLoading is true", () => {
        render(
            <UserTable
                users={[]}
                isLoading={true}
                actionLoading={null}
                deleteConfirmId={null}
                onEdit={vi.fn()}
                onLockToggle={vi.fn()}
                onDeleteRequest={vi.fn()}
                onDeleteConfirm={vi.fn()}
            />
        );
        expect(screen.getByText("Loading users…")).toBeTruthy();
    });

    it("renders empty state when users list is empty", () => {
        render(
            <UserTable
                users={[]}
                isLoading={false}
                actionLoading={null}
                deleteConfirmId={null}
                onEdit={vi.fn()}
                onLockToggle={vi.fn()}
                onDeleteRequest={vi.fn()}
                onDeleteConfirm={vi.fn()}
            />
        );
        expect(screen.getByText("No users found.")).toBeTruthy();
    });

    it("renders user rows with role and status badges", () => {
        render(
            <UserTable
                users={sampleUsers}
                isLoading={false}
                actionLoading={null}
                deleteConfirmId={null}
                onEdit={vi.fn()}
                onLockToggle={vi.fn()}
                onDeleteRequest={vi.fn()}
                onDeleteConfirm={vi.fn()}
            />
        );
        expect(screen.getByText("Alice Nguyen")).toBeTruthy();
        expect(screen.getByText("HR Manager")).toBeTruthy();
        expect(screen.getByText("Active")).toBeTruthy();
        expect(screen.getByText("Bob Tran")).toBeTruthy();
        expect(screen.getByText("Locked")).toBeTruthy();
    });

    it("invokes onEdit and onLockToggle when action buttons are clicked", () => {
        const onEdit = vi.fn();
        const onLockToggle = vi.fn();
        render(
            <UserTable
                users={[sampleUsers[0]]}
                isLoading={false}
                actionLoading={null}
                deleteConfirmId={null}
                onEdit={onEdit}
                onLockToggle={onLockToggle}
                onDeleteRequest={vi.fn()}
                onDeleteConfirm={vi.fn()}
            />
        );

        fireEvent.click(screen.getByTitle("Edit user"));
        expect(onEdit).toHaveBeenCalledWith("u-1");

        fireEvent.click(screen.getByTitle("Lock account"));
        expect(onLockToggle).toHaveBeenCalledWith(sampleUsers[0]);
    });

    it("renders inline delete confirmation and triggers onDeleteConfirm", () => {
        const onDeleteConfirm = vi.fn();
        render(
            <UserTable
                users={[sampleUsers[0]]}
                isLoading={false}
                actionLoading={null}
                deleteConfirmId="u-1"
                onEdit={vi.fn()}
                onLockToggle={vi.fn()}
                onDeleteRequest={vi.fn()}
                onDeleteConfirm={onDeleteConfirm}
            />
        );

        fireEvent.click(screen.getByText("Confirm"));
        expect(onDeleteConfirm).toHaveBeenCalledWith("u-1");
    });
});
