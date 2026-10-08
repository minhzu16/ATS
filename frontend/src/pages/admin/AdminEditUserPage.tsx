import { useState, useEffect } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { ArrowLeft, Lock, Unlock, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { userService } from "@/services/userService";
import { adminService } from "@/services/adminService";
import type { UserRecord, UserRole, UpdateUserPayload } from "@/types/user";
import type { Department } from "@/types/admin";
import axios from "axios";

const ROLES: UserRole[] = ["SYSTEM_ADMIN", "HR", "HR_MANAGER", "INTERVIEWER"];
const ROLE_LABELS: Record<UserRole, string> = {
    SYSTEM_ADMIN: "System Admin",
    HR: "HR",
    HR_MANAGER: "HR Manager",
    INTERVIEWER: "Interviewer",
};

interface FormErrors {
    fullName?: string;
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
        return msg || "An error occurred.";
    }
    return "An unexpected error occurred.";
}

export default function AdminEditUserPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [user, setUser] = useState<UserRecord | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [fullName, setFullName] = useState("");
    const [role, setRole] = useState<UserRole>("HR");
    const [departmentId, setDepartmentId] = useState("");
    const [departments, setDepartments] = useState<Department[]>([]);
    const [fieldErrors, setFieldErrors] = useState<FormErrors>({});
    const [apiError, setApiError] = useState("");
    const [isSaving, setIsSaving] = useState(false);
    const [isLocking, setIsLocking] = useState(false);
    const [saveSuccess, setSaveSuccess] = useState(false);

    useEffect(() => {
        if (!id) return;
        userService.getUserById(id)
            .then((u) => {
                setUser(u);
                setFullName(u.fullName);
                setRole(u.role);
                if (u.departmentId) setDepartmentId(u.departmentId);
            })
            .catch(() => setLoadError("Failed to load user details."))
            .finally(() => setIsLoading(false));

        adminService.getDepartments(0, 100)
            .then((data) => setDepartments(data.content))
            .catch(() => {});
    }, [id]);

    const validate = (): FormErrors => {
        const errors: FormErrors = {};
        if (!fullName.trim()) errors.fullName = "Full name is required.";
        else if (fullName.length > 100) errors.fullName = "Full name must not exceed 100 characters.";
        if (!role) errors.role = "Role is required.";
        if (role !== "SYSTEM_ADMIN" && !departmentId) {
            errors.departmentId = "Department is required for this role.";
        }
        return errors;
    };

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        setApiError("");
        setSaveSuccess(false);
        const errors = validate();
        if (Object.keys(errors).length > 0) {
            setFieldErrors(errors);
            return;
        }
        setFieldErrors({});
        setIsSaving(true);

        try {
            const payload: UpdateUserPayload = { 
                fullName, 
                role,
                ...(role !== "SYSTEM_ADMIN" && departmentId ? { departmentId } : {})
             };
            const updated = await userService.updateUser(id!, payload);
            setUser(updated);
            setSaveSuccess(true);
            setTimeout(() => setSaveSuccess(false), 3000);
        } catch (err) {
            setApiError(resolveApiError(err));
        } finally {
            setIsSaving(false);
        }
    };

    const handleLockToggle = async () => {
        if (!user) return;
        setIsLocking(true);
        try {
            const updated = user.accountLocked
                ? await userService.unlockUser(user.id)
                : await userService.lockUser(user.id);
            setUser(updated);
        } catch (err) {
            setApiError(resolveApiError(err));
        } finally {
            setIsLocking(false);
        }
    };

    if (isLoading) {
        return (
            <div className="flex items-center justify-center py-20 text-muted-foreground">
                <RefreshCw className="mr-2 h-5 w-5 animate-spin" />
                Loading…
            </div>
        );
    }

    if (loadError || !user) {
        return (
            <div className="flex flex-col items-center gap-4 py-20 text-center">
                <p className="text-destructive">{loadError || "User not found."}</p>
                <Button variant="outline" onClick={() => navigate("/admin/users")}>
                    <ArrowLeft className="mr-1.5 h-4 w-4" />
                    Back to Users
                </Button>
            </div>
        );
    }

    const statusLabel = !user.active ? "Inactive" : user.accountLocked ? "Locked" : "Active";
    const statusColor = !user.active
        ? "text-gray-500"
        : user.accountLocked
            ? "text-destructive"
            : "text-green-600 dark:text-green-400";

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
                    <h1 className="text-xl font-semibold text-foreground">Edit User</h1>
                    <p className="text-sm text-muted-foreground">{user.email}</p>
                </div>
            </div>

            {/* Status Banner */}
            <div className="flex items-center justify-between rounded-lg border border-border bg-card px-4 py-3">
                <div className="space-y-0.5">
                    <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Account Status</p>
                    <p className={`text-sm font-semibold ${statusColor}`}>{statusLabel}</p>
                </div>
                {user.active && (
                    <Button
                        variant={user.accountLocked ? "outline" : "secondary"}
                        size="sm"
                        onClick={handleLockToggle}
                        disabled={isLocking}
                    >
                        {isLocking ? (
                            <RefreshCw className="mr-1.5 h-4 w-4 animate-spin" />
                        ) : user.accountLocked ? (
                            <Unlock className="mr-1.5 h-4 w-4" />
                        ) : (
                            <Lock className="mr-1.5 h-4 w-4" />
                        )}
                        {user.accountLocked ? "Unlock Account" : "Lock Account"}
                    </Button>
                )}
            </div>

            {/* Edit Form */}
            <div className="rounded-xl border border-border bg-card p-6 shadow-sm">
                <h2 className="mb-4 text-sm font-semibold uppercase tracking-wider text-muted-foreground">
                    Profile Details
                </h2>
                <form onSubmit={handleSave} className="space-y-4" noValidate>
                    <div className="space-y-1.5">
                        <Label htmlFor="full-name">Full Name</Label>
                        <Input
                            id="full-name"
                            className={fieldErrors.fullName ? "border-destructive" : ""}
                            value={fullName}
                            onChange={(e) => {
                                setFullName(e.target.value);
                                setFieldErrors((p) => ({ ...p, fullName: undefined }));
                            }}
                            disabled={isSaving}
                        />
                        {fieldErrors.fullName && <p className="text-xs text-destructive">{fieldErrors.fullName}</p>}
                    </div>

                    {/* Email (read-only) */}
                    <div className="space-y-1.5">
                        <Label>Email</Label>
                        <div className="rounded-md border border-border bg-muted/50 px-3 py-2 text-sm text-muted-foreground">
                            {user.email}
                        </div>
                        <p className="text-xs text-muted-foreground">Email cannot be changed.</p>
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
                                disabled={isSaving || departments.length === 0}
                            >
                                <option value="">Select a department</option>
                                {departments.map((d) => (
                                    <option key={d.id} value={d.id}>{d.name}</option>
                                ))}
                            </select>
                            {fieldErrors.departmentId && <p className="text-xs text-destructive">{fieldErrors.departmentId}</p>}
                        </div>
                    )}

                    {/* Role */}
                    <div className="space-y-1.5">
                        <Label htmlFor="role">Role</Label>
                        <select
                            id="role"
                            className={`h-10 w-full rounded-md border bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring ${fieldErrors.role ? "border-destructive" : "border-input"}`}
                            value={role}
                            onChange={(e) => {
                                setRole(e.target.value as UserRole);
                                setFieldErrors((p) => ({ ...p, role: undefined }));
                            }}
                            disabled={isSaving}
                        >
                            {ROLES.map((r) => (
                                <option key={r} value={r}>{ROLE_LABELS[r]}</option>
                            ))}
                        </select>
                        {fieldErrors.role && <p className="text-xs text-destructive">{fieldErrors.role}</p>}
                    </div>

                    {/* API Error */}
                    {apiError && (
                        <div className="flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3">
                            <span className="mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">!</span>
                            <p className="text-sm text-destructive">{apiError}</p>
                        </div>
                    )}

                    {/* Success */}
                    {saveSuccess && (
                        <div className="rounded-lg border border-green-300 bg-green-50 px-4 py-3 text-sm text-green-700 dark:bg-green-950/30 dark:text-green-400">
                            User updated successfully.
                        </div>
                    )}

                    <div className="flex gap-3 pt-1">
                        <Button type="submit" disabled={isSaving}>
                            {isSaving ? (
                                <>
                                    <span className="mr-2 inline-block h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                                    Saving…
                                </>
                            ) : "Save Changes"}
                        </Button>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => navigate("/admin/users")}
                            disabled={isSaving}
                        >
                            Cancel
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}
