import { useState, useEffect, useCallback } from "react";
import {
    Search, Plus, Building2, Pencil, Trash2, RefreshCw, Users, Briefcase, Info, X, Check
} from "lucide-react";
import { Pagination } from "@/components/ui/pagination";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { adminService } from "@/services/adminService";
import type { Department } from "@/types/admin";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
    DialogDescription
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

export default function DepartmentsPage() {
    const [departments, setDepartments] = useState<Department[]>([]);
    const [page, setPage] = useState(0);
    const [pageSize] = useState(6);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [search, setSearch] = useState("");
    
    // Modal state
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingDept, setEditingDept] = useState<Department | null>(null);
    const [formData, setFormData] = useState({ name: "", description: "" });

    const loadDepartments = useCallback(async (p: number) => {
        setIsLoading(true);
        try {
            const data = await adminService.getDepartments(p, pageSize);
            setDepartments(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
            setPage(data.number);
        } catch {
            // Silently fail
        } finally {
            setIsLoading(false);
        }
    }, [pageSize]);

    useEffect(() => {
        loadDepartments(page);
    }, [loadDepartments, page]);

    const handleOpenModal = (dept: Department | null = null) => {
        if (dept) {
            setEditingDept(dept);
            setFormData({ name: dept.name, description: dept.description || "" });
        } else {
            setEditingDept(null);
            setFormData({ name: "", description: "" });
        }
        setIsModalOpen(true);
    };

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.name.trim()) return;

        setIsSaving(true);
        try {
            if (editingDept) {
                await adminService.updateDepartment(editingDept.id, formData);
            } else {
                await adminService.createDepartment(formData);
            }
            setIsModalOpen(false);
            loadDepartments(page);
        } catch (error) {
            console.error("Failed to save department", error);
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async (id: string) => {
        if (!window.confirm("Are you sure you want to delete this department?")) return;
        
        try {
            await adminService.deleteDepartment(id);
            loadDepartments(page);
        } catch (error) {
            console.error("Failed to delete department", error);
        }
    };

    const filtered = departments.filter((d) =>
        d.name.toLowerCase().includes(search.toLowerCase()) ||
        d.description?.toLowerCase().includes(search.toLowerCase())
    );

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary shadow-sm border border-primary/20">
                        <Building2 className="h-6 w-6" />
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold tracking-tight text-foreground">Department Management</h1>
                        <p className="text-sm text-muted-foreground">
                            Organize and manage organizational units
                        </p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => loadDepartments(page)} disabled={isLoading} className="rounded-full">
                        <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? "animate-spin" : ""}`} />
                        Refresh
                    </Button>
                    <Button size="sm" onClick={() => handleOpenModal()} className="rounded-full shadow-lg shadow-primary/25">
                        <Plus className="mr-2 h-4 w-4" />
                        Add Department
                    </Button>
                </div>
            </div>

            {/* Filters */}
            <div className="glass-morphism rounded-2xl border border-border/50 bg-card/60 p-4 backdrop-blur-md">
                <div className="relative max-w-md">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <Input
                        placeholder="Search departments..."
                        className="pl-10 h-11 rounded-xl bg-background/50 border-border/50 focus:ring-2 focus:ring-primary/20 transition-all"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>

            {/* Cards Grid */}
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {isLoading ? (
                    Array(3).fill(0).map((_, i) => (
                        <div key={i} className="h-48 rounded-2xl border border-border bg-card/50 animate-pulse" />
                    ))
                ) : filtered.length === 0 ? (
                    <div className="col-span-full flex flex-col items-center justify-center py-20 text-muted-foreground">
                        <Building2 className="h-16 w-16 opacity-10 mb-4" />
                        <p className="text-lg font-medium">No departments found</p>
                        <p className="text-sm opacity-60">Try adjusting your search filters</p>
                    </div>
                ) : (
                    filtered.map((dept) => (
                        <div key={dept.id} className="group relative overflow-hidden rounded-2xl border border-border bg-card p-6 transition-all hover:shadow-xl hover:-translate-y-1 hover:border-primary/20">
                            <div className="absolute top-0 left-0 w-1 h-full bg-primary transition-all duration-300 opacity-0 group-hover:opacity-100" />
                            <div className="flex items-start justify-between">
                                <div className="flex-1 min-w-0">
                                    <h3 className="text-lg font-semibold text-foreground group-hover:text-primary transition-colors truncate">{dept.name}</h3>
                                    <p className="mt-2 text-sm text-muted-foreground line-clamp-2 leading-relaxed min-h-[40px]">
                                        {dept.description || "No description provided."}
                                    </p>
                                </div>
                                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity ml-4">
                                    <Button 
                                        variant="ghost" 
                                        size="icon" 
                                        onClick={() => handleOpenModal(dept)}
                                        className="h-9 w-9 rounded-full hover:bg-primary/10 hover:text-primary"
                                    >
                                        <Pencil className="h-4 w-4" />
                                    </Button>
                                    <Button 
                                        variant="ghost" 
                                        size="icon" 
                                        onClick={() => handleDelete(dept.id)}
                                        className="h-9 w-9 rounded-full hover:bg-destructive/10 hover:text-destructive"
                                    >
                                        <Trash2 className="h-4 w-4" />
                                    </Button>
                                </div>
                            </div>
                            
                            {/* Stats */}
                            <div className="mt-6 flex items-center gap-4">
                                <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-muted/50 border border-border/50">
                                    <Users className="h-4 w-4 text-primary" />
                                    <span className="text-sm font-medium">{dept.employeeCount || 0}</span>
                                    <span className="text-[10px] text-muted-foreground hidden sm:inline uppercase tracking-wider">Members</span>
                                </div>
                                <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-muted/50 border border-border/50">
                                    <Briefcase className="h-4 w-4 text-emerald-500" />
                                    <span className="text-sm font-medium">{dept.openPositions || 0}</span>
                                    <span className="text-[10px] text-muted-foreground hidden sm:inline uppercase tracking-wider">Jobs</span>
                                </div>
                            </div>

                            <div className="mt-6 pt-4 border-t border-border/50 flex items-center justify-between text-xs text-muted-foreground">
                                <span className={`flex items-center gap-1.5 ${dept.status ? "text-emerald-500 font-medium" : "text-muted-foreground"}`}>
                                    <div className={`w-1.5 h-1.5 rounded-full ${dept.status ? "bg-emerald-500" : "bg-muted-foreground"}`} /> 
                                    {dept.status ? "Active" : "Inactive"}
                                </span>
                                <span className="flex items-center gap-1">
                                    <Info className="h-3 w-3" />
                                    ID: {dept.id.slice(0, 8)}
                                </span>
                            </div>
                        </div>
                    ))
                )}
            </div>

            <div className="mt-8 flex justify-center">
                <Pagination 
                    currentPage={page} 
                    totalPages={totalPages} 
                    totalElements={totalElements} 
                    pageSize={pageSize} 
                    onPageChange={setPage}
                />
            </div>

            {/* Add/Edit Modal */}
            <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
                <DialogContent className="sm:max-w-[425px] rounded-2xl border-border/50 bg-background/95 backdrop-blur-xl">
                    <DialogHeader>
                        <DialogTitle className="text-xl font-bold flex items-center gap-2">
                            {editingDept ? <Pencil className="h-5 w-5 text-primary" /> : <Plus className="h-5 w-5 text-primary" />}
                            {editingDept ? "Edit Department" : "New Department"}
                        </DialogTitle>
                        <DialogDescription className="text-muted-foreground">
                            {editingDept ? "Update department details and information." : "Create a new organizational unit for your system."}
                        </DialogDescription>
                    </DialogHeader>
                    <form onSubmit={handleSave} className="space-y-6 pt-4">
                        <div className="space-y-4">
                            <div className="space-y-2">
                                <Label htmlFor="name" className="text-sm font-semibold ml-1">Department Name</Label>
                                <Input
                                    id="name"
                                    placeholder="e.g. Engineering, Marketing..."
                                    className="h-11 rounded-xl bg-background/50 border-border/50 focus:ring-2 focus:ring-primary/20 transition-all font-medium"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="description" className="text-sm font-semibold ml-1">Description</Label>
                                <Textarea
                                    id="description"
                                    placeholder="Tell more about this department..."
                                    className="min-h-[120px] rounded-xl bg-background/50 border-border/50 focus:ring-2 focus:ring-primary/20 transition-all resize-none leading-relaxed"
                                    value={formData.description}
                                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                />
                            </div>
                        </div>
                        <DialogFooter className="gap-2 sm:gap-0 mt-8">
                            <Button 
                                type="button" 
                                variant="ghost" 
                                onClick={() => setIsModalOpen(false)}
                                className="rounded-xl px-6 h-11"
                            >
                                <X className="mr-2 h-4 w-4" />
                                Cancel
                            </Button>
                            <Button 
                                type="submit" 
                                disabled={isSaving || !formData.name.trim()}
                                className="rounded-xl px-8 h-11 shadow-lg shadow-primary/25"
                            >
                                {isSaving ? (
                                    <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                                ) : (
                                    <Check className="mr-2 h-4 w-4" />
                                )}
                                {editingDept ? "Update" : "Create"}
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>
        </div>
    );
}
