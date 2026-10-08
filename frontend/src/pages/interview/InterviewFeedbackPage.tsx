import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Calendar,
  Clock,
  FileText,
  MapPin,
  Mail,
  Phone,
  ChevronRight
} from 'lucide-react';
import { interviewService } from '@/services/interviewService';
import type { 
  InterviewDetailResponse, 
  ScorecardTemplateResponse, 
  SubmitFeedbackRequest, 
  ParticipantResponse 
} from '@/types/interview';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import { useAuth } from '@/hooks/useAuth';

const InterviewFeedbackPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user: currentUser } = useAuth();
  const [interview, setInterview] = useState<InterviewDetailResponse | null>(null);
  const [template, setTemplate] = useState<ScorecardTemplateResponse | null>(null);
  const [allTemplates, setAllTemplates] = useState<ScorecardTemplateResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [scores, setScores] = useState<Record<string, number>>({});
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      if (!id) return;
      try {
        const [detailData, templatesListData] = await Promise.all([
          interviewService.getInterviewById(id),
          interviewService.getAllTemplates()
        ]);

        let templateData: ScorecardTemplateResponse | null = null;
        try {
          templateData = await interviewService.getTemplate(id);
        } catch {
          templateData = templatesListData[0] ?? null;
        }

        setInterview(detailData);
        setTemplate(templateData);
        setAllTemplates(templatesListData);
      } catch (error) {
        console.error('Error fetching interview data:', error);
        toast.error('Failed to load interview details');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  const handleTemplateChange = (templateId: string) => {
    const selected = allTemplates.find(t => t.id === templateId);
    if (selected) {
      setTemplate(selected);
      // Clear scores when template changes to avoid mismatch
      setScores({});
    }
  };

  const handleScoreChange = (criterionId: string, score: number) => {
    setScores(prev => ({ ...prev, [criterionId]: score }));
  };

  const handleSubmit = async () => {
    if (!id || !currentUser || !template) return;
    
    // Check if all criteria are scored
    const allScored = template.criteria.every(c => scores[c.id]);
    if (!allScored) {
        toast.warning('Please provide scores for all criteria');
        return;
    }

    setSubmitting(true);
    try {
      const payload: SubmitFeedbackRequest = {
        interviewId: id,
        interviewerId: currentUser.id,
        scores: template.criteria.map(c => ({
          criterionId: c.id,
          score: scores[c.id]
        })),
        feedback: notes
      };
      await interviewService.submitFeedback(payload);
      toast.success('Feedback submitted successfully');
      navigate('/interviews');
    } catch (error: unknown) {
      console.error('Error submitting feedback:', error);
      const message =
        typeof error === 'object' &&
        error !== null &&
        'response' in error &&
        typeof (error as { response?: { data?: { message?: unknown } } }).response?.data?.message === 'string'
          ? (error as { response?: { data?: { message?: string } } }).response?.data?.message ?? 'Failed to submit feedback'
          : 'Failed to submit feedback';
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!interview) return <div>Interview not found</div>;

  const formatDate = (dateStr: string | null | undefined) => {
    if (!dateStr) return '';
    // Strip 'Z' to prevent timezone auto-conversion to local time (+07:00)
    const localDateString = dateStr.endsWith('Z') ? dateStr.slice(0, -1) : dateStr;
    return new Date(localDateString).toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  };

  return (
    <div className="max-w-6xl mx-auto p-6 bg-[#f8fafc] min-h-screen">
      {/* Header Card */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-slate-200 mb-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 mb-1">
              {interview.location} Interview: {interview.candidateName}
            </h1>
            <p className="text-slate-500 font-medium text-sm">
              {interview.jobTitle} • {interview.jobDepartment}
            </p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="text-slate-600 border-slate-200">Reschedule</Button>
            <Button variant="outline" className="text-red-600 border-red-100 hover:bg-red-50">Cancel</Button>
          </div>
        </div>

        <div className="flex flex-wrap gap-12 text-slate-700 mt-8">
          <div className="flex items-center gap-3">
            <Calendar className="w-6 h-6 text-slate-400" strokeWidth={1.5} />
            <span className="text-xl font-medium">{formatDate(interview.scheduledAt)}</span>
          </div>
          <div className="flex items-center gap-3">
            <Clock className="w-6 h-6 text-slate-400" strokeWidth={1.5} />
            <span className="text-xl font-medium">
              {(() => {
                const dateStr = interview.scheduledAt;
                const localDateString = dateStr.endsWith('Z') ? dateStr.slice(0, -1) : dateStr;
                return new Date(localDateString).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
              })()}
            </span>
          </div>
          <div className="flex items-center gap-3">
            <MapPin className="w-6 h-6 text-slate-400" strokeWidth={1.5} />
            {interview.location === 'ONLINE' ? (
              <a href={interview.meetingLink || '#'} target="_blank" className="text-xl font-medium text-slate-700 hover:text-blue-600 transition-colors">
                Google Meet
              </a>
            ) : (
              <span className="text-xl font-medium">{interview.location}</span>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sidebar */}
        <div className="lg:col-span-1 space-y-6">
          {/* Candidate Card */}
          <div className="bg-white rounded-xl p-6 shadow-sm border border-slate-200">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">Candidate</h3>
            <div className="flex items-center gap-4 mb-6">
              <div className="w-12 h-12 bg-pink-100 text-pink-600 rounded-full flex items-center justify-center font-bold text-lg">
                {interview.candidateName?.split(' ').map((n: string) => n[0]).join('').slice(0, 2).toUpperCase()}
              </div>
              <div>
                <h4 className="font-bold text-slate-900">{interview.candidateName}</h4>
                <div className="space-y-1 mt-1">
                  {interview.candidateEmail && (
                    <div className="flex items-center gap-2 text-xs text-slate-500">
                      <Mail className="w-3 h-3" />
                      <span>{interview.candidateEmail}</span>
                    </div>
                  )}
                  {interview.candidatePhone && (
                    <div className="flex items-center gap-2 text-xs text-slate-500">
                      <Phone className="w-3 h-3" />
                      <span>{interview.candidatePhone}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
            
            {interview.candidateResumeUrl && (
              <a 
                href={interview.candidateResumeUrl} 
                target="_blank"
                className="flex items-center gap-3 p-3 bg-slate-50 border border-slate-200 rounded-lg group hover:bg-slate-100 transition-colors"
              >
                <div className="p-2 bg-white rounded border border-slate-200 text-red-500">
                   <FileText className="w-4 h-4" />
                </div>
                <span className="text-sm font-medium text-slate-700 group-hover:text-slate-900">resume.pdf</span>
              </a>
            )}

            <Button 
              variant="link" 
              className="p-0 h-auto text-indigo-600 font-bold mt-6 hover:text-indigo-700 flex items-center group/eval"
              onClick={() => navigate(`/interviews/applications/${interview.applicationId}/evaluation`)}
            >
              View Full Evaluation History
              <ChevronRight className="w-4 h-4 ml-1 transition-transform group-hover/eval:translate-x-1" />
            </Button>
          </div>

          {/* Interviewers Card */}
          <div className="bg-white rounded-xl p-6 shadow-sm border border-slate-200">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">Interviewers</h3>
            <div className="space-y-4">
              {interview.participants.map((p: ParticipantResponse, idx: number) => (
                <div key={idx} className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-slate-100 text-slate-600 rounded-full flex items-center justify-center text-xs font-bold">
                    {p.fullName.split(' ').map((n: string) => n[0]).join('').slice(0, 2).toUpperCase()}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-slate-900 leading-tight">{p.fullName}</p>
                    <p className="text-xs text-slate-500 font-medium">
                      {p.role === 'ORGANIZER' ? 'Organizer' : 'Interviewer'}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Main Feedback Area */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-xl p-8 shadow-sm border border-slate-200">
            <div className="flex justify-between items-center mb-8">
              <h2 className="text-xl font-bold text-slate-900 font-inter">Interview Feedback</h2>
              <div className="flex flex-col gap-1">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Scorecard Template</label>
                <select 
                  className="bg-slate-50 border border-slate-200 text-slate-700 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 font-medium"
                  value={template?.id || ''}
                  onChange={(e) => handleTemplateChange(e.target.value)}
                >
                  {allTemplates.map(t => (
                    <option key={t.id} value={t.id}>{t.name}</option>
                  ))}
                </select>
              </div>
            </div>
            
            <div className="space-y-10">


              {/* Dynamic Criteria from Template */}
              {template?.criteria.map((criterion) => (
                <div key={criterion.id} className="space-y-3">
                  <label className="text-sm font-semibold text-slate-700">
                    {criterion.name} (1-5)
                  </label>
                  <div className="flex gap-2">
                    {[1, 2, 3, 4, 5].map((num) => (
                      <button
                        key={num}
                        onClick={() => handleScoreChange(criterion.id, num)}
                        className={`w-10 h-10 rounded-full border flex items-center justify-center text-sm font-bold transition-all
                          ${scores[criterion.id] === num 
                            ? 'bg-blue-600 border-blue-600 text-white shadow-md scale-110' 
                            : 'bg-white border-slate-200 text-slate-600 hover:border-blue-400 hover:text-blue-500'
                          }`}
                      >
                        {num}
                      </button>
                    ))}
                  </div>
                </div>
              ))}

              <div className="space-y-3">
                <label className="text-sm font-semibold text-slate-700">Detailed Notes</label>
                <Textarea 
                  placeholder="Strengths, weaknesses, key takeaways..."
                  className="min-h-[150px] border-slate-200 focus:border-blue-500 focus:ring-blue-500"
                  value={notes}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setNotes(e.target.value)}
                />
              </div>

              <div className="pt-4 flex justify-end">
                <Button 
                  onClick={handleSubmit} 
                  disabled={submitting}
                  className="bg-[#0f172a] hover:bg-[#1e293b] text-white px-8 py-6 h-auto text-sm font-bold rounded-lg"
                >
                  {submitting ? 'Submitting...' : 'Submit Feedback'}
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default InterviewFeedbackPage;
