import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import StatCard from "./StatCard";
import { Users } from "lucide-react";

afterEach(() => {
  cleanup();
});

describe("StatCard Component tests", () => {
  it("renders title, string value, and icon", () => {
    render(
      <StatCard
        title="Tổng ứng viên"
        value="1,250"
        icon={Users}
      />
    );

    expect(screen.getByText("Tổng ứng viên")).toBeTruthy();
    expect(screen.getByText("1,250")).toBeTruthy();
  });

  it("renders numeric value correctly", () => {
    render(
      <StatCard
        title="Phỏng vấn hôm nay"
        value={15}
        icon={Users}
      />
    );

    expect(screen.getByText("Phỏng vấn hôm nay")).toBeTruthy();
    expect(screen.getByText("15")).toBeTruthy();
  });

  it("renders description when provided", () => {
    render(
      <StatCard
        title="Đề xuất đang chờ"
        value="8"
        icon={Users}
        description="+12% so với tháng trước"
      />
    );

    expect(screen.getByText("+12% so với tháng trước")).toBeTruthy();
  });
});
