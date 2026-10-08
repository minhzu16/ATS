import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";

/** Recruiter routes restricted to HR only (excludes HR_MANAGER, interviewer, admin). */
export default function HrOnlyRoute() {
    const { user } = useAuth();

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (user.role !== "HR") {
        return <Navigate to="/dashboard" replace />;
    }

    return <Outlet />;
}
