import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/hooks/useAuth";
import { Lock, KeyRound, Camera } from "lucide-react";
import { userService } from "@/services/userService";

export default function ProfilePage() {
    const { user, updateUser } = useAuth();
    const navigate = useNavigate();

    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState("");
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        let cancelled = false;
        void userService
            .getMe()
            .then((me) => {
                if (cancelled) return;
                updateUser({
                    fullName: me.fullName,
                    email: me.email,
                    department: me.department,
                    avatarUrl: me.avatarUrl,
                });
            })
            .catch(() => {
                /* session may be invalid; ignore */
            });
        return () => {
            cancelled = true;
        };
    }, [updateUser]);

    if (!user) return null;

    const handleAvatarClick = () => {
        if (!uploading) fileInputRef.current?.click();
    };

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        if (file.size > 5 * 1024 * 1024) {
            setError("Avatar file size must be less than 5MB.");
            return;
        }

        setError("");
        setUploading(true);
        try {
            const data = await userService.uploadAvatar(file);
            updateUser({ avatarUrl: data.avatarUrl });
        } catch (err: any) {
            setError(err.response?.data?.message || "Failed to upload avatar. Please try again.");
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="mx-auto max-w-4xl space-y-6">
            {/* Profile Header */}
            <div className="overflow-hidden rounded-lg border border-border bg-card shadow-sm">
                <div className="h-32 bg-gradient-to-r from-slate-700 to-slate-900" />
                <div className="px-6 pb-6">
                    <div className="relative -mt-12 mb-4 flex items-end">
                        <div
                            className="group relative flex h-24 w-24 cursor-pointer items-center justify-center overflow-hidden rounded-full bg-muted text-2xl font-bold text-muted-foreground ring-4 ring-card"
                            onClick={handleAvatarClick}
                        >
                            {user.avatarUrl ? (
                                <img src={user.avatarUrl} alt={user.fullName} className="h-full w-full object-cover" />
                            ) : (
                                <span>
                                    {user.fullName
                                        .split(" ")
                                        .map((n) => n[0])
                                        .slice(0, 2)
                                        .join("")}
                                </span>
                            )}
                            <div className="absolute inset-0 flex items-center justify-center bg-black/50 opacity-0 transition-opacity group-hover:opacity-100">
                                <Camera className="h-6 w-6 text-white" />
                            </div>
                        </div>
                        <input
                            type="file"
                            accept="image/*"
                            className="hidden"
                            ref={fileInputRef}
                            onChange={handleFileChange}
                        />
                        <div className="mb-1 ml-4 flex flex-col justify-end">
                            <h2 className="text-2xl font-bold text-card-foreground">
                                {user.fullName}
                            </h2>
                            <p className="text-sm text-muted-foreground">
                                {user.role.replace(/_/g, " ")} {user.department ? `• ${user.department}` : ""}
                            </p>
                            {uploading && <p className="text-xs text-blue-500 mt-1">Uploading avatar...</p>}
                            {error && <p className="text-xs text-destructive mt-1">{error}</p>}
                        </div>
                    </div>
                </div>
            </div>

            {/* Content Grid */}
            <div className="mx-auto max-w-2xl space-y-6">
                <div className="space-y-6">
                    {/* Personal Information */}
                    <div className="rounded-lg border border-border bg-card p-6 shadow-sm">
                        <h3 className="mb-4 text-lg font-medium text-card-foreground">
                            Personal Information
                        </h3>
                        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                            <div className="space-y-1.5">
                                <Label className="text-sm font-medium text-muted-foreground">Full Name</Label>
                                <div className="rounded-md border border-border bg-muted/50 px-3 py-2 text-sm text-muted-foreground">
                                    {user.fullName}
                                </div>
                            </div>
                            <div className="space-y-1.5">
                                <Label className="text-sm font-medium text-muted-foreground">Email Address</Label>
                                <div className="rounded-md border border-border bg-muted/50 px-3 py-2 text-sm text-muted-foreground">
                                    {user.email}
                                </div>
                            </div>
                            <div className="space-y-1.5">
                                <Label className="text-sm font-medium text-muted-foreground">Department</Label>
                                <div className="rounded-md border border-border bg-muted/50 px-3 py-2 text-sm text-muted-foreground">
                                    {user.department || "—"}
                                </div>
                            </div>
                            <div className="space-y-1.5">
                                <Label className="text-sm font-medium text-muted-foreground">Role</Label>
                                <div className="rounded-md border border-border bg-muted/50 px-3 py-2 text-sm capitalize text-muted-foreground">
                                    {user.role.replace(/_/g, " ")}
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Security */}
                    <div className="rounded-lg border border-border bg-card p-6 shadow-sm">
                        <h3 className="mb-4 text-lg font-medium text-card-foreground">
                            Security
                        </h3>
                        <div className="flex items-center justify-between rounded-lg border border-border bg-muted/30 px-4 py-3">
                            <div className="flex items-center gap-3">
                                <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
                                    <Lock className="h-4 w-4" />
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-foreground">Password</p>
                                    <p className="text-xs text-muted-foreground">Update your account password</p>
                                </div>
                            </div>
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => navigate("/change-password")}
                            >
                                <KeyRound className="mr-1.5 h-3.5 w-3.5" />
                                Change Password
                            </Button>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    );
}
