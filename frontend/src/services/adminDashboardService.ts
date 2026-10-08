/**
 * Admin Dashboard Service
 * Handles API communication for admin dashboard analytics, health, and user statistics
 */

import api from '@/lib/api';

export interface AdminAnalyticsDTO {
  totalApplications: number;
  newApplicationsThisMonth: number;
  conversionRate: number;
  timeToHireAverage: number;
  hiringFunnel: {
    APPLIED: number;
    SCREENING: number;
    INTERVIEW: number;
    OFFER: number;
    HIRED: number;
  };
  applicationsByDepartment?: Record<string, number>;
  topJobs?: Array<{
    jobId: string;
    jobTitle: string;
    applications: number;
    hired: number;
    conversionRate: number;
  }>;
  applicationsBySource?: Record<string, number>;
  rejectionReasons?: Record<string, number>;
  period: string;
}

export interface SystemHealthDTO {
  databaseHealthy: boolean;
  databaseStatus: string;
  applicationHealthy: boolean;
  applicationStatus: string;
  cpuUsage: number;
  memoryUsage: number;
  uptime: number;
  errorsLast24Hours: number;
  errorsLast7Days: number;
  errorRate: number;
  p95ResponseTime: number;
  p99ResponseTime: number;
  averageResponseTime: number;
  requestsPerSecond: number;
  healthScore: number;
  lastChecked: string;
}

export interface UserManagementStatsDTO {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  newUsersThisMonth: number;
  adminCount: number;
  hrManagerCount: number;
  recruiterCount: number;
  interviewerCount: number;
  averageLoginFrequency: number;
  usersInactiveMoreThan30Days: number;
  usersInactiveMoreThan60Days: number;
  failedLoginAttempts: number;
  accountsLocked: number;
  passwordExpiringSoon: number;
}

const API_BASE = '/admin/dashboard';

export const adminDashboardService = {
  /**
   * Fetch analytics data for the recruitment dashboard
   * @param period - Time period: '7days', '30days', 'quarter', or 'default'
   */
  getAnalytics: async (period: string = '30days'): Promise<AdminAnalyticsDTO> => {
    try {
      const { data } = await api.get<AdminAnalyticsDTO>(
        `${API_BASE}/analytics`,
        {
          params: { period },
        }
      );
      return data;
    } catch (error) {
      console.error('Failed to fetch analytics:', error);
      throw error;
    }
  },

  /**
   * Fetch system health metrics
   */
  getSystemHealth: async (): Promise<SystemHealthDTO> => {
    try {
      const { data } = await api.get<SystemHealthDTO>(
        `${API_BASE}/health`
      );
      return data;
    } catch (error) {
      console.error('Failed to fetch system health:', error);
      throw error;
    }
  },

  /**
   * Fetch user management statistics
   */
  getUserStats: async (): Promise<UserManagementStatsDTO> => {
    try {
      const { data } = await api.get<UserManagementStatsDTO>(
        `${API_BASE}/users-stats`
      );
      return data;
    } catch (error) {
      console.error('Failed to fetch user stats:', error);
      throw error;
    }
  },
};

export default adminDashboardService;
