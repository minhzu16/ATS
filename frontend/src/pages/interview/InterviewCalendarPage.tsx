import { useEffect, useState } from "react";
import { ChevronLeft, ChevronRight, Plus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { interviewService } from "@/services/interviewService";
import type { InterviewResponse } from "@/types/interview";

const HOURS = [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]; // 8 AM to 8 PM

const getStartOfWeek = (date: Date) => {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
  d.setHours(0, 0, 0, 0);
  return d;
};

const addDays = (date: Date, days: number) => {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
};

const formatTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleTimeString("en-US", {
    hour: "numeric",
    minute: "2-digit",
  });
};

const getColorByType = (type: string) => {
  switch (type) {
    case "SCREENING":
      return "bg-blue-50 text-blue-600 border-blue-200 border";
    case "TECHNICAL":
      return "bg-purple-50 text-purple-600 border-purple-200 border";
    case "CULTURE_FIT":
      return "bg-pink-50 text-pink-600 border-pink-200 border";
    default:
      return "bg-gray-50 text-gray-600 border-gray-200 border";
  }
};

const getTitleByType = (type: string) => {
  switch (type) {
    case "SCREENING": return "Screening";
    case "TECHNICAL": return "Tech";
    case "CULTURE_FIT": return "Culture";
    default: return type;
  }
};

export default function InterviewCalendarPage() {
  const { user } = useAuth();
  const [currentWeekStart, setCurrentWeekStart] = useState(getStartOfWeek(new Date()));
  const [interviews, setInterviews] = useState<InterviewResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const showScheduleButton = user?.role !== "INTERVIEWER";

  const fetchInterviews = async () => {
    try {
      const data = await interviewService.getAllInterviews();
      setInterviews(data);
    } catch (error) {
      console.error("Failed to fetch interviews", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInterviews();
  }, []);

  const handlePrevWeek = () => setCurrentWeekStart(addDays(currentWeekStart, -7));
  const handleNextWeek = () => setCurrentWeekStart(addDays(currentWeekStart, 7));
  const handleToday = () => setCurrentWeekStart(getStartOfWeek(new Date()));

  // Monday to Friday
  const weekDays = Array.from({ length: 5 }).map((_, i) => addDays(currentWeekStart, i));

  const endOfWeek = addDays(currentWeekStart, 4);
  const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  const headerDateStr = `${monthNames[currentWeekStart.getMonth()]} ${currentWeekStart.getDate()} - ${currentWeekStart.getMonth() !== endOfWeek.getMonth() ? monthNames[endOfWeek.getMonth()] + ' ' : ''}${endOfWeek.getDate()}, ${endOfWeek.getFullYear()}`;

  return (
    <div className="flex h-full flex-col bg-white">
      {/* Header Controls */}
      <div className="flex items-center justify-between border-b p-4">
        <h1 className="text-2xl font-bold text-gray-800">Interview Calendar</h1>
      </div>
      <div className="flex items-center justify-between px-6 py-4">
        <div className="flex items-center space-x-4">
          <div className="flex items-center rounded-md border text-sm font-medium text-gray-700 shadow-sm">
            <button
              onClick={handlePrevWeek}
              className="flex items-center px-3 py-1.5 hover:bg-gray-50 rounded-l-md border-r"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <span className="px-4 py-1.5">{headerDateStr}</span>
            <button
              onClick={handleNextWeek}
              className="flex items-center px-3 py-1.5 hover:bg-gray-50 rounded-r-md border-l"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
          <button onClick={handleToday} className="text-sm font-medium text-gray-600 hover:text-gray-900">
            Today
          </button>
        </div>

        <div className="flex items-center space-x-4">
          <div className="flex rounded-md border shadow-sm">
            <button className="px-4 py-1.5 text-sm font-medium text-blue-600 bg-blue-50 border-r rounded-l-md">
              Week
            </button>
            <button className="px-4 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-50 rounded-r-md">
              Month
            </button>
          </div>
          {showScheduleButton && (
            <button className="flex items-center space-x-1 rounded-md bg-gray-900 px-4 py-1.5 text-sm font-medium text-white hover:bg-gray-800 shadow-sm">
              <Plus className="h-4 w-4" />
              <span>Schedule</span>
            </button>
          )}
        </div>
      </div>

      {/* Calendar Grid */}
      {loading ? (
        <div className="flex flex-1 items-center justify-center p-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-900 border-t-transparent" />
        </div>
      ) : (
      <div className="flex flex-1 overflow-auto rounded-md shadow-sm border border-gray-200 m-6 mt-0">
        <div className="flex min-w-[800px] flex-1 flex-col">
          {/* Day Headers */}
          <div className="flex border-b border-gray-200 bg-gray-50">
            <div className="w-16 flex-shrink-0" />
            {weekDays.map((day, i) => (
              <div key={i} className="flex-1 py-3 text-center text-sm font-medium text-gray-700 border-l border-gray-200">
                {day.toLocaleDateString("en-US", { weekday: "short" })} {day.getDate()}
              </div>
            ))}
          </div>

          {/* Time Rows */}
          <div className="relative flex-1 bg-white">
            {HOURS.map((hour) => (
              <div key={hour} className="flex border-b border-gray-100 h-28">
                <div className="w-16 flex-shrink-0 border-r border-gray-200 px-2 py-2 text-right text-xs font-medium text-gray-500">
                  {hour === 12 ? "12 PM" : hour > 12 ? `${hour - 12} PM` : `${hour} AM`}
                </div>
                {weekDays.map(( day, i ) => {
                  // Find event for this slot
                   const slotEvents = interviews.filter((inv: InterviewResponse) => {
                    if (!inv.scheduledAt) return false;
                    // Remove 'Z' to prevent timezone auto-conversion to +07:00
                    const localDateString = inv.scheduledAt.endsWith('Z') 
                          ? inv.scheduledAt.slice(0, -1) 
                          : inv.scheduledAt;
                    const invDate = new Date(localDateString);
                    return invDate.getDate() === day.getDate() &&
                           invDate.getMonth() === day.getMonth() &&
                           invDate.getFullYear() === day.getFullYear() &&
                           invDate.getHours() === hour;
                  });

                  return (
                    <div key={i} className="flex-1 border-r border-gray-100 p-1">
                      {slotEvents.map((evt: InterviewResponse) => {
                         const localStartStr = evt.scheduledAt.endsWith('Z') ? evt.scheduledAt.slice(0, -1) : evt.scheduledAt;
                         const startTime = formatTime(localStartStr);
                         
                         let endTime = "";
                         if (evt.endedAt) {
                            const localEndStr = evt.endedAt.endsWith('Z') ? evt.endedAt.slice(0, -1) : evt.endedAt;
                            endTime = formatTime(localEndStr);
                         } else {
                            // Default 1 hour duration if endedAt is missing
                            const d = new Date(localStartStr);
                            d.setHours(d.getHours() + 1);
                            endTime = formatTime(d.toISOString().slice(0, -1)); // Drop Z for consistency
                         }
                         
                         const isPast = new Date(localStartStr) < new Date();
                         
                         return (
                          <div
                            key={evt.id}
                            onClick={() => !isPast && navigate(`/interviews/${evt.id}`)}
                            className={`rounded p-2 text-xs mb-1 transition-shadow 
                              ${isPast 
                                ? 'opacity-50 grayscale cursor-not-allowed pointer-events-none' 
                                : 'cursor-pointer hover:shadow-md'
                              } ${getColorByType(evt.type)}`}
                            style={{ minHeight: '4.5rem' }}
                          >
                            <div className="font-semibold mb-1">
                              {isPast ? '[REJECTED] ' : ''}{getTitleByType(evt.type)}: {evt.candidateName || 'Candidate'}
                            </div>
                            <div className="text-opacity-80">
                              {startTime} - {endTime}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
        </div>
      </div>
      )}
    </div>
  );
}
