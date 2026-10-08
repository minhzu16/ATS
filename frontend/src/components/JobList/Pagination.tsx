import { useState, type FormEvent } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    pageSize: number;
    totalElements: number;
    onPageChange: (page: number) => void;
}

function buildVisiblePages(totalPages: number, currentPage: number): Array<number | "ellipsis"> {
    if (totalPages <= 0) return [];
    if (totalPages <= 4) {
        return Array.from({ length: totalPages }, (_, i) => i);
    }
    const last = totalPages - 1;
    const safe = Math.min(Math.max(currentPage, 0), last);
    const set = new Set<number>();
    set.add(0);
    set.add(1);
    set.add(last - 1);
    set.add(last);
    for (let i = safe - 1; i <= safe + 1; i++) {
        if (i >= 0 && i <= last) set.add(i);
    }
    const sorted = [...set].sort((a, b) => a - b);
    const out: Array<number | "ellipsis"> = [];
    for (let i = 0; i < sorted.length; i++) {
        if (i > 0 && sorted[i] - sorted[i - 1] > 1) {
            out.push("ellipsis");
        }
        out.push(sorted[i]);
    }
    return out;
}

export function Pagination({
    currentPage,
    totalPages,
    pageSize,
    totalElements,
    onPageChange,
}: PaginationProps) {
    const [goToValue, setGoToValue] = useState("");

    const start = totalElements === 0 ? 0 : currentPage * pageSize + 1;
    const end = Math.min((currentPage + 1) * pageSize, totalElements);

    const visible = buildVisiblePages(totalPages, currentPage);

    const handleGoToSubmit = (e: FormEvent) => {
        e.preventDefault();
        const n = parseInt(goToValue, 10);
        if (Number.isNaN(n) || n < 1) return;
        const zero = Math.min(n - 1, Math.max(totalPages - 1, 0));
        onPageChange(zero);
        setGoToValue("");
    };

    if (totalElements === 0 && totalPages === 0) {
        return null;
    }

    return (
        <div className="mt-4 flex flex-col gap-4 rounded-lg border border-border bg-card px-4 py-3 shadow-sm sm:flex-row sm:items-center sm:justify-between sm:px-6">
            <div className="flex flex-1 justify-between sm:hidden">
                <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={currentPage <= 0}
                    onClick={() => onPageChange(currentPage - 1)}
                >
                    Previous
                </Button>
                <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={currentPage >= totalPages - 1}
                    onClick={() => onPageChange(currentPage + 1)}
                >
                    Next
                </Button>
            </div>

            <div className="hidden flex-1 items-center justify-between gap-4 sm:flex">
                <p className="text-sm text-muted-foreground">
                    Showing <span className="font-medium text-foreground">{start}</span> to{" "}
                    <span className="font-medium text-foreground">{end}</span> of{" "}
                    <span className="font-medium text-foreground">{totalElements}</span> results
                </p>

                <div className="flex flex-wrap items-center gap-2">
                    <nav className="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                        <Button
                            type="button"
                            variant="outline"
                            size="icon"
                            className="rounded-l-md rounded-r-none"
                            disabled={currentPage <= 0}
                            onClick={() => onPageChange(currentPage - 1)}
                            aria-label="Previous page"
                        >
                            <ChevronLeft className="h-5 w-5" />
                        </Button>
                        {visible.map((item, idx) =>
                            item === "ellipsis" ? (
                                <span
                                    key={`e-${idx}`}
                                    className="relative inline-flex items-center rounded-none border border-border bg-card px-3 py-2 text-sm text-muted-foreground"
                                >
                                    …
                                </span>
                            ) : (
                                <Button
                                    key={item}
                                    type="button"
                                    variant={item === currentPage ? "default" : "outline"}
                                    size="sm"
                                    className={cn(
                                        "relative min-w-[2.5rem] rounded-none border-border",
                                        item === currentPage && "z-10",
                                    )}
                                    onClick={() => onPageChange(item)}
                                    aria-current={item === currentPage ? "page" : undefined}
                                >
                                    {item + 1}
                                </Button>
                            ),
                        )}
                        <Button
                            type="button"
                            variant="outline"
                            size="icon"
                            className="rounded-l-none rounded-r-md"
                            disabled={currentPage >= totalPages - 1}
                            onClick={() => onPageChange(currentPage + 1)}
                            aria-label="Next page"
                        >
                            <ChevronRight className="h-5 w-5" />
                        </Button>
                    </nav>

                    <form onSubmit={handleGoToSubmit} className="flex items-center gap-2">
                        <label htmlFor="job-page-goto" className="text-sm text-muted-foreground whitespace-nowrap">
                            Go to page
                        </label>
                        <Input
                            id="job-page-goto"
                            type="number"
                            min={1}
                            max={Math.max(totalPages, 1)}
                            aria-label="Page number"
                            placeholder="1"
                            value={goToValue}
                            onChange={(e) => setGoToValue(e.target.value)}
                            className="h-9 w-16 border-border bg-background text-center"
                        />
                        <Button type="submit" size="sm" variant="secondary">
                            Go
                        </Button>
                    </form>
                </div>
            </div>
        </div>
    );
}
