import { cn } from "@/lib/utils";

const STEPS = [
    { step: 1, label: "Step 1", title: "Job Details" },
    { step: 2, label: "Step 2", title: "Requirements" },
    { step: 3, label: "Step 3", title: "Workflow" },
    { step: 4, label: "Step 4", title: "Review" },
] as const;

interface JobFormStepperProps {
    currentStep: number;
}

export function JobFormStepper({ currentStep }: JobFormStepperProps) {
    return (
        <nav aria-label="Progress">
            <ol className="mb-8 flex flex-col space-y-4 md:flex-row md:space-x-8 md:space-y-0">
                {STEPS.map((s) => {
                    const active = s.step === currentStep;
                    const done = s.step < currentStep;
                    return (
                        <li key={s.step} className="md:flex-1">
                            <div
                                className={cn(
                                    "group flex flex-col border-l-4 py-2 pl-4 md:border-l-0 md:border-t-4 md:pb-0 md:pl-0 md:pt-4",
                                    active || done ? "border-foreground" : "border-border",
                                )}
                            >
                                <span
                                    className={cn(
                                        "text-sm font-medium",
                                        active || done ? "text-foreground" : "text-muted-foreground",
                                    )}
                                >
                                    {s.label}
                                </span>
                                <span className="text-sm font-medium text-foreground">{s.title}</span>
                            </div>
                        </li>
                    );
                })}
            </ol>
        </nav>
    );
}
