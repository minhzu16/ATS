import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { StageBadge } from "./StageBadge";
import { stageLabel, STAGE_CONFIG } from "./stage-badge.config";

afterEach(() => {
  cleanup();
});

describe("stageLabel and STAGE_CONFIG helper tests", () => {
  it("translates standard stages to human readable labels", () => {
    expect(stageLabel("APPLIED")).toBe("Applied");
    expect(stageLabel("applied")).toBe("Applied");
    expect(stageLabel("SCREENING")).toBe("Screening");
    expect(stageLabel("INTERVIEW")).toBe("Interview");
    expect(stageLabel("OFFER")).toBe("Offer");
    expect(stageLabel("HIRED")).toBe("Hired");
    expect(stageLabel("REJECTED")).toBe("Rejected");
  });

  it("replaces underscores in custom stage names", () => {
    expect(stageLabel("TECHNICAL_ASSESSMENT")).toBe("TECHNICAL ASSESSMENT");
    expect(stageLabel("FINAL_ROUND")).toBe("FINAL ROUND");
  });

  it("returns empty string for empty input", () => {
    expect(stageLabel("")).toBe("");
  });

  it("contains style configurations for all 6 core stages", () => {
    const keys = ["APPLIED", "SCREENING", "INTERVIEW", "OFFER", "HIRED", "REJECTED"];
    for (const k of keys) {
      expect(STAGE_CONFIG[k]).toBeDefined();
      expect(STAGE_CONFIG[k].bg).toBeTruthy();
      expect(STAGE_CONFIG[k].dot).toBeTruthy();
      expect(STAGE_CONFIG[k].text).toBeTruthy();
    }
  });
});

describe("StageBadge Component tests", () => {
  it("renders stage name and dot indicator by default", () => {
    const { container } = render(<StageBadge stage="INTERVIEW" />);
    expect(screen.getByText("Interview")).toBeTruthy();
    const dot = container.querySelector(".rounded-full");
    expect(dot).toBeTruthy();
    expect(dot?.getAttribute("aria-hidden")).toBe("true");
  });

  it("hides dot when showDot is false", () => {
    const { container } = render(<StageBadge stage="OFFER" showDot={false} />);
    expect(screen.getByText("Offer")).toBeTruthy();
    const dot = container.querySelector(".rounded-full");
    expect(dot).toBeNull();
  });

  it("merges custom className onto badge container", () => {
    const { container } = render(
      <StageBadge stage="HIRED" className="custom-test-class" />
    );
    expect(screen.getByText("Hired")).toBeTruthy();
    const badge = container.querySelector(".custom-test-class");
    expect(badge).toBeTruthy();
  });

  it("handles unknown or custom stages with fallback styling", () => {
    render(<StageBadge stage="CUSTOM_STAGE" />);
    expect(screen.getByText("CUSTOM STAGE")).toBeTruthy();
  });
});
