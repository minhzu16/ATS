import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { ArrowLeft, UserPlus, CheckCircle2, Mail } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { userService } from "@/services/userService";
import { adminService } from "@/services/adminService";
import type { CreateUserPayload, UserRole } from "@/types/user";
import type { Department } from "@/types/admin";
import axios from "axios";

const ROLES: UserRole[] = ["SYSTEM_ADMIN", "HR", "HR_MANAGER", "INTERVIEWER"];
const ROLE_LABELS: Record<UserRole, string> = {
    SYSTEM_ADMIN: "System Admin",
    HR: "HR",
    HR_MANAGER: "HR Manager",
    INTERVIEWER: "Interviewer",
};

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface FormErrors {
    fullName?: string;
    email?: string;
    role?: string;
    departmentId?: string;
}

function resolveApiError(err: unknown): string {
    if (axios.isAxiosError(err)) {
        const msg: string | undefined = err.response?.data?.message;
        if (err.response?.status === 400) {
            const details: string[] | undefined = err.response?.data?.details;
            return details?.join(", ") || msg || "Invalid input.";
        }
        if (err.response?.status === 409) return msg || "Email already in use.";
        return msg || "An error occurred.";
    }
    return "An unexpected error occurred.";
}

export default function AdminCreateUserPage() {
    const navigate = useNavigate();

    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [role, setRole] = useState<UserRole>("HR");
    const [departmentId, setDepartmentId] = useState("");
    const [departments, setDepartments] = useState<Department[]>([]);
    const [fieldErrors, setFieldErrors] = useState<FormErrors>({});
    const [apiError, setApiError] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [createdEmail, setCreatedEmail] = useState<string | null>(null);

    useEffect(() => {
        adminService.getDepartments(0, 100)
            .then(data => setDepartments(data.content))
            .catch(() => {});
    }, []);

    const validate = (): FormErrors => {
        const errors: FormErrors = {};
        if (!fullName.trim()) errors.fullName = "Full name is required.";
        else if (fullName.length > 100) errors.fullName = "Full name must not exceed 100 characters.";
        if (!email.trim()) {
            errors.email = "Email is required.";
        } else if (!EMAIL_REGEX.test(email)) {
            errors.email = "Please enter a valid email address.";
        }
        if (!role) errors.role = "Role is required.";
        if (role !== "SYSTEM_ADMIN" && !departmentId) {
            errors.departmentId = "Department is required for this role.";
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
            const payload: CreateUserPayload = { 
                fullName, 
                email, 
                role,
                ...(role !== "SYSTEM_ADMIN" && departmentId ? { departmentId } : {})
            };
            await userService.createUser(payload);
            setCreatedEmail(email);
        } catch (err) {
            setApiError(resolveApiError(err));
        } finally {
            setIsLoading(false);
        }
    };

    // ── Success state ──────────────────────────────────────────────────────
    if (createdEmail) {
        return (
            <div className="mx-auto max-w-lg">
                <div className="rounded-xl border border-border bg-card p-8 text-center shadow-sm">
                    <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/30">
                        <CheckCircle2 className="h-7 w-7 text-green-600 dark:text-green-400" />
                    </div>
                    <h2 className="mb-2 text-xl font-semibold text-foreground">User Created</h2>
                    <p className="mb-1 text-sm text-muted-foreground">
                        An activation email has been sent to:
                    </p>
                    <div className="mx-auto mb-6 flex items-center justify-center gap-2 rounded-lg bg-muted px-4 py-2">
                        <Mail className="h-4 w-4 text-muted-foreground" />
                        <span className="text-sm font-medium text-foreground">{createdEmail}</span>
                    </div>
                    <p className="mb-6 text-xs text-muted-foreground">
                        The user must click the link in the email to activate their account and set a password.
                        The link expires in <strong>24 hours</strong>.
                    </p>
                    <div className="flex flex-col gap-2 sm:flex-row sm:justify-center">
                        <Button onClick={() => { setCreatedEmail(null); setFullName(""); setEmail(""); setRole("HR"); setDepartmentId(""); }}>
                            <UserPlus className="mr-1.5 h-4 w-4" />
                            Add Another User
                        </Button>
                        <Button variant="outline" onClick={() => navigate("/admin/users")}>
                            Back to Users
                        </Button>
                    </div>
                </div>
            </div>
        );
    }

    // ── Form ──────────────────────────────────────────────────────────────
    return (
        <div className="mx-auto max-w-lg space-y-6">
            {/* Header */}
            <div className="flex items-center gap-3">
                <Link
                    to="/admin/users"
                    className="flex h-8 w-8 items-center justify-center rounded-md border border-border text-muted-foreground hover:bg-muted"
                >
                    <ArrowLeft className="h-4 w-4" />
                </Link>
                <div>
                    <h1 className="text-xl font-semibold text-foreground">Create User</h1>
                    <p className="text-sm text-muted-foreground">An activation email will be sent to the user</p>
                </div>
            </div>

            <div className="rounded-xl border border-border bg-card p-6 shadow-sm">
                <form onSubmit={handleSubmit} className="space-y-4" noValidate>
                    {/* Full Name */}
                    <div className="space-y-1.5">
                        <Label htmlFor="full-name">Full Name <span className="text-destructive">*</span></Label>
                        <Input
                            id="full-name"
                            placeholder="John Doe"
                            className={fieldErrors.fullName ? "border-destructive" : ""}
                            value={fullName}
                            onChange={(e) => {
                                setFullName(e.target.value);
                                setFieldErrors((p) => ({ ...p, fullName: undefined }));
                            }}
                            disabled={isLoading}
                        />
                        {fieldErrors.fullName && <p className="text-xs text-destructive">{fieldErrors.fullName}</p>}
                    </div>

                    {/* Email */}
                    <div className="space-y-1.5">
                        <Label htmlFor="email">Email <span className="text-destructive">*</span></Label>
                        <Input
                            id="email"
                            type="email"
                            placeholder="john@company.com"
                            className={fieldErrors.email ? "border-destructive" : ""}
                            value={email}
                            onChange={(e) => {
                                setEmail(e.target.value);
                                setFieldErrors((p) => ({ ...p, email: undefined }));
                            }}
                            disabled={isLoading}
                        />
                        {fieldErrors.email && <p className="text-xs text-destructive">{fieldErrors.email}</p>}
                    </div>

                    {/* Role */}
                    <div className="space-y-1.5">
                        <Label htmlFor="role">Role <span className="text-destructive">*</span></Label>
                        <select
                            id="role"
                            className={`h-10 w-full rounded-md border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring ${fieldErrors.role ? "border-destructive" : "border-input"}`}
                            value={role}
                            onChange={(e) => {
                                setRole(e.target.value as UserRole);
                                setFieldErrors((p) => ({ ...p, role: undefined }));
                            }}
                            disabled={isLoading}
                        >
                            {ROLES.map((r) => (
                                <option key={r} value={r}>{ROLE_LABELS[r]}</option>
                            ))}
                        </select>
                        {fieldErrors.role && <p className="text-xs text-destructive">{fieldErrors.role}</p>}
                    </div>

                    {/* Department */}
                    {role !== "SYSTEM_ADMIN" && (
                        <div className="space-y-1.5">
                            <Label htmlFor="department">Department <span className="text-destructive">*</span></Label>
                            <select
                                id="department"
                                className={`h-10 w-full rounded-md border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring ${fieldErrors.departmentId ? "border-destructive" : "border-input"}`}
                                value={departmentId}
                                onChange={(e) => {
                                    setDepartmentId(e.target.value);
                                    setFieldErrors((p) => ({ ...p, departmentId: undefined }));
                                }}
                                disabled={isLoading || departments.length === 0}
                            >
                                <option value="">Select a department</option>
                                {departments.map((d) => (
                                    <option key={d.id} value={d.id}>{d.name}</option>
                                ))}
                            </select>
                            {fieldErrors.departmentId && <p className="text-xs text-destructive">{fieldErrors.departmentId}</p>}
                        </div>
                    )}

                    {/* Info banner */}
                    <div className="flex items-start gap-3 rounded-lg border border-blue-200 bg-blue-50 px-4 py-3 dark:border-blue-800 dark:bg-blue-950/20">
                        <Mail className="mt-0.5 h-4 w-4 shrink-0 text-blue-600 dark:text-blue-400" />
                        <p className="text-xs text-blue-700 dark:text-blue-300">
                            The user will receive an email with a link to set their password and activate their account. No password is needed during creation.
                        </p>
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
                                    Creating…
                                </>
                            ) : (
                                <>
                                    <UserPlus className="mr-1.5 h-4 w-4" />
                                    Create & Send Email
                                </>
                            )}
                        </Button>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => navigate("/admin/users")}
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
