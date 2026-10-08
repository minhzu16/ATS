import React from "react";
import { cn } from "@/lib/utils";

export type StageKey =
  | "APPLIED"
  | "SCREENING"
  | "INTERVIEW"
  | "OFFER"
  | "HIRED"
  | "REJECTED"
  | string;

export function stageLabel(stage: string): string {
  if (!stage) return "";
  const map: Record<string, string> = {
    APPLIED: "Applied",
    SCREENING: "Screening",
    INTERVIEW: "Interview",
    OFFER: "Offer",
    HIRED: "Hired",
    REJECTED: "Rejected",
  };
  return map[stage.toUpperCase()] ?? stage.replace(/_/g, " ");
}

const STAGE_CONFIG: Record<
  string,
  { bg: string; dot: string; text: string; border: string }
> = {
  APPLIED: {
    bg: "bg-slate-50 dark:bg-slate-900/40",
    dot: "bg-slate-400",
    text: "text-slate-700 dark:text-slate-300",
    border: "border-slate-200 dark:border-slate-700",
  },
  SCREENING: {
    bg: "bg-blue-50/70 dark:bg-blue-950/30",
    dot: "bg-blue-500",
    text: "text-blue-700 dark:text-blue-300",
    border: "border-blue-200 dark:border-blue-800/60",
  },
  INTERVIEW: {
    bg: "bg-indigo-50/70 dark:bg-indigo-950/30",
    dot: "bg-indigo-500",
    text: "text-indigo-700 dark:text-indigo-300",
    border: "border-indigo-200 dark:border-indigo-800/60",
  },
  OFFER: {
    bg: "bg-amber-50/70 dark:bg-amber-950/30",
    dot: "bg-amber-500",
    text: "text-amber-800 dark:text-amber-300",
    border: "border-amber-200 dark:border-amber-800/60",
  },
  HIRED: {
    bg: "bg-emerald-50/70 dark:bg-emerald-950/30",
    dot: "bg-emerald-500",
    text: "text-emerald-800 dark:text-emerald-300",
    border: "border-emerald-200 dark:border-emerald-800/60",
  },
  REJECTED: {
    bg: "bg-rose-50/70 dark:bg-rose-950/30",
    dot: "bg-rose-500",
    text: "text-rose-800 dark:text-rose-300",
    border: "border-rose-200 dark:border-rose-800/60",
  },
};

interface StageBadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  stage: StageKey;
  showDot?: boolean;
}

export function StageBadge({
  stage,
  showDot = true,
  className,
  ...props
}: StageBadgeProps) {
  const upper = (stage || "").toUpperCase();
  const config = STAGE_CONFIG[upper] ?? {
    bg: "bg-muted/50",
    dot: "bg-muted-foreground",
    text: "text-muted-foreground",
    border: "border-border",
  };

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-md border px-2 py-0.5 text-xs font-medium tracking-tight transition-colors",
        config.bg,
        config.border,
        config.text,
        className
      )}
      {...props}
    >
      {showDot && (
        <span
          className={cn("h-1.5 w-1.5 rounded-full shrink-0", config.dot)}
          aria-hidden="true"
        />
      )}
      <span>{stageLabel(stage)}</span>
    </span>
  );
}
