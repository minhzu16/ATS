import { Navigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";

/** Default landing after login: admins go to admin dashboard, others to recruiter dashboard. */
export default function HomeRedirect() {
    const { user } = useAuth();

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (user.role === "SYSTEM_ADMIN") {
        return <Navigate to="/admin/dashboard" replace />;
    }

    return <Navigate to="/dashboard" replace />;
}
