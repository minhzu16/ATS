import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Search, FileText } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { offerService } from "@/services/offerService";
import type { Offer, OfferStatus } from "@/types/offer";
import type { CandidateStage } from "@/types/candidate";

function candidateStageLabel(stage: CandidateStage | undefined): string {
  if (!stage) return "—";
  return stage.charAt(0) + stage.slice(1).toLowerCase();
}

const STATUS_STYLES: Record<OfferStatus, string> = {
  DRAFT: "bg-gray-100 text-gray-700 ring-1 ring-gray-300",
  PENDING_APPROVAL: "bg-yellow-50 text-yellow-800 ring-1 ring-yellow-600/20",
  APPROVED: "bg-green-50 text-green-700 ring-1 ring-green-700/10",
  REJECTED: "bg-red-50 text-red-700 ring-1 ring-red-700/10",
  SENT: "bg-blue-50 text-blue-700 ring-1 ring-blue-700/10",
  ACCEPTED: "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-700/10",
  DECLINED: "bg-orange-50 text-orange-700 ring-1 ring-orange-700/10",
};

function statusLabel(status: OfferStatus): string {
  return status.replace(/_/g, " ");
}

export default function OffersListPage() {
  const navigate = useNavigate();
  const [offers, setOffers] = useState<Offer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<"ALL" | OfferStatus>("ALL");

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const data = await offerService.getAllOffers();
        setOffers(data);
      } catch {
        setError("Failed to load offers.");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, []);

  const filtered = useMemo(() => {
    let result = offers;
    if (statusFilter !== "ALL") {
      result = result.filter((o) => o.status === statusFilter);
    }
    const q = search.trim().toLowerCase();
    if (q) {
      result = result.filter(
        (o) =>
          o.candidateName.toLowerCase().includes(q) ||
          o.jobTitle.toLowerCase().includes(q) ||
          o.positionTitle.toLowerCase().includes(q),
      );
    }
    return result;
  }, [offers, search, statusFilter]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-semibold tracking-tight">Offers</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Submitting an offer for approval moves the candidate Interview → Offer; HR Manager approval moves Offer →
          Hired, or rejection moves Offer → Rejected.
        </p>
      </div>

      <section className="rounded-lg border border-border bg-card p-4">
        <div className="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div className="relative w-full md:max-w-xl">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search offers..."
              className="pl-9"
            />
          </div>
          <div className="flex items-center gap-2">
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as "ALL" | OfferStatus)}
              className="h-9 rounded-md border border-input bg-background px-3 text-sm"
            >
              <option value="ALL">All Status</option>
              <option value="DRAFT">Draft</option>
              <option value="PENDING_APPROVAL">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="SENT">Sent</option>
              <option value="ACCEPTED">Accepted</option>
              <option value="DECLINED">Declined</option>
            </select>
            <Button size="sm" className="h-9" onClick={() => navigate("/offers/new")}>
              <Plus className="h-4 w-4" />
              New Offer
            </Button>
          </div>
        </div>

        {error && <p className="py-4 text-sm text-destructive">{error}</p>}

        {!error && (
          <div className="overflow-hidden rounded-lg border border-border">
            <table className="min-w-full divide-y divide-border">
              <thead className="bg-muted/30">
                <tr>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Candidate</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Position</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Candidate stage</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Salary</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Offer status</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Created</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {loading ? (
                  <tr>
                    <td colSpan={6} className="px-5 py-10 text-center text-sm text-muted-foreground">
                      Loading offers...
                    </td>
                  </tr>
                ) : filtered.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-5 py-10 text-center text-sm text-muted-foreground">
                      <FileText className="mx-auto mb-2 h-10 w-10 opacity-30" />
                      No offers found.
                    </td>
                  </tr>
                ) : (
                  filtered.map((offer) => (
                    <tr
                      key={offer.id}
                      className="cursor-pointer bg-background transition-colors hover:bg-muted/20"
                      onClick={() => navigate(`/offers/${offer.id}`)}
                    >
                      <td className="px-5 py-4">
                        <p className="font-medium">{offer.candidateName}</p>
                        <p className="text-sm text-muted-foreground">{offer.jobTitle}</p>
                      </td>
                      <td className="px-5 py-4 text-sm">{offer.positionTitle}</td>
                      <td className="px-5 py-4 text-sm text-muted-foreground">
                        {candidateStageLabel(offer.applicationStage)}
                      </td>
                      <td className="px-5 py-4 text-sm font-medium">
                        ${offer.salary.toLocaleString()}
                      </td>
                      <td className="px-5 py-4">
                        <Badge
                          variant="outline"
                          className={STATUS_STYLES[offer.status]}
                        >
                          {statusLabel(offer.status)}
                        </Badge>
                      </td>
                      <td className="px-5 py-4 text-sm text-muted-foreground">
                        {new Date(offer.createdAt).toLocaleDateString()}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
