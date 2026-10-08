import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";

interface StatCardProps {
    title: string;
    value: string | number;
    icon: LucideIcon;
    iconBgClass?: string;
    iconColorClass?: string;
    description?: string;
}

export default function StatCard({
    title,
    value,
    icon: Icon,
    iconBgClass = "bg-primary/5",
    iconColorClass = "text-primary",
    description,
}: StatCardProps) {
    return (
        <div className="group relative overflow-hidden rounded-xl border border-border bg-card p-5 transition-all duration-200 hover:border-border/80 hover:shadow-xs">
            <div className="flex items-start justify-between gap-4">
                <div className="space-y-1">
                    <p className="text-xs font-medium text-muted-foreground">
                        {title}
                    </p>
                    <p className="text-2xl font-bold tracking-tight text-foreground font-mono-numbers">
                        {value}
                    </p>
                    {description && (
                        <p className="text-xs text-muted-foreground/80">
                            {description}
                        </p>
                    )}
                </div>
                <div
                    className={cn(
                        "flex h-9 w-9 shrink-0 items-center justify-center rounded-lg ring-1 ring-border/50",
                        iconBgClass,
                    )}
                >
                    <Icon className={cn("h-4.5 w-4.5", iconColorClass)} />
                </div>
            </div>
        </div>
    );
}
