import { useState, useEffect } from "react";
import { NavLink } from "react-router-dom";
import { adminService } from "@/services/adminService";
import {
    Briefcase,
    Calendar,
    LayoutDashboard,
    Users,
    ShieldCheck,
    Building2,
    Settings,
    Activity,
    Bell,
    ClipboardList,
    FileText,
    ClipboardCheck,
    ListChecks,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useAuth } from "@/hooks/useAuth";

const navItems = [
    { to: "/dashboard", icon: LayoutDashboard, label: "Dashboard" },
    { to: "/jobs", icon: Briefcase, label: "Jobs" },
    { to: "/interviews", icon: Calendar, label: "Interviews" },
];

const hrNavItems = [
    { to: "/candidates", icon: Users, label: "Candidates" },
    { to: "/scorecard-templates", icon: ClipboardList, label: "Scorecard Templates" },
    { to: "/offers", icon: FileText, label: "Offers" },
    { to: "/onboarding-list", icon: ClipboardCheck, label: "Onboarding" },
];

const adminNavItems = [
    { to: "/admin/dashboard", icon: LayoutDashboard, label: "Admin Dashboard" },
    { to: "/admin/users", icon: ShieldCheck, label: "Manage Users" },
    { to: "/admin/departments", icon: Building2, label: "Departments" },
    { to: "/admin/system-config", icon: Settings, label: "System Config" },
    { to: "/admin/audit-logs", icon: Activity, label: "Audit Logs" },
    { to: "/admin/notifications", icon: Bell, label: "Notifications" },
];


export default function Sidebar() {
    const { user } = useAuth();
    const isAdmin = user?.role === "SYSTEM_ADMIN";
    const [appName, setAppName] = useState("Enterprise ATS");

    useEffect(() => {
        adminService.getConfigs(0, 100)
            .then(data => {
                const nameConfig = data.content.find((c: any) => c.key === "APP_NAME" || c.configKey === "APP_NAME");
                if (nameConfig?.value) {
                    setAppName(nameConfig.value);
                }
            })
            .catch(() => {});
    }, []);
    const isHr = user?.role === "HR" || user?.role === "HR_MANAGER";
    const isHrManager = user?.role === "HR_MANAGER";

    const visibleNavItems = navItems.filter((item) => {
        if (item.to === "/interviews" && user?.role !== "INTERVIEWER") return false;
        if (
            user?.role === "SYSTEM_ADMIN" &&
            (item.to === "/dashboard" || item.to === "/jobs")
        ) {
            return false;
        }
        return true;
    });

    return (
        <aside className="flex h-screen w-64 flex-col border-r border-border bg-sidebar">
            {/* Logo */}
            <div className="flex h-16 shrink-0 items-center gap-2 border-b border-sidebar-border px-6">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-primary-foreground">
                    <Briefcase className="h-4 w-4" />
                </div>
                <span className="text-lg font-bold text-sidebar-foreground whitespace-nowrap overflow-hidden text-ellipsis">
                    {appName}
                </span>
            </div>

            {/* Navigation */}
            <nav className="flex-1 overflow-y-auto px-3 py-4">
                <ul className="space-y-1">
                    {visibleNavItems.map(({ to, icon: Icon, label }) => (
                        <li key={to}>
                            <NavLink
                                to={to}
                                end={to === "/jobs"}
                                className={({ isActive }) =>
                                    cn(
                                        "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                                        isActive
                                            ? "bg-sidebar-primary text-sidebar-primary-foreground"
                                            : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                                    )
                                }
                            >
                                <Icon className="h-4 w-4 shrink-0" />
                                {label}
                            </NavLink>
                        </li>
                    ))}

                    {isHrManager && (
                        <li>
                            <NavLink
                                to="/jobs/pending-approval"
                                className={({ isActive }) =>
                                    cn(
                                        "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                                        isActive
                                            ? "bg-sidebar-primary text-sidebar-primary-foreground"
                                            : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                                    )
                                }
                            >
                                <ListChecks className="h-4 w-4 shrink-0" />
                                Pending approvals
                            </NavLink>
                        </li>
                    )}

                    {isHr && (
                        <>
                            {hrNavItems.map(({ to, icon: Icon, label }) => (
                                <li key={to}>
                                    <NavLink
                                        to={to}
                                        className={({ isActive }) =>
                                            cn(
                                                "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                                                isActive
                                                    ? "bg-sidebar-primary text-sidebar-primary-foreground"
                                                    : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                                            )
                                        }
                                    >
                                        <Icon className="h-4 w-4 shrink-0" />
                                        {label}
                                    </NavLink>
                                </li>
                            ))}
                        </>
                    )}

                    {/* Admin section */}
                    {isAdmin && (
                        <>
                            <li className="pt-3">
                                <p className="mb-1 px-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                                    Administration
                                </p>
                            </li>
                            {adminNavItems.map(({ to, icon: Icon, label }) => (
                                <li key={to}>
                                    <NavLink
                                        to={to}
                                        className={({ isActive }) =>
                                            cn(
                                                "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                                                isActive
                                                    ? "bg-sidebar-primary text-sidebar-primary-foreground"
                                                    : "text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                                            )
                                        }
                                    >
                                        <Icon className="h-4 w-4 shrink-0" />
                                        {label}
                                    </NavLink>
                                </li>
                            ))}
                        </>
                    )}
                </ul>
            </nav>

            {/* Footer */}
            <div className="shrink-0 border-t border-sidebar-border p-4">
                <p className="text-center text-xs text-muted-foreground">
                    © 2025 Enterprise ATS
                </p>
            </div>
        </aside>
    );
}
