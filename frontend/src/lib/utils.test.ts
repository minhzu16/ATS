import { describe, it, expect } from "vitest";
import { cn } from "./utils";

describe("cn utility tests", () => {
  it("concatenates class names", () => {
    expect(cn("px-4", "py-2", "text-sm")).toBe("px-4 py-2 text-sm");
  });

  it("filters out falsy and conditional values", () => {
    const isHidden = false;
    const isPrimary = true;
    expect(cn("base-class", isHidden && "hidden", isPrimary && "text-primary", null, undefined)).toBe(
      "base-class text-primary"
    );
  });

  it("merges conflicting Tailwind classes favoring the latter", () => {
    expect(cn("px-2 text-red-500", "px-4 text-blue-500")).toBe("px-4 text-blue-500");
  });
});
