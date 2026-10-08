import { Button } from "./button";
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react";

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
    totalElements: number;
    pageSize: number;
}

export function Pagination({
    currentPage,
    totalPages,
    onPageChange,
    totalElements,
    pageSize
}: PaginationProps) {
    if (totalElements === 0) return null;

    const start = currentPage * pageSize + 1;
    const end = Math.min((currentPage + 1) * pageSize, totalElements);

    return (
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 px-2 py-6 border-t border-border/10 mt-4">
            <div className="text-sm text-muted-foreground order-2 sm:order-1">
                Showing <span className="font-semibold text-foreground">{start}</span>–<span className="font-semibold text-foreground">{end}</span> of <span className="font-semibold text-foreground">{totalElements}</span> records
            </div>
            
            {totalPages > 1 && (
                <div className="flex items-center gap-1.5 order-1 sm:order-2">
                    <Button
                        variant="outline"
                        size="icon"
                        className="h-9 w-9 rounded-xl border-border/50 bg-background/50 backdrop-blur-sm hover:bg-primary/10 hover:text-primary transition-all"
                        onClick={() => onPageChange(0)}
                        disabled={currentPage === 0}
                    >
                        <ChevronsLeft className="h-4 w-4" />
                    </Button>
                    <Button
                        variant="outline"
                        size="icon"
                        className="h-9 w-9 rounded-xl border-border/50 bg-background/50 backdrop-blur-sm hover:bg-primary/10 hover:text-primary transition-all"
                        onClick={() => onPageChange(currentPage - 1)}
                        disabled={currentPage === 0}
                    >
                        <ChevronLeft className="h-4 w-4" />
                    </Button>
                    
                    <div className="flex items-center gap-2 px-4 py-1.5 rounded-xl bg-muted/30 border border-border/30 mx-1">
                        <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Page</span>
                        <span className="text-sm font-bold text-foreground">{currentPage + 1}</span>
                        <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">of</span>
                        <span className="text-sm font-bold text-foreground">{totalPages}</span>
                    </div>

                    <Button
                        variant="outline"
                        size="icon"
                        className="h-9 w-9 rounded-xl border-border/50 bg-background/50 backdrop-blur-sm hover:bg-primary/10 hover:text-primary transition-all"
                        onClick={() => onPageChange(currentPage + 1)}
                        disabled={currentPage >= totalPages - 1}
                    >
                        <ChevronRight className="h-4 w-4" />
                    </Button>
                    <Button
                        variant="outline"
                        size="icon"
                        className="h-9 w-9 rounded-xl border-border/50 bg-background/50 backdrop-blur-sm hover:bg-primary/10 hover:text-primary transition-all"
                        onClick={() => onPageChange(totalPages - 1)}
                        disabled={currentPage >= totalPages - 1}
                    >
                        <ChevronsRight className="h-4 w-4" />
                    </Button>
                </div>
            )}
        </div>
    );
}
