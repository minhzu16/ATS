import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";

export default function ProtectedRoute() {
    const { user, isLoading } = useAuth();

    // While loading, render nothing (prevents flash redirect on page refresh)
    if (isLoading) return null;

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
}
