import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Briefcase, Lock, Mail, Eye, EyeOff } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/hooks/useAuth";
import axios from "axios";

// ── Validation ───────────────────────────────────────────────────────────────
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface FormErrors {
    email?: string;
    password?: string;
}

function validateForm(email: string, password: string): FormErrors {
    const errors: FormErrors = {};
    if (!email.trim()) {
        errors.email = "Email is required.";
    } else if (!EMAIL_REGEX.test(email)) {
        errors.email = "Please enter a valid email address.";
    }
    if (!password) {
        errors.password = "Password is required.";
    } else if (password.length < 6) {
        errors.password = "Password must be at least 6 characters.";
    }
    return errors;
}

function resolveApiError(err: unknown): string {
    if (axios.isAxiosError(err)) {
        const status = err.response?.status;
        const message: string | undefined = err.response?.data?.message;
        if (status === 400) {
            const details: string[] | undefined = err.response?.data?.details;
            return details?.join(", ") || "Invalid request. Please check your input.";
        }
        if (status === 401) {
            return message || "Invalid email or password.";
        }
        if (status === 403) {
            return message || "Your account has been locked. Please contact your administrator.";
        }
        if (status === 0 || !err.response) {
            return "Cannot connect to the server. Please try again later.";
        }
    }
    return "An unexpected error occurred. Please try again.";
}

export default function LoginPage() {
    const { login } = useAuth();
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [rememberMe, setRememberMe] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [apiError, setApiError] = useState<string | null>(null);
    const [fieldErrors, setFieldErrors] = useState<FormErrors>({});

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setApiError(null);

        const errors = validateForm(email, password);
        if (Object.keys(errors).length > 0) {
            setFieldErrors(errors);
            return;
        }
        setFieldErrors({});
        setIsLoading(true);

        try {
            const userData = await login({ email, password }, rememberMe);
            navigate(
                userData.role === "SYSTEM_ADMIN" ? "/admin/dashboard" : "/dashboard",
            );
        } catch (err) {
            setApiError(resolveApiError(err));
        } finally {
            setIsLoading(false);
        }
    };

    const clearFieldError = (field: keyof FormErrors) => {
        if (fieldErrors[field]) {
            setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
        }
    };

    return (
        <div className="w-full max-w-md space-y-8 rounded-xl border border-border/50 bg-card p-8 shadow-xl">
            {/* Logo & Title */}
            <div className="text-center">
                <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-primary text-primary-foreground shadow-lg">
                    <Briefcase className="h-7 w-7" />
                </div>
                <h1 className="mt-6 text-2xl font-bold tracking-tight text-foreground">
                    Welcome back
                </h1>
                <p className="mt-2 text-sm text-muted-foreground">
                    Sign in to access your ATS workspace
                </p>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-5" noValidate>
                <div className="space-y-4">
                    {/* Email */}
                    <div className="space-y-1.5">
                        <Label htmlFor="email">Email address</Label>
                        <div className="relative">
                            <Mail className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                            <Input
                                id="email"
                                name="email"
                                type="email"
                                autoComplete="email"
                                placeholder="you@company.com"
                                className={`pl-9 ${fieldErrors.email ? "border-destructive focus-visible:ring-destructive" : ""}`}
                                value={email}
                                onChange={(e) => {
                                    setEmail(e.target.value);
                                    clearFieldError("email");
                                }}
                                disabled={isLoading}
                                aria-describedby={fieldErrors.email ? "email-error" : undefined}
                            />
                        </div>
                        {fieldErrors.email && (
                            <p id="email-error" className="text-xs text-destructive">
                                {fieldErrors.email}
                            </p>
                        )}
                    </div>

                    {/* Password */}
                    <div className="space-y-1.5">
                        <div className="flex items-center justify-between">
                            <Label htmlFor="password">Password</Label>
                            <Link
                                to="/forgot-password"
                                className="text-xs text-primary hover:underline"
                            >
                                Forgot password?
                            </Link>
                        </div>
                        <div className="relative">
                            <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                            <Input
                                id="password"
                                name="password"
                                type={showPassword ? "text" : "password"}
                                autoComplete="current-password"
                                placeholder="••••••••"
                                className={`pl-9 pr-9 ${fieldErrors.password ? "border-destructive focus-visible:ring-destructive" : ""}`}
                                value={password}
                                onChange={(e) => {
                                    setPassword(e.target.value);
                                    clearFieldError("password");
                                }}
                                disabled={isLoading}
                                aria-describedby={fieldErrors.password ? "password-error" : undefined}
                            />
                            <button
                                type="button"
                                onClick={() => setShowPassword((v) => !v)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                                tabIndex={-1}
                                aria-label={showPassword ? "Hide password" : "Show password"}
                            >
                                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                            </button>
                        </div>
                        {fieldErrors.password && (
                            <p id="password-error" className="text-xs text-destructive">
                                {fieldErrors.password}
                            </p>
                        )}
                    </div>
                </div>

                {/* Remember Me */}
                <div className="flex items-center gap-2">
                    <input
                        id="remember-me"
                        type="checkbox"
                        checked={rememberMe}
                        onChange={(e) => setRememberMe(e.target.checked)}
                        className="h-4 w-4 rounded border-border accent-primary"
                    />
                    <Label htmlFor="remember-me" className="cursor-pointer text-sm font-normal">
                        Remember me
                    </Label>
                </div>

                {/* API Error */}
                {apiError && (
                    <div
                        role="alert"
                        className="flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3"
                    >
                        <span className="mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">
                            !
                        </span>
                        <p className="text-sm text-destructive">{apiError}</p>
                    </div>
                )}

                <Button
                    type="submit"
                    className="w-full"
                    disabled={isLoading}
                    size="lg"
                >
                    {isLoading ? (
                        <>
                            <span
                                className="mr-2 inline-block h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"
                                aria-hidden="true"
                            />
                            Signing in…
                        </>
                    ) : (
                        "Sign in"
                    )}
                </Button>
            </form>

            {/* Footer note */}
            <p className="text-center text-xs text-muted-foreground">
                Don&apos;t have an account? Contact your system administrator.
            </p>
        </div>
    );
}
