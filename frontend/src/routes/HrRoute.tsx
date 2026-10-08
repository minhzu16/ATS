import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";

export default function HrRoute() {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Allow both HR and HR_MANAGER to access HR pages
  if (user.role !== "HR" && user.role !== "HR_MANAGER") {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
