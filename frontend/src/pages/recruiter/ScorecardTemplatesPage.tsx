import { useEffect, useMemo, useState } from "react";
import { Plus, Search, Trash2, Pencil, ClipboardList } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { scorecardService } from "@/services/scorecardService";
import { departmentService } from "@/services/departmentService";
import type { Department } from "@/types/department";
import type {
  ScorecardTemplate,
  CreateScorecardTemplateRequest,
  CriterionRequest,
} from "@/types/scorecard";

export default function ScorecardTemplatesPage() {
  const [templates, setTemplates] = useState<ScorecardTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<ScorecardTemplate | null>(null);

  const loadTemplates = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await scorecardService.getAll();
      setTemplates(data);
    } catch {
      setError("Failed to load templates.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadTemplates();
  }, []);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return templates;
    return templates.filter(
      (t) =>
        t.name.toLowerCase().includes(q) ||
        (t.departmentName && t.departmentName.toLowerCase().includes(q)),
    );
  }, [templates, search]);

  const handleDelete = async (id: string) => {
    if (!confirm("Are you sure you want to delete this template?")) return;
    try {
      await scorecardService.remove(id);
      await loadTemplates();
    } catch {
      alert("Failed to delete template.");
    }
  };

  const openCreate = () => {
    setEditingTemplate(null);
    setDialogOpen(true);
  };

  const openEdit = (tpl: ScorecardTemplate) => {
    setEditingTemplate(tpl);
    setDialogOpen(true);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-semibold tracking-tight">Scorecard Templates</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Manage evaluation criteria templates for interviews
        </p>
      </div>

      <section className="rounded-lg border border-border bg-card p-4">
        <div className="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div className="relative w-full md:max-w-xl">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search templates..."
              className="pl-9"
            />
          </div>
          <Button size="sm" className="h-9" onClick={openCreate}>
            <Plus className="h-4 w-4" />
            New Template
          </Button>
        </div>

        {error && <p className="py-4 text-sm text-destructive">{error}</p>}

        {!error && (
          <div className="overflow-hidden rounded-lg border border-border">
            <table className="min-w-full divide-y divide-border">
              <thead className="bg-muted/30">
                <tr>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Name</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Department</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Criteria</th>
                  <th className="px-5 py-3 text-right text-sm font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {loading ? (
                  <tr>
                    <td
                      colSpan={4}
                      className="px-5 py-10 text-center text-sm text-muted-foreground"
                    >
                      Loading templates...
                    </td>
                  </tr>
                ) : filtered.length === 0 ? (
                  <tr>
                    <td
                      colSpan={4}
                      className="px-5 py-10 text-center text-sm text-muted-foreground"
                    >
                      <ClipboardList className="mx-auto mb-2 h-10 w-10 opacity-30" />
                      No templates found.
                    </td>
                  </tr>
                ) : (
                  filtered.map((tpl) => (
                    <tr
                      key={tpl.id}
                      className="cursor-pointer bg-background transition-colors hover:bg-muted/20"
                      onClick={() => openEdit(tpl)}
                    >
                      <td className="px-5 py-4">
                        <p className="font-medium">{tpl.name}</p>
                      </td>
                      <td className="px-5 py-4 text-sm text-muted-foreground">
                        {tpl.departmentName || "—"}
                      </td>
                      <td className="px-5 py-4">
                        <Badge variant="secondary">{tpl.criteria.length} criteria</Badge>
                      </td>
                      <td className="px-5 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              openEdit(tpl);
                            }}
                          >
                            <Pencil className="h-3.5 w-3.5" />
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="text-destructive hover:text-destructive"
                            onClick={(e) => {
                              e.stopPropagation();
                              void handleDelete(tpl.id);
                            }}
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {dialogOpen && (
        <TemplateDialog
          template={editingTemplate}
          onClose={() => setDialogOpen(false)}
          onSaved={() => {
            setDialogOpen(false);
            void loadTemplates();
          }}
        />
      )}
    </div>
  );
}

/* ──────────────────────── Template Create/Edit Dialog ──────────────────────── */

function TemplateDialog({
  template,
  onClose,
  onSaved,
}: {
  template: ScorecardTemplate | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const isEdit = template !== null;
  const [name, setName] = useState(template?.name ?? "");
  const [departmentId, setDepartmentId] = useState(template?.departmentId ?? "");
  const [criteria, setCriteria] = useState<CriterionRequest[]>(
    template?.criteria.map((c: any) => ({ name: c.name, weight: c.weight })) ?? [
      { name: "", weight: 1 },
    ],
  );
  const [saving, setSaving] = useState(false);
  const [departments, setDepartments] = useState<Department[]>([]);

  useEffect(() => {
    void departmentService.getAllDepartments().then(setDepartments).catch(console.error);
  }, []);

  const addCriterion = () => {
    setCriteria([...criteria, { name: "", weight: 1 }]);
  };

  const removeCriterion = (index: number) => {
    setCriteria(criteria.filter((_, i) => i !== index));
  };

  const updateCriterion = (index: number, field: keyof CriterionRequest, value: string | number) => {
    const updated = [...criteria];
    updated[index] = { ...updated[index], [field]: value };
    setCriteria(updated);
  };

  const handleSubmit = async () => {
    if (!name.trim()) {
      alert("Template name is required.");
      return;
    }
    if (criteria.some((c) => !c.name.trim())) {
      alert("All criteria must have a name.");
      return;
    }
    setSaving(true);
    try {
      const request: CreateScorecardTemplateRequest = {
        name: name.trim(),
        departmentId: departmentId || null,
        criteria,
      };
      if (isEdit && template) {
        await scorecardService.update(template.id, request);
      } else {
        await scorecardService.create(request);
      }
      onSaved();
    } catch {
      alert("Failed to save template.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="w-full max-w-lg rounded-lg border border-border bg-card p-6 shadow-lg">
        <h2 className="mb-4 text-lg font-semibold">
          {isEdit ? "Edit Template" : "Create Template"}
        </h2>

        <div className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium">Template Name</label>
            <Input value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Engineering Interview" />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Department (optional)</label>
            <select
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              value={departmentId}
              onChange={(e) => setDepartmentId(e.target.value)}
            >
              <option value="">-- Company Wide --</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <div className="mb-2 flex items-center justify-between">
              <label className="text-sm font-medium">Criteria</label>
              <Button size="sm" variant="outline" onClick={addCriterion}>
                <Plus className="h-3.5 w-3.5" /> Add
              </Button>
            </div>
            <div className="space-y-2">
              {criteria.map((c, i) => (
                <div key={i} className="flex items-center gap-2">
                  <Input
                    value={c.name}
                    onChange={(e) => updateCriterion(i, "name", e.target.value)}
                    placeholder="Criterion name"
                    className="flex-1"
                  />
                  <Input
                    type="number"
                    value={c.weight}
                    onChange={(e) => updateCriterion(i, "weight", parseFloat(e.target.value) || 1)}
                    className="w-20"
                    min={0.1}
                    step={0.1}
                  />
                  {criteria.length > 1 && (
                    <Button
                      size="sm"
                      variant="outline"
                      className="text-destructive"
                      onClick={() => removeCriterion(i)}
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-2">
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={saving}>
            {saving ? "Saving..." : isEdit ? "Update" : "Create"}
          </Button>
        </div>
      </div>
    </div>
  );
}
