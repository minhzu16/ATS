import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Lock, Eye, EyeOff, ArrowLeft, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/hooks/useAuth";
import { userService } from "@/services/userService";
import axios from "axios";

interface FormErrors {
    currentPassword?: string;
    newPassword?: string;
    confirmPassword?: string;
}

function resolveApiError(err: unknown): string {
    if (axios.isAxiosError(err)) {
        const msg: string | undefined = err.response?.data?.message;
        if (err.response?.status === 400) {
            const details: string[] | undefined = err.response?.data?.details;
            return details?.join(", ") || msg || "Invalid input.";
        }
        return msg || "An error occurred. Please try again.";
    }
    return "An unexpected error occurred.";
}

export default function ChangePasswordPage() {
    const { user } = useAuth();
    const navigate = useNavigate();

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [showCurrent, setShowCurrent] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [fieldErrors, setFieldErrors] = useState<FormErrors>({});
    const [apiError, setApiError] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    if (!user) return null;

    const validate = (): FormErrors => {
        const errors: FormErrors = {};
        if (!currentPassword) errors.currentPassword = "Current password is required.";
        if (!newPassword) {
            errors.newPassword = "New password is required.";
        } else if (newPassword.length < 8) {
            errors.newPassword = "Password must be at least 8 characters.";
        }
        if (!confirmPassword) {
            errors.confirmPassword = "Please confirm your new password.";
        } else if (newPassword !== confirmPassword) {
            errors.confirmPassword = "Passwords do not match.";
        }
        return errors;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setApiError("");

        const errors = validate();
        if (Object.keys(errors).length > 0) {
            setFieldErrors(errors);
            return;
        }
        setFieldErrors({});
        setIsLoading(true);

        try {
            await userService.changePassword({ currentPassword, newPassword, confirmPassword });
            setSuccess(true);
        } catch (err) {
            setApiError(resolveApiError(err));
        } finally {
            setIsLoading(false);
        }
    };

    if (success) {
        return (
            <div className="mx-auto max-w-lg rounded-xl border border-border bg-card p-8 shadow-sm">
                <div className="flex flex-col items-center gap-4 text-center">
                    <CheckCircle2 className="h-16 w-16 text-green-500" />
                    <h2 className="text-xl font-semibold text-foreground">Password Changed!</h2>
                    <p className="text-sm text-muted-foreground">
                        Your password has been changed successfully.
                    </p>
                    <Button onClick={() => navigate("/profile")} className="w-full">
                        Back to Profile
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-lg space-y-6">
            {/* Header */}
            <div className="flex items-center gap-3">
                <Link
                    to="/profile"
                    className="flex h-8 w-8 items-center justify-center rounded-md border border-border text-muted-foreground hover:bg-muted"
                >
                    <ArrowLeft className="h-4 w-4" />
                </Link>
                <div>
                    <h1 className="text-xl font-semibold text-foreground">Change Password</h1>
                    <p className="text-sm text-muted-foreground">Update your account password</p>
                </div>
            </div>

            {/* Form */}
            <div className="rounded-xl border border-border bg-card p-6 shadow-sm">
                <form onSubmit={handleSubmit} className="space-y-5" noValidate>

                    {/* Current Password */}
                    <div className="space-y-1.5">
                        <Label htmlFor="current-password">Current Password</Label>
                        <div className="relative">
                            <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                            <Input
                                id="current-password"
                                type={showCurrent ? "text" : "password"}
                                placeholder="Enter your current password"
                                className={`pl-9 pr-9 ${fieldErrors.currentPassword ? "border-destructive" : ""}`}
                                value={currentPassword}
                                onChange={(e) => {
                                    setCurrentPassword(e.target.value);
                                    setFieldErrors((p) => ({ ...p, currentPassword: undefined }));
                                }}
                                disabled={isLoading}
                            />
                            <button
                                type="button"
                                onClick={() => setShowCurrent((v) => !v)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                                tabIndex={-1}
                            >
                                {showCurrent ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                            </button>
                        </div>
                        {fieldErrors.currentPassword && (
                            <p className="text-xs text-destructive">{fieldErrors.currentPassword}</p>
                        )}
                    </div>

                    <div className="border-t border-border" />

                    {/* New Password */}
                    <div className="space-y-1.5">
                        <Label htmlFor="new-password">New Password</Label>
                        <div className="relative">
                            <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                            <Input
                                id="new-password"
                                type={showNew ? "text" : "password"}
                                placeholder="At least 8 characters"
                                className={`pl-9 pr-9 ${fieldErrors.newPassword ? "border-destructive" : ""}`}
                                value={newPassword}
                                onChange={(e) => {
                                    setNewPassword(e.target.value);
                                    setFieldErrors((p) => ({ ...p, newPassword: undefined }));
                                }}
                                disabled={isLoading}
                            />
                            <button
                                type="button"
                                onClick={() => setShowNew((v) => !v)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                                tabIndex={-1}
                            >
                                {showNew ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                            </button>
                        </div>
                        {fieldErrors.newPassword && (
                            <p className="text-xs text-destructive">{fieldErrors.newPassword}</p>
                        )}
                        {!fieldErrors.newPassword && (
                            <p className="text-xs text-muted-foreground">Must be at least 8 characters and different from current password.</p>
                        )}
                    </div>

                    {/* Confirm Password */}
                    <div className="space-y-1.5">
                        <Label htmlFor="confirm-password">Confirm New Password</Label>
                        <div className="relative">
                            <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                            <Input
                                id="confirm-password"
                                type={showConfirm ? "text" : "password"}
                                placeholder="Repeat new password"
                                className={`pl-9 pr-9 ${fieldErrors.confirmPassword ? "border-destructive" : ""}`}
                                value={confirmPassword}
                                onChange={(e) => {
                                    setConfirmPassword(e.target.value);
                                    setFieldErrors((p) => ({ ...p, confirmPassword: undefined }));
                                }}
                                disabled={isLoading}
                            />
                            <button
                                type="button"
                                onClick={() => setShowConfirm((v) => !v)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                                tabIndex={-1}
                            >
                                {showConfirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                            </button>
                        </div>
                        {fieldErrors.confirmPassword && (
                            <p className="text-xs text-destructive">{fieldErrors.confirmPassword}</p>
                        )}
                    </div>

                    {/* API Error */}
                    {apiError && (
                        <div className="flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3">
                            <span className="mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">!</span>
                            <p className="text-sm text-destructive">{apiError}</p>
                        </div>
                    )}

                    <div className="flex gap-3 pt-1">
                        <Button type="submit" disabled={isLoading}>
                            {isLoading ? (
                                <>
                                    <span className="mr-2 inline-block h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                                    Saving…
                                </>
                            ) : "Update Password"}
                        </Button>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => navigate("/profile")}
                            disabled={isLoading}
                        >
                            Cancel
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}
