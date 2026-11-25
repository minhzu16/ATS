import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { NotificationList } from "./NotificationList";
import type { Notification } from "@/types/admin";

afterEach(() => {
    cleanup();
});

const sampleNotifications: Notification[] = [
    {
        id: "n-1",
        title: "Interview Scheduled",
        message: "Candidate Nguyen Van A at 10:00 AM",
        type: "INTERVIEW_PENDING",
        read: false,
        createdAt: "2026-01-15T10:00:00Z",
    },
    {
        id: "n-2",
        title: "Offer Approved",
        message: "Offer for Tran Thi B was approved",
        type: "OFFER_APPROVED",
        read: true,
        createdAt: "2026-01-14T09:00:00Z",
    },
];

describe("NotificationList", () => {
    it("renders loading skeleton placeholders when loading", () => {
        const { container } = render(
            <NotificationList
                notifications={[]}
                isLoading={true}
                activeTab="personal"
                onReload={vi.fn()}
                onMarkAsRead={vi.fn()}
                onDelete={vi.fn()}
            />
        );
        expect(container.querySelectorAll(".animate-pulse").length).toBe(5);
    });

    it("renders empty personal inbox message and triggers onReload", () => {
        const onReload = vi.fn();
        render(
            <NotificationList
                notifications={[]}
                isLoading={false}
                activeTab="personal"
                onReload={onReload}
                onMarkAsRead={vi.fn()}
                onDelete={vi.fn()}
            />
        );
        expect(screen.getByText("Your inbox is empty")).toBeTruthy();
        fireEvent.click(screen.getByText("Check again"));
        expect(onReload).toHaveBeenCalled();
    });

    it("renders empty system log message when activeTab is system", () => {
        render(
            <NotificationList
                notifications={[]}
                isLoading={false}
                activeTab="system"
                onReload={vi.fn()}
                onMarkAsRead={vi.fn()}
                onDelete={vi.fn()}
            />
        );
        expect(screen.getByText("No system notifications found")).toBeTruthy();
    });

    it("renders unread notification with Mark as read button on personal tab", () => {
        const onMarkAsRead = vi.fn();
        render(
            <NotificationList
                notifications={sampleNotifications}
                isLoading={false}
                activeTab="personal"
                onReload={vi.fn()}
                onMarkAsRead={onMarkAsRead}
                onDelete={vi.fn()}
            />
        );
        expect(screen.getByText("Interview Scheduled")).toBeTruthy();
        const markBtn = screen.getByText("Mark as read");
        fireEvent.click(markBtn);
        expect(onMarkAsRead).toHaveBeenCalledWith("n-1");
    });

    it("hides Mark as read button on system tab", () => {
        render(
            <NotificationList
                notifications={sampleNotifications}
                isLoading={false}
                activeTab="system"
                onReload={vi.fn()}
                onMarkAsRead={vi.fn()}
                onDelete={vi.fn()}
            />
        );
        expect(screen.queryByText("Mark as read")).toBeNull();
    });
});
