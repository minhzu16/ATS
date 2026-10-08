import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Mail, CheckCircle2, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authService } from "@/services/authService";
import axios from "axios";

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function resolveError(err: unknown): string {
    if (axios.isAxiosError(err)) {
        const msg: string | undefined = err.response?.data?.message;
        const details: string[] | undefined = err.response?.data?.details;
        return details?.join(", ") || msg || "An error occurred.";
    }
    return "An unexpected error occurred.";
}

export default function ForgotPasswordPage() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [emailError, setEmailError] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [apiError, setApiError] = useState("");
    const [submitted, setSubmitted] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setApiError("");

        if (!email.trim()) {
            setEmailError("Email is required.");
            return;
        }
        if (!EMAIL_REGEX.test(email)) {
            setEmailError("Please enter a valid email address.");
            return;
        }
        setEmailError("");
        setIsLoading(true);

        try {
            await authService.forgotPassword(email.trim());
            setSubmitted(true);
        } catch (err) {
            setApiError(resolveError(err));
        } finally {
            setIsLoading(false);
        }
    };

    if (submitted) {
        return (
            <div className="w-full max-w-sm space-y-6 text-center">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/30">
                    <CheckCircle2 className="h-8 w-8 text-green-600 dark:text-green-400" />
                </div>
                <div className="space-y-2">
                    <h2 className="text-xl font-semibold text-foreground">Check Your Email</h2>
                    <p className="text-sm text-muted-foreground">
                        If an account with <strong>{email}</strong> exists, we've sent a password reset link.
                        Please check your inbox (and spam folder).
                    </p>
                    <p className="text-xs text-muted-foreground">The link expires in 15 minutes.</p>
                </div>
                <div className="space-y-2">
                    <Button variant="outline" className="w-full" onClick={() => setSubmitted(false)}>
                        Didn't receive it? Try again
                    </Button>
                    <Button variant="ghost" className="w-full" onClick={() => navigate("/login")}>
                        Back to Login
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <div className="w-full max-w-sm space-y-6">
            {/* Header */}
            <div className="space-y-1 text-center">
                <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
                    <Mail className="h-6 w-6" />
                </div>
                <h1 className="text-2xl font-bold tracking-tight text-foreground">Forgot Password?</h1>
                <p className="text-sm text-muted-foreground">
                    Enter your email and we'll send you a link to reset your password.
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
                <div className="space-y-1.5">
                    <Label htmlFor="email">Email Address</Label>
                    <Input
                        id="email"
                        type="email"
                        placeholder="you@company.com"
                        className={emailError ? "border-destructive" : ""}
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            setEmailError("");
                            setApiError("");
                        }}
                        disabled={isLoading}
                        autoFocus
                    />
                    {emailError && <p className="text-xs text-destructive">{emailError}</p>}
                </div>

                {apiError && (
                    <div className="flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                        <span className="mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">!</span>
                        {apiError}
                    </div>
                )}

                <Button type="submit" className="w-full" disabled={isLoading}>
                    {isLoading ? (
                        <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            Sending…
                        </>
                    ) : "Send Reset Link"}
                </Button>

                <Link
                    to="/login"
                    className="flex items-center justify-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
                >
                    <ArrowLeft className="h-3.5 w-3.5" />
                    Back to login
                </Link>
            </form>
        </div>
    );
}
