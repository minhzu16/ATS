import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { SendNotificationDialog } from "./SendNotificationDialog";

afterEach(() => {
    cleanup();
});

describe("SendNotificationDialog", () => {
    it("renders dialog fields when open and submits notification", () => {
        const onSubmit = vi.fn();
        const onTitleChange = vi.fn();
        const onMessageChange = vi.fn();

        render(
            <SendNotificationDialog
                isOpen={true}
                isSending={false}
                targetType="broadcast"
                selectedRole="RECRUITER"
                notifType="SYSTEM_ALERT"
                title="System Update"
                message="Scheduled tonight"
                onOpenChange={vi.fn()}
                onTargetTypeChange={vi.fn()}
                onSelectedRoleChange={vi.fn()}
                onNotifTypeChange={vi.fn()}
                onTitleChange={onTitleChange}
                onMessageChange={onMessageChange}
                onSubmit={onSubmit}
            />
        );

        expect(screen.getByLabelText("Title")).toBeTruthy();
        expect(screen.getByLabelText("Message")).toBeTruthy();

        fireEvent.change(screen.getByLabelText("Title"), { target: { value: "Alert" } });
        expect(onTitleChange).toHaveBeenCalledWith("Alert");

        fireEvent.click(screen.getByText("Send Now"));
        expect(onSubmit).toHaveBeenCalled();
    });

    it("shows Target Role selector only when targetType is role", () => {
        render(
            <SendNotificationDialog
                isOpen={true}
                isSending={false}
                targetType="role"
                selectedRole="HR_MANAGER"
                notifType="SYSTEM_ALERT"
                title=""
                message=""
                onOpenChange={vi.fn()}
                onTargetTypeChange={vi.fn()}
                onSelectedRoleChange={vi.fn()}
                onNotifTypeChange={vi.fn()}
                onTitleChange={vi.fn()}
                onMessageChange={vi.fn()}
                onSubmit={vi.fn()}
            />
        );

        expect(screen.getByText("Target Role")).toBeTruthy();
    });
});
