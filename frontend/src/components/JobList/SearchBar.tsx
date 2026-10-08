import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";

interface SearchBarProps {
    value: string;
    onChange: (value: string) => void;
    id?: string;
}

export function SearchBar({ value, onChange, id = "job-search" }: SearchBarProps) {
    return (
        <div className="relative w-full rounded-md shadow-sm">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                <Search className="h-4 w-4 text-muted-foreground" aria-hidden />
            </div>
            <label htmlFor={id} className="sr-only">
                Search jobs
            </label>
            <Input
                id={id}
                type="search"
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder="Search jobs..."
                className="border-border bg-background pl-10"
            />
        </div>
    );
}
