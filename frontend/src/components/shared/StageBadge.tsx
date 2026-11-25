import React from "react";
import { cn } from "@/lib/utils";
import {
  STAGE_CONFIG,
  stageLabel,
  type StageKey,
} from "./stage-badge.config";

export type { StageKey };

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
