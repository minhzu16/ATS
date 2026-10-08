import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";

/** Wraps routes that require SYSTEM_ADMIN role. Redirects to /dashboard if not admin. */
export default function AdminRoute() {
    const { user } = useAuth();

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (user.role !== "SYSTEM_ADMIN") {
        return <Navigate to="/dashboard" replace />;
    }

    return <Outlet />;
}
