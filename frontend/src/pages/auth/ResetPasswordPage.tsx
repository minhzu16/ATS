import { useState, useEffect } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { Eye, EyeOff, CheckCircle2, XCircle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authService } from "@/services/authService";
import axios from "axios";

function resolveError(err: unknown): string {
    if (axios.isAxiosError(err)) {
        const msg: string | undefined = err.response?.data?.message;
        if (err.response?.status === 400) {
            const details: string[] | undefined = err.response?.data?.details;
            return details?.join(", ") || msg || "Invalid input.";
        }
        return msg || "An error occurred.";
    }
    return "An unexpected error occurred.";
}

export default function ResetPasswordPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const token = searchParams.get("token") ?? "";

    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [showPw, setShowPw] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);
    const [tokenMissing, setTokenMissing] = useState(false);

    useEffect(() => {
        if (!token) setTokenMissing(true);
    }, [token]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");

        if (password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }
        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setIsLoading(true);
        try {
            await authService.resetPassword(token, password, confirmPassword);
            setSuccess(true);
        } catch (err) {
            setError(resolveError(err));
        } finally {
            setIsLoading(false);
        }
    };

    if (tokenMissing) {
        return (
            <div className="flex flex-col items-center gap-4 text-center">
                <XCircle className="h-12 w-12 text-destructive" />
                <h2 className="text-lg font-semibold text-foreground">Invalid Link</h2>
                <p className="text-sm text-muted-foreground">
                    This reset link is missing or invalid. Please request a new one.
                </p>
                <Link to="/forgot-password" className="text-sm text-primary hover:underline">
                    Request new reset link
                </Link>
            </div>
        );
    }

    if (success) {
        return (
            <div className="flex flex-col items-center gap-4 text-center">
                <CheckCircle2 className="h-12 w-12 text-green-500" />
                <h2 className="text-xl font-semibold text-foreground">Password Reset!</h2>
                <p className="text-sm text-muted-foreground">
                    Your password has been reset successfully. You can now log in.
                </p>
                <Button onClick={() => navigate("/login")} className="mt-2">
                    Go to Login
                </Button>
            </div>
        );
    }

    return (
        <div className="w-full max-w-sm space-y-6">
            <div className="space-y-1 text-center">
                <h1 className="text-2xl font-bold tracking-tight text-foreground">Reset Password</h1>
                <p className="text-sm text-muted-foreground">Choose a new password for your account.</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
                {/* Password */}
                <div className="space-y-1.5">
                    <Label htmlFor="password">New Password</Label>
                    <div className="relative">
                        <Input
                            id="password"
                            type={showPw ? "text" : "password"}
                            placeholder="At least 8 characters"
                            className="pr-10"
                            value={password}
                            onChange={(e) => {
                                setPassword(e.target.value);
                                setError("");
                            }}
                            disabled={isLoading}
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                            onClick={() => setShowPw((v) => !v)}
                            tabIndex={-1}
                        >
                            {showPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                        </button>
                    </div>
                </div>

                {/* Confirm Password */}
                <div className="space-y-1.5">
                    <Label htmlFor="confirm-password">Confirm Password</Label>
                    <div className="relative">
                        <Input
                            id="confirm-password"
                            type={showConfirm ? "text" : "password"}
                            placeholder="Repeat new password"
                            className="pr-10"
                            value={confirmPassword}
                            onChange={(e) => {
                                setConfirmPassword(e.target.value);
                                setError("");
                            }}
                            disabled={isLoading}
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                            onClick={() => setShowConfirm((v) => !v)}
                            tabIndex={-1}
                        >
                            {showConfirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                        </button>
                    </div>
                </div>

                {/* Error */}
                {error && (
                    <div className="flex items-center gap-2 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                        <XCircle className="h-4 w-4 shrink-0" />
                        {error}
                    </div>
                )}

                <Button type="submit" className="w-full" disabled={isLoading}>
                    {isLoading ? (
                        <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            Resetting…
                        </>
                    ) : "Reset Password"}
                </Button>

                <p className="text-center text-sm text-muted-foreground">
                    <Link to="/login" className="text-primary hover:underline">Back to login</Link>
                </p>
            </form>
        </div>
    );
}
