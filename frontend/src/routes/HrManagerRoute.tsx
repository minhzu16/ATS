import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";

export default function HrManagerRoute() {
    const { user } = useAuth();

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (user.role !== "HR_MANAGER") {
        return <Navigate to="/dashboard" replace />;
    }

    return <Outlet />;
}
