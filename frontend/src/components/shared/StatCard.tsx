import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";

interface StatCardProps {
    title: string;
    value: string | number;
    icon: LucideIcon;
    iconBgClass: string;
    iconColorClass: string;
}

export default function StatCard({
    title,
    value,
    icon: Icon,
    iconBgClass,
    iconColorClass,
}: StatCardProps) {
    return (
        <div className="rounded-lg border border-border bg-card p-6 shadow-sm">
            <div className="flex items-center justify-between">
                <div>
                    <p className="text-sm font-medium text-muted-foreground">{title}</p>
                    <p className="mt-1 text-3xl font-bold text-card-foreground">
                        {value}
                    </p>
                </div>
                <div
                    className={cn(
                        "flex h-12 w-12 items-center justify-center rounded-full",
                        iconBgClass,
                    )}
                >
                    <Icon className={cn("h-5 w-5", iconColorClass)} />
                </div>
            </div>
        </div>
    );
}
