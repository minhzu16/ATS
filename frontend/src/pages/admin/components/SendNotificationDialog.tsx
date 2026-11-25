import { Bell, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";

interface SendNotificationDialogProps {
    isOpen: boolean;
    isSending: boolean;
    targetType: "broadcast" | "role";
    selectedRole: string;
    notifType: string;
    title: string;
    message: string;
    onOpenChange: (open: boolean) => void;
    onTargetTypeChange: (value: "broadcast" | "role") => void;
    onSelectedRoleChange: (value: string) => void;
    onNotifTypeChange: (value: string) => void;
    onTitleChange: (value: string) => void;
    onMessageChange: (value: string) => void;
    onSubmit: () => void;
}

export function SendNotificationDialog({
    isOpen,
    isSending,
    targetType,
    selectedRole,
    notifType,
    title,
    message,
    onOpenChange,
    onTargetTypeChange,
    onSelectedRoleChange,
    onNotifTypeChange,
    onTitleChange,
    onMessageChange,
    onSubmit,
}: SendNotificationDialogProps) {
    return (
        <Dialog open={isOpen} onOpenChange={onOpenChange}>
            <DialogTrigger asChild>
                <Button size="sm" className="rounded-full shadow-lg shadow-primary/20">
                    <Bell className="mr-2 h-4 w-4" />
                    Send Notification
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px] glass-morphism border-primary/20">
                <DialogHeader>
                    <DialogTitle className="text-xl flex items-center gap-2">
                        <Bell className="h-5 w-5 text-primary" />
                        Send Notification
                    </DialogTitle>
                    <DialogDescription>
                        Broadcast a message to all users or specific roles.
                    </DialogDescription>
                </DialogHeader>
                <div className="grid gap-6 py-4">
                    <div className="space-y-2">
                        <Label htmlFor="target">Recipient Target</Label>
                        <Select
                            value={targetType}
                            onValueChange={(v: unknown) => {
                                if (v === "broadcast" || v === "role") {
                                    onTargetTypeChange(v);
                                }
                            }}
                        >
                            <SelectTrigger className="rounded-xl border-primary/20 bg-background/50">
                                <SelectValue placeholder="Select target" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="broadcast">All Users (Broadcast)</SelectItem>
                                <SelectItem value="role">Specific Role</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    {targetType === "role" && (
                        <div className="space-y-2 animate-in slide-in-from-top-2 duration-300">
                            <Label htmlFor="role">Target Role</Label>
                            <Select value={selectedRole} onValueChange={onSelectedRoleChange}>
                                <SelectTrigger className="rounded-xl border-primary/20 bg-background/50">
                                    <SelectValue placeholder="Select role" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="SYSTEM_ADMIN">System Admin</SelectItem>
                                    <SelectItem value="HR_MANAGER">HR Manager</SelectItem>
                                    <SelectItem value="RECRUITER">Recruiter</SelectItem>
                                    <SelectItem value="INTERVIEWER">Interviewer</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    )}

                    <div className="space-y-2">
                        <Label htmlFor="type">Notification Type</Label>
                        <Select value={notifType} onValueChange={onNotifTypeChange}>
                            <SelectTrigger className="rounded-xl border-primary/20 bg-background/50">
                                <SelectValue placeholder="Select type" />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="SYSTEM_ALERT">System Alert</SelectItem>
                                <SelectItem value="ONBOARDING_ASSIGNED">Onboarding</SelectItem>
                                <SelectItem value="INTERVIEW_PENDING">Interview</SelectItem>
                                <SelectItem value="OFFER_APPROVED">Offer</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="title">Title</Label>
                        <Input
                            id="title"
                            placeholder="Enter notification title"
                            value={title}
                            onChange={(e) => onTitleChange(e.target.value)}
                            className="rounded-xl border-primary/20 bg-background/50"
                        />
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="message">Message</Label>
                        <Textarea
                            id="message"
                            placeholder="Enter notification message"
                            value={message}
                            onChange={(e) => onMessageChange(e.target.value)}
                            className="min-h-[100px] rounded-xl border-primary/20 bg-background/50"
                        />
                    </div>
                </div>
                <DialogFooter>
                    <Button
                        variant="outline"
                        onClick={() => onOpenChange(false)}
                        className="rounded-full"
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={onSubmit}
                        disabled={isSending}
                        className="rounded-full px-8 shadow-lg shadow-primary/20"
                    >
                        {isSending && <RefreshCw className="mr-2 h-4 w-4 animate-spin" />}
                        Send Now
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
