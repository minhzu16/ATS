import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Star, 
  CheckCircle, 
  Users, 
  Calendar,
  MessageSquare,
  ChevronRight,
  User,
  Layout
} from 'lucide-react';
import { interviewService } from '@/services/interviewService';
import type { ApplicationEvaluationResponse, InterviewStageEvaluationResponse } from '@/types/interview';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';

const CandidateEvaluationPage = () => {
  const { applicationId } = useParams<{ applicationId: string }>();
  const navigate = useNavigate();
  const [evaluation, setEvaluation] = useState<ApplicationEvaluationResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEvaluation = async () => {
      if (!applicationId) return;
      try {
        const data = await interviewService.getApplicationEvaluation(applicationId);
        setEvaluation(data);
      } catch (error) {
        console.error('Error fetching evaluation:', error);
        toast.error('Failed to load evaluation data');
      } finally {
        setLoading(false);
      }
    };
    fetchEvaluation();
  }, [applicationId]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!evaluation) return <div className="p-8 text-center text-slate-500">Evaluation data not found</div>;

  const formatDate = (dateStr: string) => {
    const localDateString = dateStr.endsWith('Z') ? dateStr.slice(0, -1) : dateStr;
    return new Date(localDateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  };

  return (
    <div className="max-w-6xl mx-auto p-6 bg-[#f8fafc] min-h-screen font-inter">
      {/* Header section */}
      <div className="flex justify-between items-start mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 mb-2">Candidate Evaluation</h1>
          <div className="flex items-center gap-2 text-slate-500 font-medium">
            <span className="text-slate-900">{evaluation.candidateName}</span>
            <span>•</span>
            <span>{evaluation.jobTitle}</span>
          </div>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" className="text-slate-600 border-slate-200 bg-white">
            Download PDF
          </Button>
          <Button className="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold">
            Final Decision
          </Button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
              <Star className="w-5 h-5" />
            </div>
            <span className="text-sm font-bold text-slate-400 uppercase tracking-wider">Overall Score</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-4xl font-bold text-slate-900">{evaluation.overallScore}</span>
            <span className="text-slate-400 font-medium">/ 5.0</span>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
              <CheckCircle className="w-5 h-5" />
            </div>
            <span className="text-sm font-bold text-slate-400 uppercase tracking-wider">Recommendation</span>
          </div>
          <div className="text-4xl font-bold text-emerald-600">
            {evaluation.recommendation}
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 bg-amber-50 text-amber-600 rounded-lg">
              <Users className="w-5 h-5" />
            </div>
            <span className="text-sm font-bold text-slate-400 uppercase tracking-wider">Interviews Completed</span>
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-4xl font-bold text-slate-900">{evaluation.interviewsCompleted}</span>
            <span className="text-slate-400 font-medium">/ {evaluation.totalInterviews} stages</span>
          </div>
        </div>
      </div>

      {/* Details Section */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
          <h3 className="font-bold text-slate-900">Stage-by-Stage Feedback</h3>
          <Layout className="w-5 h-5 text-slate-400" />
        </div>

        <div className="divide-y divide-slate-100">
          {evaluation.stages.map((stage: InterviewStageEvaluationResponse, idx: number) => (
            <div key={idx} className="p-6 hover:bg-slate-50 transition-colors group cursor-pointer" onClick={() => navigate(`/interviews/${stage.interviewId}`)}>
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-3">
                    <span className={`px-2.5 py-1 text-xs font-bold rounded-full uppercase
                      ${stage.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'}`}
                    >
                      {stage.status === 'COMPLETED' ? 'Passed' : 'Pending'}
                    </span>
                    <h4 className="font-bold text-slate-900 text-lg">{stage.type}</h4>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div className="flex items-center gap-6">
                      <div className="flex items-center gap-2 text-sm">
                        <User className="w-4 h-4 text-slate-400" />
                        <span className="font-medium text-slate-600">{stage.interviewerName}</span>
                      </div>
                      <div className="flex items-center gap-2 text-sm">
                        <Calendar className="w-4 h-4 text-slate-400" />
                        <span className="font-medium text-slate-600">{formatDate(stage.scheduledAt)}</span>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-slate-400 uppercase tracking-wider italic">Score:</span>
                      <span className="text-lg font-bold text-slate-900">{stage.score > 0 ? stage.score : '--'}</span>
                    </div>
                  </div>

                  <div className="bg-slate-50 p-4 rounded-xl border border-slate-100 flex gap-3">
                    <MessageSquare className="w-5 h-5 text-slate-400 shrink-0 mt-0.5" />
                    <p className="text-slate-600 text-sm leading-relaxed italic">
                      "{stage.feedbackSnippet || 'No feedback provided yet.'}"
                    </p>
                  </div>
                </div>
                <div className="p-2 text-slate-300 group-hover:text-indigo-600 transition-colors">
                  <ChevronRight className="w-6 h-6" />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default CandidateEvaluationPage;
