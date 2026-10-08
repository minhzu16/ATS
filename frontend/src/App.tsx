import { Navigate, BrowserRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { Toaster } from "sonner";
import ProtectedRoute from "@/routes/ProtectedRoute";
import AdminRoute from "@/routes/AdminRoute";
import HrRoute from "@/routes/HrRoute";
import HrOnlyRoute from "@/routes/HrOnlyRoute";
import HrManagerRoute from "@/routes/HrManagerRoute";
import HrOrAdminRoute from "@/routes/HrOrAdminRoute";
import InterviewRoute from "@/routes/InterviewRoute";
import HomeRedirect from "@/routes/HomeRedirect";
import AppLayout from "@/layouts/AppLayout";
import AuthLayout from "@/layouts/AuthLayout";
import LoginPage from "@/pages/auth/LoginPage";
import ForgotPasswordPage from "@/pages/auth/ForgotPasswordPage";
import SetPasswordPage from "@/pages/auth/SetPasswordPage";
import ResetPasswordPage from "@/pages/auth/ResetPasswordPage";
import DashboardPage from "@/pages/recruiter/DashboardPage";
import AdminDashboardPage from "@/pages/admin/AdminDashboardPage";
import ProfilePage from "@/pages/recruiter/ProfilePage";
import ChangePasswordPage from "@/pages/recruiter/ChangePasswordPage";
import CandidateListPage from "@/pages/recruiter/CandidateListPage";
import CandidateProfilePage from "@/pages/recruiter/CandidateProfilePage";
import CandidateScheduleInterviewsPage from "@/pages/recruiter/CandidateScheduleInterviewsPage";
import InterviewCalendarPage from "@/pages/interview/InterviewCalendarPage";
import InterviewFeedbackPage from "@/pages/interview/InterviewFeedbackPage";
import InterviewCandidateEvaluationPage from "@/pages/interview/CandidateEvaluationPage";
import AdminUsersPage from "@/pages/admin/AdminUsersPage";
import AdminCreateUserPage from "@/pages/admin/AdminCreateUserPage";
import AdminEditUserPage from "@/pages/admin/AdminEditUserPage";
import DepartmentsPage from "@/pages/admin/DepartmentsPage";
import SystemConfigPage from "@/pages/admin/SystemConfigPage";
import AuditLogsPage from "@/pages/admin/AuditLogsPage";
import NotificationPage from "@/pages/admin/NotificationPage";
import ScorecardTemplatesPage from "@/pages/recruiter/ScorecardTemplatesPage";
import CandidateRankingPage from "@/pages/recruiter/CandidateRankingPage";
import CandidateEvaluationPage from "@/pages/recruiter/CandidateEvaluationPage";
import OffersListPage from "@/pages/recruiter/OffersListPage";
import OfferFormPage from "@/pages/recruiter/OfferFormPage";
import OfferDetailPage from "@/pages/recruiter/OfferDetailPage";
import OnboardingPage from "@/pages/recruiter/OnboardingPage";
import OnboardingListPage from "@/pages/recruiter/OnboardingListPage";
import JobListPage from "@/pages/recruiter/JobListPage";
import JobDetailPage from "@/pages/recruiter/JobDetailPage";
import CreateJobPage from "@/pages/recruiter/CreateJobPage";
import EditJobPage from "@/pages/recruiter/EditJobPage";
import JobApprovalPage from "@/pages/recruiter/JobApprovalPage";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster position="top-right" expand={false} richColors />
        <Routes>
          {/* Public auth routes (centered card layout) */}
          <Route element={<AuthLayout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            {/* Token-based pages (linked from emails) */}
            <Route path="/activate" element={<SetPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
          </Route>

          {/* Protected routes — any authenticated user */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/" element={<HomeRedirect />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/change-password" element={<ChangePasswordPage />} />

              {/* Jobs: specific paths before /jobs/:id */}
              <Route element={<HrOnlyRoute />}>
                <Route path="/jobs/new" element={<CreateJobPage />} />
                <Route path="/jobs/:id/edit" element={<EditJobPage />} />
              </Route>
              <Route element={<HrManagerRoute />}>
                <Route path="/jobs/pending-approval" element={<JobApprovalPage />} />
              </Route>
              <Route path="/jobs" element={<JobListPage />} />
              <Route path="/jobs/:id" element={<JobDetailPage />} />
               {/* Interview routes (HR, Admin, Interviewer) */}
              <Route element={<InterviewRoute />}>
                <Route path="/interviews" element={<InterviewCalendarPage />} />
                <Route path="/interviews/:id" element={<InterviewFeedbackPage />} />
                <Route path="/interviews/applications/:applicationId/evaluation" element={<InterviewCandidateEvaluationPage />} />
              </Route>

              {/* Evaluation summary (HR, Admin) */}
              <Route element={<HrOrAdminRoute />}>
                <Route path="/applications/:applicationId/evaluation" element={<CandidateEvaluationPage />} />
              </Route>

              {/* HR-only route */}
              <Route element={<HrRoute />}>
                <Route path="/candidates" element={<CandidateListPage />} />
                <Route path="/candidates/:candidateId" element={<CandidateProfilePage />} />
                <Route path="/candidates/:candidateId/schedule-interviews" element={<CandidateScheduleInterviewsPage />} />
                <Route path="/scorecard-templates" element={<ScorecardTemplatesPage />} />
                <Route path="/applications/:applicationId/evaluation" element={<CandidateEvaluationPage />} />
                <Route path="/jobs/:jobId/ranking" element={<CandidateRankingPage />} />
                <Route path="/offers" element={<OffersListPage />} />
                <Route path="/offers/new" element={<OfferFormPage />} />
                <Route path="/offers/:id" element={<OfferDetailPage />} />
                <Route path="/offers/:id/edit" element={<OfferFormPage />} />
                <Route path="/onboarding-list" element={<OnboardingListPage />} />
                <Route path="/onboarding/application/:applicationId" element={<OnboardingPage />} />
                <Route path="/onboarding/:id" element={<OnboardingPage />} />
              </Route>

              {/* Admin-only routes — SYSTEM_ADMIN only */}
              <Route element={<AdminRoute />}>
                <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
                <Route path="/admin/users" element={<AdminUsersPage />} />
                <Route path="/admin/users/create" element={<AdminCreateUserPage />} />
                <Route path="/admin/users/:id/edit" element={<AdminEditUserPage />} />
                <Route path="/admin/departments" element={<DepartmentsPage />} />
                <Route path="/admin/system-config" element={<SystemConfigPage />} />
                <Route path="/admin/audit-logs" element={<AuditLogsPage />} />
                <Route path="/admin/notifications" element={<NotificationPage />} />
              </Route>
            </Route>
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
